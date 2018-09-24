import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import picocli.CommandLine.*;
import picocli.CommandLine.Command;

@Command
public class Git implements Runnable {
    private static final String currentDir = System.getProperty("user.dir");
    private static final String gitDir = currentDir + File.separator + "_git";
    private static final String gitObjDir = gitDir + File.separator + "objects";
    private static final String gitRefDir = gitDir + File.separator + "refs";
    private static final String masterBranch = gitRefDir + File.separator + "master";
    private static final String HEAD = gitRefDir + File.separator + "HEAD";
    private static final String gitIndex = gitDir + File.separator + "index";

    @Command(description = "Git init", mixinStandardHelpOptions = true)
    public void init() throws IOException {
        boolean dir = new File(gitDir).mkdirs();
        if (dir) {
            boolean obj = new File(gitObjDir).mkdirs();
            boolean refs = new File(gitRefDir).mkdirs();
            File index = new File(gitIndex);
            index.createNewFile();
            if (refs) {
                File master = new File(masterBranch);
                master.createNewFile();

                File head = new File(HEAD);
                head.createNewFile();
                try (PrintWriter writer = new PrintWriter(head)) {
                    writer.print("master");
                }
            }
        }
    }

    @Command(description = "Git add", mixinStandardHelpOptions = true)
    public static void add(
            @Parameters(arity = "1..*", paramLabel = "FILES") List<String> files)
            throws IOException, NoSuchAlgorithmException, ParseException {
        Iterator<String> it = files.iterator();
        JsonWrapper index = new JsonWrapper(gitIndex);
        String filename;
        String hash;
        String md;
        Utility ut = new Utility(gitObjDir);
        while (it.hasNext()) {
            filename = currentDir + File.separator + Utility.normalizePath(it.next());
            ArrayList<String> info = new ArrayList<>();
            hash = ut.createBlob(filename);
            md = Utility.getMD5(filename);
            info.add(hash);
            info.add(md);
            index.addItemArray(filename, info);
        }
        index.write();
    }

    @Command(description = "Git remove", mixinStandardHelpOptions = true)
    public static void rm(
            @Parameters(arity = "1..*", paramLabel = "FILES") List<String> files)
            throws IOException, NoSuchAlgorithmException, ParseException {
        JsonWrapper index = new JsonWrapper(gitIndex);
        JsonWrapper tree = getWorkingTree();
        String filename;
        for (Iterator<String> it = files.iterator(); it.hasNext(); ) {
            filename = currentDir + File.separator + Utility.normalizePath(it.next());
            index.deleteItem(filename);
            if (tree != null) {
                tree.deleteItem(filename);
            }
        }
        index.write();
        if (tree != null) {
            tree.write();
        }
    }


    @Command(description = "Git status", mixinStandardHelpOptions = true)
    public static void status() throws IOException, NoSuchAlgorithmException, ParseException {
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        System.out.println("# On branch " + currentBranch);
        ArrayList<String> untracked = new ArrayList<>();
        ArrayList<String> notStaged = new ArrayList<>();
        ArrayList<String> stagedM = new ArrayList<>();
        ArrayList<String> stagedN = new ArrayList<>();
        JSONObject wd = wdState();
        JsonWrapper cInfo = getWorkingTree();
        JsonWrapper index = new JsonWrapper(gitIndex);
        //TODO refactor this mess
        if (cInfo != null) {
            JSONObject commitInfo = cInfo.getJsonObject();
            for (Iterator iterator = wd.keySet().iterator(); iterator.hasNext(); ) {
                String wdKey = (String) iterator.next();
                if (index != null && !index.getJsonObject().containsKey(wdKey)) {
                    if (!commitInfo.containsKey(wdKey)) {
                        untracked.add(wdKey);
                    } else {
                        JSONArray valWd = (JSONArray) wd.get(wdKey);
                        JSONArray valC = (JSONArray) commitInfo.get(wdKey);
                        if (!(valWd.get(1).equals(valC.get(1)))) {
                            notStaged.add(wdKey);
                        }
                    }
                } else if (index != null && index.getJsonObject().containsKey(wdKey)) {
                    JSONArray valInd = (JSONArray) index.getArrayItem(wdKey);
                    JSONArray valC = (JSONArray) commitInfo.get(wdKey);
                    JSONArray valWd = (JSONArray) wd.get(wdKey);
                    if (!commitInfo.containsKey(wdKey)) {
                        stagedN.add(wdKey);
                    } else {
                        if (!(valInd.get(1).equals(valWd.get(1)))) {
                            notStaged.add(wdKey);
                        }
                        if (!(valInd.get(1).equals(valC.get(1)))) {
                            stagedM.add(wdKey);
                        }
                    }

                } else {
                    untracked.add(wdKey);
                }
            }
        } else {
            for (Iterator iterator = wd.keySet().iterator(); iterator.hasNext(); ) {
                String wdKey = (String) iterator.next();
                if (index != null) {
                    if (index.getJsonObject().containsKey(wdKey)) {
                        stagedN.add(wdKey);
                    } else {
                        untracked.add(wdKey);
                    }
                } else {
                    untracked.add(wdKey);
                }
            }

        }
        printStatus(untracked, notStaged, stagedM, stagedN);
    }

