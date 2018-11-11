import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import picocli.CommandLine.*;
import picocli.CommandLine.Command;

import static java.util.stream.Collectors.toList;

@Command
public class Git implements Runnable {
    private static final String CURRENT_DIR = System.getProperty("user.dir");
    private static final String GIT_DIR = CURRENT_DIR + File.separator + "_git";
    private static final String GIT_OBJ_DIR = GIT_DIR + File.separator + "objects";
    private static final String GIT_REF_DIR = GIT_DIR + File.separator + "refs";
    private static final String MASTER_BRANCH = GIT_REF_DIR + File.separator + "master";
    private static final String HEAD = GIT_REF_DIR + File.separator + "HEAD";
    private static final String GIT_INDEX = GIT_DIR + File.separator + "index";
    private static final String GIT_INDEX_DEL = GIT_DIR + File.separator + "index_del";

    @Command(description = "Git init", mixinStandardHelpOptions = true)
    public void init() throws IOException {
        boolean dir = new File(GIT_DIR).mkdirs();
        if (dir) {
            boolean refs = new File(GIT_REF_DIR).mkdirs();
            File index = new File(GIT_INDEX);
            index.createNewFile();
            File indexDel = new File(GIT_INDEX_DEL);
            indexDel.createNewFile();
            if (refs) {
                File master = new File(MASTER_BRANCH);
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
        JsonWrapper index = new JsonWrapper(GIT_INDEX);
        String filename;
        String hash;
        String md;
        Utility ut = new Utility(GIT_OBJ_DIR);
        while (it.hasNext()) {
            filename = CURRENT_DIR + File.separator + Utility.normalizePath(it.next());
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
        JsonWrapper index = new JsonWrapper(GIT_INDEX_DEL);
        String filename;
        for (Iterator<String> it = files.iterator(); it.hasNext(); ) {
            filename = CURRENT_DIR + File.separator + Utility.normalizePath(it.next());
            index.addItemSingle(filename, "");
        }
        index.write();

    }


    @Command(description = "Git status", mixinStandardHelpOptions = true)
    public static void status() throws IOException, NoSuchAlgorithmException, ParseException {
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        System.out.println("# On branch " + currentBranch);
        ArrayList<String> untracked = new ArrayList<>();
        ArrayList<String> notStaged = new ArrayList<>();
        ArrayList<String> stagedM = new ArrayList<>();
        ArrayList<String> stagedN = new ArrayList<>();
        ArrayList<String> stagedD = new ArrayList<>();
        ArrayList<String> notStagedD = new ArrayList<>();
        JSONObject wd = wdState();
        JsonWrapper cInfo = getWorkingTree();
        JsonWrapper index = new JsonWrapper(GIT_INDEX);
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

                }
                else {
                    untracked.add(wdKey);
                }
            }
            //manage deleted files
            for (Iterator iterator = commitInfo.keySet().iterator(); iterator.hasNext(); ) {
                String cKey = (String) iterator.next();
                JsonWrapper indexDel = new JsonWrapper(GIT_INDEX_DEL);
                if ((indexDel!=null && !indexDel.getJsonObject().containsKey(cKey) && !wd.containsKey(cKey)) ||
                        indexDel == null && !wd.containsKey(cKey)) {
                    notStagedD.add(cKey);
                }
                else if (indexDel!=null && indexDel.getJsonObject().containsKey(cKey)) {
                    stagedD.add(cKey);
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
        printStatus(untracked, notStaged, stagedM, stagedN, stagedD, notStagedD);
    }

    private static void printStatus(List<String> untracked, List<String> notStaged,
                                    List<String> stagedM, List<String> stagedN, List<String> stagedD, List<String> notStagedD) {
        if (!untracked.isEmpty()) {
            System.out.println("# Untracked files:");
            System.out.println("#");
            for (String name : untracked) {
                System.out.println("#   " + name);
            }
            System.out.println("#");
            System.out.println("#");
        }
        if (!stagedM.isEmpty() || !stagedN.isEmpty() || !stagedD.isEmpty()) {
            System.out.println("# Changes to be committed:");
            System.out.println("#");
            for (String name : stagedN) {
                System.out.println("#   new file:   " + name);
            }
            for (String name : stagedM) {
                System.out.println("#   modified:   " + name);
            }
            for (String name : stagedD) {
                System.out.println("#   deleted:   " + name);
            }
            System.out.println("#");
            System.out.println("#");
        }
        if (!notStaged.isEmpty() || !notStagedD.isEmpty()) {
            System.out.println("# Changes not staged for commit:");
            System.out.println("#");
            for (String name : notStaged) {
                System.out.println("#   modified:   " + name);
            }
            for (String name : notStagedD) {
                System.out.println("#   deleted:   " + name);
            }
        }
    }

    //возвращаемая здесь штука вполне могла быть не джсоном
    private static JSONObject wdState() throws IOException, NoSuchAlgorithmException {
        List<String> filesInDir = Utility.indexDir(CURRENT_DIR);
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
    public static String commit(
            @Option(names = {"-m", "-message"}) String message)
            throws IOException, ParseException {
        JSONObject commitInfo = new JSONObject();
        commitInfo.put("message", message);
        commitInfo.put("timestamp", Utility.getCurrentTime());

        String commitFilename = Utility.generateRandomHash();
        File commit = new File(GIT_OBJ_DIR + File.separator + commitFilename);
        commit.createNewFile();

        Utility ut = new Utility(GIT_OBJ_DIR);
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        File branch = new File(GIT_REF_DIR + File.separator + currentBranch);
        String tree;
        JsonWrapper treeInfo;

        if (branch.length() == 0) { //first commit
            tree = ut.createTree(CURRENT_DIR);
            commitInfo.put("parent", "");

        } else {
            String prevCommit = new String(Files.readAllBytes(branch.toPath()));
            commitInfo.put("parent", prevCommit);
            String treePrev = ut.getCommitTree(prevCommit);
            //copy previous tree
            tree = ut.createBlob(GIT_OBJ_DIR + File.separator + treePrev);
        }
        treeInfo = new JsonWrapper(GIT_OBJ_DIR + File.separator + tree);
        commitInfo.put("tree", tree);
        try (PrintWriter writer = new PrintWriter(commit)) {
            writer.print(commitInfo.toJSONString());
        }

        //update tree info
        JsonWrapper index = new JsonWrapper(GIT_INDEX);
        Iterator<String> it = index.getKeyIterator();

        while (it.hasNext()) {
            String key = it.next();
            JSONArray values = index.getArrayItem(key);
            treeInfo.addItemArray(key, values);
        }
        treeInfo.write();
        index.clear();

        //delete files
        index = new JsonWrapper(GIT_INDEX_DEL);
        it = index.getKeyIterator();

        while (it.hasNext()) {
            String key = it.next();
            treeInfo.deleteItem(key);
        }
        treeInfo.write();
        index.clear();

        //write current commit to revision
        try (PrintWriter writer = new PrintWriter(branch)) {
            writer.print(commitFilename);
        }
        return commitFilename;
    }

    @Command(description = "Get log of commits", mixinStandardHelpOptions = true)
    public static void log(
            @Parameters(arity = "0..1", paramLabel = "commit") String revision)
            throws IOException, ParseException {
        Utility ut = new Utility(GIT_OBJ_DIR);
        String commit;
        if (revision == null) {
            commit = getCurrentCommit();
        } else {
            commit = revision;
        }
        if (commit!=null && !commit.isEmpty()) {
            printAllCommits(commit, "");
        }
    }

    private static void printAllCommits(String commit, String untilCommit) throws IOException, ParseException {
        if (!commit.isEmpty()) {
            if (commit.equals(untilCommit)) {
                return;
            }
            Utility ut = new Utility(GIT_OBJ_DIR);
            JSONObject commitInfo = ut.getJsonInfo(commit);
            printCommit(commitInfo, commit);
            String parent = (String) commitInfo.get("parent");
            if (commitInfo.containsKey("merged")) {
                String parentOne = (String) commitInfo.get("parentOne");
                String parentTwo = (String) commitInfo.get("parentTwo");
                printAllCommits(parentOne, parent);
                printAllCommits(parentTwo, parent);
            }
            printAllCommits(parent, untilCommit);
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

        String commit = new String(Files.readAllBytes(Paths.get(GIT_REF_DIR + File.separator + currentBranch)));
        return commit;
    }


    @Command(description = "Reset to head", mixinStandardHelpOptions = true)
    public static void reset(
            @Parameters(arity = "1", paramLabel = "commit") String commit)
            throws IOException, ParseException {
        // reset and then log
        String oldCommit = getCurrentCommit();
        //replace head branch pointer
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        try (PrintWriter writer = new PrintWriter(new File(GIT_REF_DIR + File.separator + currentBranch))) {
            writer.print(commit);
        }
        //clean metadata
        JsonWrapper index = new JsonWrapper(GIT_INDEX);
        index.clear();
        index.write();
        //copy to working directory
        updateFromTree(oldCommit, commit);

    }

    private static void updateFromTree(String currentCommit, String updateFromCommit) throws IOException, ParseException {
        Utility ut = new Utility(GIT_OBJ_DIR);
        JSONObject currInfo = ut.getJsonInfo(currentCommit);
        currInfo = ut.getJsonInfo((String) currInfo.get("tree"));

        JSONObject revInfo = ut.getJsonInfo(updateFromCommit);
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
                file.createNewFile();
            }
        }

        //replace contents of matching files
        for (Iterator iterator = revInfo.keySet().iterator(); iterator.hasNext(); ) {
            String revKey = (String) iterator.next();
            if (currInfo.containsKey(revKey)) {
                JSONArray revCopy = (JSONArray) revInfo.get(revKey);
                Files.copy(Paths.get(GIT_OBJ_DIR + File.separator + (String) revCopy.get(0)),
                        Paths.get(revKey),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }


    }

    private static String getCurrentCommit() throws IOException {
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        String result = new String(Files.readAllBytes(Paths.get(GIT_REF_DIR + File.separator + currentBranch)));
        if (result.isEmpty()) return null;
        else return result;
    }

    private static JsonWrapper getWorkingTree() throws IOException, ParseException {
        String curCommit = getCurrentCommit();
        if (curCommit != null) {
            JsonWrapper workTree = new JsonWrapper(GIT_OBJ_DIR + File.separator + curCommit);
            String tree = workTree.getItemSingle("tree");
            return new JsonWrapper(GIT_OBJ_DIR + File.separator + tree);
        } else return null;
    }

    /* double dash "--" was replaced to "-f" because of picocli lib
    When one of the command line arguments is just two dashes without any characters attached (--),
    picocli interprets all following arguments as positional parameters,
    even arguments that match an option name.
     */
    //TODO: fix error log after reset
    @Command(description = "Checkout files", mixinStandardHelpOptions = true)
    public static void checkout(
            @Option(names = {"-b", "--branch"}) boolean newBranch,
            @Option(names = "-f", description = "Checkout files") boolean cf,
            @Parameters(arity = "1..*", paramLabel = "FILES") List<String> args) throws IOException, ParseException {
        if (cf) {
            checkoutFiles(args);
        }
        else if (newBranch && args.size() == 1) {
            branch(false, (String) args.get(0));
            try (PrintWriter writer = new PrintWriter(HEAD)) {
                writer.print((String) args.get(0));
            }
        }
        else if (args.size() == 1) {
            String name = (String) args.get(0);
            Path here = Files.list(Paths.get(GIT_REF_DIR))
                    .filter(x -> name.equals(x.getFileName().toString()))
                    .findAny()
                    .orElse(null);
            if (here == null) { //no such branch, trying to checout revision
                checkoutRevision(name);
            }
            else {
                checkoutBranch(name);
            }
        }

    }


    @Command(description = "Creates new branch", mixinStandardHelpOptions = true)
    public static void branch(
            @Option(names =  {"-d", "--delete"}, description = "Delete branch") boolean del,
            @Parameters(arity = "1", paramLabel = "BRANCH_NAME") String branch) throws IOException {
        if (del) {
            String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
            if (currentBranch.equals(branch)) {
                System.out.println("Deleting the current branch is denied!");
            }
            else {
                Files.deleteIfExists(Paths.get(GIT_REF_DIR + File.separator + branch));
            }
            return;
        }

        File folder = new File(GIT_REF_DIR);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> files = new ArrayList<>();

        for (File file : listOfFiles) {
            if ((file.getName()).equals(branch)) {
                System.out.println("Branch " + branch + " already exists!");
                return;
            }
        }

        Path newBranchPath = Paths.get(GIT_REF_DIR + File.separator + branch);
        Files.createFile(newBranchPath);
        String currrentCommit = getCurrentCommit();
        if (currrentCommit!=null) {
            try (PrintWriter writer = new PrintWriter(GIT_REF_DIR + File.separator + branch)) {
                writer.print(currrentCommit);
            }
        }
    }

    private static void checkoutFiles(List<String> files) throws IOException, ParseException {
        JsonWrapper commitInfo = getWorkingTree();
        JsonWrapper index = new JsonWrapper(GIT_INDEX);
        for (String file : files) {
            String filename = CURRENT_DIR + File.separator + Utility.normalizePath(file);
            if (commitInfo.getJsonObject().containsKey(filename)) {
                String comCopy = (String) commitInfo.getArrayItem(filename).get(0);
                Files.copy(Paths.get(GIT_OBJ_DIR + File.separator + comCopy),
                        Paths.get(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            if (index.getJsonObject().containsKey(filename)) {
                index.deleteItem(filename);
                index.write();
            }
        }
    }

    private static void checkoutRevision(String revision) throws IOException, ParseException {
        updateFromTree(getCurrentCommit(), revision);
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        try (PrintWriter writer = new PrintWriter(GIT_REF_DIR + File.separator + currentBranch)) {
            writer.print(revision);
        }
    }


    private static void checkoutBranch(String revision)
            throws IOException, ParseException {
        String revisionCommit = new String(Files.readAllBytes(Paths.get(GIT_REF_DIR + File.separator + revision)));
        if (!revisionCommit.isEmpty()) {
            updateFromTree(getCurrentCommit(), revisionCommit);
        }
        else {
            JsonWrapper index = new JsonWrapper(GIT_INDEX);
            index.clear();
            index.write();
        }
        try (PrintWriter writer = new PrintWriter(HEAD)) {
            writer.print(revision);
        }

    }

    @Command(description = "Merge two branches", mixinStandardHelpOptions = true)
    public static void merge(
            @Parameters(arity = "1", paramLabel = "BRANCH_NAME") String branch) throws IOException, ParseException, NoSuchAlgorithmException {
        String commitX = getCurrentCommit();
        String commitY = getRevisionCommit(branch);
        Utility ut = new Utility(GIT_OBJ_DIR);
        String commitW = ut.LCA(commitX, commitY);
        String currentBranch = new String(Files.readAllBytes(Paths.get(HEAD)));
        if (commitW.equals(commitX) || commitW.equals(commitY)) {     //current is parent of new branch
            updateFromTree(commitX, commitY);
            try (PrintWriter writer = new PrintWriter(GIT_REF_DIR + File.separator + currentBranch)) {
                writer.print(commitY);
            }
            return;
        }
        if (!commitW.isEmpty()) {
            JsonWrapper cX = new JsonWrapper(GIT_OBJ_DIR + File.separator + commitX);
            JsonWrapper cY = new JsonWrapper(GIT_OBJ_DIR + File.separator + commitY);
            JsonWrapper cW = new JsonWrapper(GIT_OBJ_DIR + File.separator + commitW);

            JsonWrapper treeX = new JsonWrapper(GIT_OBJ_DIR + File.separator + cX.getItemSingle("tree"));
            JsonWrapper treeY = new JsonWrapper(GIT_OBJ_DIR + File.separator + cY.getItemSingle("tree"));
            JsonWrapper treeW = new JsonWrapper(GIT_OBJ_DIR + File.separator + cW.getItemSingle("tree"));
            JsonWrapper workingTree = new JsonWrapper(GIT_OBJ_DIR + File.separator + cX.getItemSingle("tree"));


            //intersection of X and Y -- checkConflicts
            ArrayList<String> conflict = new ArrayList<>();

            Iterator<String> iterX = treeX.getKeyIterator();
            while (iterX.hasNext()) {
                String key = iterX.next();
                if (!treeY.hasKey(key) && !treeW.hasKey(key)) {
                    System.out.println("Auto-merging " + key);
                }
                else if (treeY.hasKey(key)) {
                    if (((String) (treeY.getArrayItem(key).get(1))).equals((String) (treeX.getArrayItem(key).get(1)))) { //identical contents
                        System.out.println("Auto-merging " + key);
                    }
                    else {
                        conflict.add(key);
                    }
                }
            }
            JsonWrapper index = new JsonWrapper(GIT_INDEX);

            //files exists only in Y -- will be added
            Iterator<String> iterY = treeY.getKeyIterator();
            while (iterY.hasNext()) {
                String key = iterY.next();
                if (!treeX.hasKey(key) && !treeW.hasKey(key)) {
                    System.out.println("Auto-merging " + key);
                    File file = new File(key);
                    File parent = file.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw new IllegalStateException("Couldn't create dir: " + parent);
                    }
                    file.createNewFile();
                    Files.copy(Paths.get(GIT_OBJ_DIR + File.separator + (String) treeY.getArrayItem(key).get(0)),
                            Paths.get(key),
                            StandardCopyOption.REPLACE_EXISTING);
                    index.addItemArray(key, (JSONArray) treeY.getArrayItem(key));
                }
            }
            index.write();


            //files exists only in W -- will be deleted
            Iterator<String> iterW = treeW.getKeyIterator();
            while (iterW.hasNext()) {
                String key = iterW.next();
                if (!treeX.hasKey(key) && !treeY.hasKey(key)) {
                    File file = new File(key);
                    file.delete();
                }
            }
            if (!conflict.isEmpty()) {
                solveConflicts(conflict, treeX, treeY);
            }

            //create special merge commit
            createMergeCommit(branch, commitX, commitY, commitW);
        }
    }

    private static void createMergeCommit(String branch, String commitX, String commitY, String parent)
            throws IOException, ParseException {
        String commitFilename = commit("Merge branch " + branch);
        JsonWrapper commitInfo = new JsonWrapper(GIT_OBJ_DIR + File.separator + commitFilename);
        commitInfo.addItemSingle("parent", parent);
        commitInfo.addItemSingle("parentOne", commitX);
        commitInfo.addItemSingle("parentTwo", commitY);
        commitInfo.addItemSingle("merged", "true");
        commitInfo.write();

    }

    private static void solveConflicts(ArrayList<String> conflict, JsonWrapper treeX, JsonWrapper treeY) throws IOException, ParseException {
        for (String filename : conflict) {
            String filenameOne = (String) treeX.getArrayItem(filename).get(0);
            String filenameTwo = (String) treeY.getArrayItem(filename).get(0);
            FileMerger fm = new FileMerger(filename,
                    GIT_OBJ_DIR + File.separator + filenameOne,
                    GIT_OBJ_DIR + File.separator + filenameTwo
            );

            System.out.println("Auto-merging " + filename);
            System.out.println("CONFLICT (content): Merge conflict in " + filename);
            List<String> mergeResult = fm.runMerge();
            try (PrintWriter writer = new PrintWriter(filename)) {
                for (String s : mergeResult) {
                    writer.println(s);
                }
            }
            System.out.println("Automatic merge failed; fix conflicts and then commit the result.");
   }

    }


    @Override
    public void run() {
        System.out.println("Test");
    }
}
