import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class GitTest {

    private final String PATH = System.getProperty("user.dir") + File.separator + "examples";
    private final String PATH_GIT = System.getProperty("user.dir") + File.separator + "_git";
    private File testFileOne = new File(PATH + File.separator + "readme");
    private File testFileTwo = new File(PATH + File.separator + "merge");
    private File testFileThree = new File(PATH + File.separator + "Test.java");

    @Before
    public void setUp() {

        boolean dir = new File(PATH).mkdirs();

        boolean obj = new File(PATH + File.separator + "lib").mkdirs();


        File testFileFour = new File(PATH + File.separator + "lib" + File.separator + "Class.java");

        try {
            testFileOne.createNewFile();
            testFileTwo.createNewFile();
            testFileThree.createNewFile();
            testFileFour.createNewFile();
            try (PrintWriter writer = new PrintWriter(testFileOne)) {
                writer.print("java <3 1");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] init = {"init"};
        CommandLine.run(new Git(), init);

    }

    @Test
    public void testCreateAndDeleteBranch() {
        String[] add = {"add", "./examples/readme"};
        CommandLine.run(new Git(), add);
        String[] firstCommit = {"commit", "-m", "first commit"};
        CommandLine.run(new Git(), firstCommit);
        int oldBranchesNumber = new File(PATH_GIT+ File.separator + "refs").list().length;
        String[] newBranch = {"checkout", "-b", "justbranch"};
        CommandLine.run(new Git(), newBranch);
        int newBranchesNumber = new File(PATH_GIT+ File.separator + "refs").list().length;
        assertEquals(oldBranchesNumber + 1, newBranchesNumber);
        String[] deleteBranch = {"branch", "-d", "justbranch"};
        CommandLine.run(new Git(), deleteBranch);
        assertEquals(oldBranchesNumber + 1, new File(PATH_GIT+ File.separator + "refs").list().length);
        String[] checkoutRev = {"checkout", "master"};
        CommandLine.run(new Git(), checkoutRev);
        CommandLine.run(new Git(), deleteBranch);
        assertEquals(oldBranchesNumber, new File(PATH_GIT+ File.separator + "refs").list().length);

    }

    @Test
    public void testMergeComplex() {
        String[] status = {"status"};
        String[] secondCommit = {"commit", "-m", "second commit"};
        String[] checkoutRev = {"checkout", "master"};
        String[] newBranch = {"checkout", "-b", "newbranch"};
        String[] addMerge = {"add", "./examples/merge"};
        String[] addNewFile = {"add", "./examples/Test.java"};
        String[] commitToNewBranch = {"commit", "-m", "commit to second branch"};
        String[] commitToMaster = {"commit", "-m", "change ./examples/merge"};
        String[] commitFinal = {"commit", "-m", "merge with new branch"};
        String[] merge = {"merge", "newbranch"};

        CommandLine.run(new Git(), addMerge);
        CommandLine.run(new Git(), secondCommit);

        //new branch
        CommandLine.run(new Git(), newBranch);
        CommandLine.run(new Git(), addNewFile);
        try (PrintWriter writer = new PrintWriter(testFileTwo)) {
            writer.print("totally different content to merge later");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        CommandLine.run(new Git(), addMerge);
        CommandLine.run(new Git(), commitToNewBranch);

        //come back to master
        CommandLine.run(new Git(), checkoutRev);
        try (PrintWriter writer = new PrintWriter(testFileTwo)) {
            writer.println("this is some content to mess with");
            writer.println("content to append");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        CommandLine.run(new Git(), addMerge);
        CommandLine.run(new Git(), commitToMaster);
        CommandLine.run(new Git(), merge);
        CommandLine.run(new Git(), status);
        CommandLine.run(new Git(), addMerge);
        CommandLine.run(new Git(), commitFinal);

        String merged = "";
        try {
            merged = new String(Files.readAllBytes(testFileTwo.toPath()));
        } catch (IOException e) {
            fail();
        }
        String newline = System.getProperty("line.separator");
        assertEquals("<<<<<<< HEAD" + newline +
                "this is some content to mess with" + newline +
                "content to append" + newline +
                "=======" + newline +
                "totally different content to merge later" + newline +
                ">>>>>>> new branch to commit" + newline, merged);



        File[] files = new File(PATH).listFiles();
        assertTrue(Arrays.asList(files).contains(testFileOne));
        assertTrue(Arrays.asList(files).contains(testFileThree));
    }

    /*
    private void deleter(String pathDel) throws IOException {
        File testDir = new File(pathDel);
        Files.walk(testDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }



    @After
    public void tearDown() throws IOException {
        try {
            //deleter(PATH);
            //deleter(PATH_GIT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

}