    private static void printStatus(List<String> untracked, List<String> notStaged,
                                    List<String> stagedM, List<String> stagedN) {
        if (!untracked.isEmpty()) {
            System.out.println("# Untracked files:");
            System.out.println("#");
            for (String name : untracked) {
                System.out.println("#   " + name);
            }
            System.out.println("#");
            System.out.println("#");
        }
        if (!stagedM.isEmpty() || !stagedN.isEmpty()) {
            System.out.println("# Changes to be committed:");
            System.out.println("#");
            for (String name : stagedN) {
                System.out.println("#   new file:   " + name);
            }
            for (String name : stagedM) {
                System.out.println("#   modified:   " + name);
            }
            System.out.println("#");
            System.out.println("#");
        }
        if (!notStaged.isEmpty()) {
            System.out.println("# Changes not staged for commit:");
            System.out.println("#");
            for (String name : notStaged) {
                System.out.println("#   modified:   " + name);
            }
        }
    }

    private static JSONObject wdState() throws IOException, NoSuchAlgorithmException {
        List<String> filesInDir = Utility.indexDir(currentDir);
        Utility ut = new Utility(gitObjDir);
        JSONObject wd = new JSONObject();
        for (String filename : filesInDir) {
            JSONArray values = new JSONArray();
            values.add(null);
            values.add(Utility.getMD5(filename));
            wd.put(filename, values);
        }
        return wd;
    }


    @Command(description = "Git commit", mixinStandardHelpOptions = true)
    public static void commit(
            @Option(names = {"-m", "-message"}) String message)
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
        JsonWrapper treeInfo;

        if (branch.length() == 0) { //first commit
            tree = ut.createTree(currentDir);
            commitInfo.put("parent", "");

        } else {
            String prevCommit = new String(Files.readAllBytes(branch.toPath()));
            commitInfo.put("parent", prevCommit);
            String treePrev = ut.getCommitTree(prevCommit);
            //copy previous tree
            tree = ut.createBlob(gitObjDir + File.separator + treePrev);
        }
        treeInfo = new JsonWrapper(gitObjDir + File.separator + tree);
        commitInfo.put("tree", tree);
        try (PrintWriter writer = new PrintWriter(commit)) {
            writer.print(commitInfo.toJSONString());
        }

        //update tree info
        JsonWrapper index = new JsonWrapper(gitIndex);
        Iterator<String> it = index.getKeyIterator();

        while (it.hasNext()) {
            String key = it.next();
            JSONArray values = index.getArrayItem(key);
            treeInfo.addItemArray(key, values);
        }
        treeInfo.write();
        index.clear();

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
        String result = new String(Files.readAllBytes(Paths.get(gitRefDir + File.separator + currentBranch)));
        if (result.isEmpty()) return null;
        else return result;
    }

    private static JsonWrapper getWorkingTree() throws IOException, ParseException {
        String curCommit = getCurrentCommit();
        if (curCommit != null) {
            JsonWrapper workTree = new JsonWrapper(gitObjDir + File.separator + curCommit);
            String tree = workTree.getItemSingle("tree");
            return new JsonWrapper(gitObjDir + File.separator + tree);
        } else return null;
    }

    /* double dash "--" was replaced to "-f" because of picocli lib
    When one of the command line arguments is just two dashes without any characters attached (--),
    picocli interprets all following arguments as positional parameters,
    even arguments that match an option name.
     */
    @Command(description = "Checkout files", mixinStandardHelpOptions = true)
    public static void checkout(
            @Option(names = "-f", description = "Checkout files") boolean cf,
            @Parameters(arity = "1..*", paramLabel = "FILES") List<String> args) throws IOException, ParseException {
        if (cf) {
            checkoutFiles(args);
        } else if (args.size() == 1) {
            checkoutBranch((String) args.get(0));
        }
    }


    private static void checkoutFiles(List<String> files) throws IOException, ParseException {
        JsonWrapper commitInfo = getWorkingTree();
        JsonWrapper index = new JsonWrapper(gitIndex);
        for (String file : files) {
            String filename = currentDir + File.separator + Utility.normalizePath(file);
            if (commitInfo.getJsonObject().containsKey(filename)) {
                String comCopy = (String) commitInfo.getArrayItem(filename).get(0);
                Files.copy(Paths.get(gitObjDir + File.separator + comCopy),
                        Paths.get(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            if (index.getJsonObject().containsKey(filename)) {
                index.deleteItem(filename);
                index.write();
            }
        }
    }


    private static void checkoutBranch(String revision)
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
                JSONArray revCopy = (JSONArray) revInfo.get(revKey);
                Files.copy(Paths.get(gitObjDir + File.separator + (String) revCopy.get(0)),
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
