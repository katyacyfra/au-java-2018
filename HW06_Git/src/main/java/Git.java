import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import picocli.CommandLine.*;
import picocli.CommandLine.Command;

@Command
public class Git implements  Runnable{
    private static final String currentDir = System.getProperty("user.dir");
    private static final String gitDir = currentDir + File.separator + "_git";
    private static final String gitObjDir = gitDir + File.separator + "objects";
    private static final String gitRefDir = gitDir + File.separator + "refs";
    private static final String masterBranch = gitRefDir + File.separator + "master";
    private static final String HEAD = gitRefDir + File.separator + "HEAD";

    @Command(description = "Git init", mixinStandardHelpOptions = true)
    public void init() throws IOException {
        boolean dir = new File(gitDir).mkdirs();
        boolean obj = new File(gitObjDir).mkdirs();
        boolean refs = new File(gitRefDir).mkdirs();
        if (refs == true) {
            File master = new File(masterBranch);
            master.createNewFile();

            File head = new File(HEAD);
            head.createNewFile();
            try (PrintWriter writer = new PrintWriter(head)) {
                writer.print("master");
            }
        }
    }

    @Command(description = "Git commit", mixinStandardHelpOptions = true)
    public static void commit(
            @Option(names = {"-m", "-message"}) String message,
            @Parameters(arity="1..*", paramLabel = "FILE") List<String> files)
            throws IOException, ParseException {
        JSONObject commitInfo = new JSONObject();
        commitInfo.put("message", message);
        commitInfo.put("timestamp", Utility.getCurrentTime());

        String commitFilename = Utility.generateRandomHash();
        File commit = new File(gitObjDir + File.separator + commitFilename);
        commit.createNewFile();

        Utility ut = new Utility(gitObjDir);
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        File branch = new File(gitRefDir + File.separator + currentBranch);
        String tree;
        JSONObject treeInfo;

        if (branch.length() == 0) { //first commit
            tree = ut.createTree(currentDir);
            treeInfo = new JSONObject();
            commitInfo.put("parent", "");

        } else {
            String prevCommit = new String(Files.readAllBytes(branch.toPath()));
            commitInfo.put("parent", prevCommit);
            String treePrev = ut.getCommitTree(prevCommit);
            //copy previous tree
            tree = ut.createBlob(gitObjDir + File.separator + treePrev);
            treeInfo = ut.getJsonInfo(tree);

        }
        commitInfo.put("tree", tree);
        try (PrintWriter writer = new PrintWriter(commit)) {
            writer.print(commitInfo.toJSONString());
        }

        //update tree info
        Iterator<String> it = files.iterator();
        String filename;
        String hash;
        while (it.hasNext()) {
            filename = currentDir + File.separator + Utility.normalizePath(it.next());
            //compare changes
            if (treeInfo.containsKey(filename)) {
                boolean notEqual;
                try {

                    notEqual = !(Utility.equalHash(filename, gitObjDir + File.separator + (String) treeInfo.get(filename)));
                } catch (Exception e) {
                    //do nothing
                    e.printStackTrace();
                    notEqual = false;
                }
                if (notEqual) {
                    hash = ut.createBlob(filename);
                    treeInfo.put(filename, hash);
                }
            } else {
                hash = ut.createBlob(filename);
                treeInfo.put(filename, hash);
            }
        }
        try (PrintWriter writer = new PrintWriter(new File(gitObjDir + File.separator + tree))) {
            writer.print(treeInfo.toJSONString());
        }
        //write current commit to revision
        try (PrintWriter writer = new PrintWriter(branch)) {
            writer.print(commitFilename);
        }
    }


    @Command(description = "Get log of commits", mixinStandardHelpOptions = true)
    public static void log(
            @Parameters(arity = "0..1", paramLabel = "revision") String revision)
            throws IOException, ParseException {
        Utility ut = new Utility(gitObjDir);
        String commit = getRevisionCommit(revision);
        while (!"".equals(commit)) {
            JSONObject commitInfo = ut.getJsonInfo(commit);
            printCommit(commitInfo, commit);
            commit = (String) commitInfo.get("parent");
        }
    }

    private static void printCommit(JSONObject commitInfo, String commit) {
        System.out.println("commit " + commit);
        System.out.println("Date: " + (String) commitInfo.get("timestamp"));
        System.out.println();
        System.out.println("        " + (String) commitInfo.get("message"));
        System.out.println();
    }

    private static String getRevisionCommit(String revision) throws IOException {
        String currentBranch;
        if (revision == null) {//take current from head
            currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        } else {
            currentBranch = revision;
        }

        String commit = new String(Files.readAllBytes(Paths.get(gitRefDir + File.separator + currentBranch)));
        return commit;
    }

    /**
     * works like reset --soft in real git
     */
    @Command(description = "Reset to commit", mixinStandardHelpOptions = true)
    public static void reset(
            @Parameters(arity = "1", paramLabel = "commit") String commit)
            throws IOException {
        //replace head branch pointer
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        try (PrintWriter writer = new PrintWriter(new File(gitRefDir + File.separator + currentBranch))) {
            writer.print(commit);
        }
    }

    private static String getCurrentCommit() throws IOException {
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        return new String(Files.readAllBytes(Paths.get(gitRefDir + File.separator + currentBranch)));
    }

    @Command(description = "Checkout", mixinStandardHelpOptions = true)
    public static void checkout(
            @Parameters(arity = "1", paramLabel = "branch") String revision)
            throws IOException, ParseException {
        Utility ut = new Utility(gitObjDir);
        //replace head pointer
        String currentCommit = getCurrentCommit();
        String revisionCommit = new String(Files.readAllBytes(Paths.get(gitRefDir + File.separator + revision)));

        JSONObject currInfo = ut.getJsonInfo(currentCommit);
        currInfo = ut.getJsonInfo((String) currInfo.get("tree"));

        JSONObject revInfo = ut.getJsonInfo(revisionCommit);
        revInfo = ut.getJsonInfo((String) revInfo.get("tree"));

        //delete files which are not in new branch
        for (Iterator iterator = currInfo.keySet().iterator(); iterator.hasNext(); ) {
            String currKey = (String) iterator.next();

            if (!revInfo.containsKey(currKey)) {
                File file = new File(currKey);
                file.delete();
            }
        }

        //create new files from revision
        for (Iterator iterator = revInfo.keySet().iterator(); iterator.hasNext(); ) {
            String revKey = (String) iterator.next();
            if (!currInfo.containsKey(revKey)) {
                File file = new File(revKey);
                File parent = file.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + parent);
                }
            }
        }

        //replace contents of matching files
        for (Iterator iterator = revInfo.keySet().iterator(); iterator.hasNext(); ) {
            String revKey = (String) iterator.next();
            if (currInfo.containsKey(revKey)) {
                String revCopy = (String) revInfo.get(revKey);
                Files.copy(Paths.get(gitObjDir + File.separator + revCopy),
                        Paths.get(revKey),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }

        try (PrintWriter writer = new PrintWriter(HEAD)) {
            writer.print(revision);
        }
    }

    @Override
    public void run() {
        System.out.println("Test");
    }
}
