import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public class GitTest {

    private String path = System.getProperty("user.dir") + File.separator + "examples";
    private String pathGit = System.getProperty("user.dir") + File.separator + "_git";
    private File testFileOne = new File(path + File.separator + "readme");

    @Before
    public void setUp() {

        boolean dir = new File(path).mkdirs();

        boolean obj = new File(path + File.separator + "lib").mkdirs();

        File testFileTwo = new File(path + File.separator + "Test.java");
        File testFileThree = new File(path + File.separator + "lib" + File.separator + "Class.java");

        try {
            testFileOne.createNewFile();
            testFileTwo.createNewFile();
            testFileThree.createNewFile();
            try (PrintWriter writer = new PrintWriter(testFileOne)) {
                writer.print("java <3 1");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPathNormalization() {
        assertEquals("C:\\Users\\Katya\\Documents\\AU\\Java\\ДЗ\\HW05_Git\\test\\readme",
                Utility.normalizePath("C:\\\\Users\\\\Katya\\\\Documents\\\\AU\\\\Java\\\\ДЗ\\\\HW05_Git\\\\"
                        + "./test/readme"));
    }


    @Test
    public void testHistory() {
        String[] init = {"init"};
        String[] status = {"status"};
        String[] firstCommit = {"commit", "-m", "first commit"};
        String[] secondCommit = {"commit", "-m", "second commit"};
        String[] checkoutRev = {"checkout", "master"};
        String[] add = {"add", "./examples/readme"};
        String[] rm = {"rm", "./examples/readme"};
        String[] checkoutFile = {"checkout", "-f", "./examples/readme"};


        CommandLine.run(new Git(), init);
        System.out.println("Status after init");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);

        CommandLine.run(new Git(), add);
        System.out.println("Status after add ./examples/readme");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);

        CommandLine.run(new Git(), rm);
        System.out.println("Status after rm ./examples/readme");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);

        CommandLine.run(new Git(), add);
        System.out.println("Status after add ./examples/readme");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);

        CommandLine.run(new Git(), firstCommit);
        System.out.println("Status after commit");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);


        try (PrintWriter writer = new PrintWriter(testFileOne)) {
            writer.print("java <3 2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        System.out.println("Status after changing repo file");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);

        CommandLine.run(new Git(), add);
        System.out.println("Status after add ./example/readme");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);


        CommandLine.run(new Git(), checkoutFile);
        System.out.println("Status after checkout -f ./example/readme");
        System.out.println("******************************************************************");
        CommandLine.run(new Git(), status);

        String current = "";
        try {
            current = new String(Files.readAllBytes(testFileOne.toPath()));
        } catch (IOException e) {
            fail();
        }
        assertEquals("java <3 1", current);


        try (PrintWriter writer = new PrintWriter(testFileOne)) {
            writer.print("java <3 3");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        CommandLine.run(new Git(), add);

        String firstCommitVal = "";
        try {
            firstCommitVal = Files.walk(Paths.get(pathGit + File.separator + "objects"))
                    .filter(Files::isRegularFile)
                    .map(f -> f.getFileName())
                    .map(f -> f.toString())
                    .filter(e -> (e.length() <= 32))
                    .findFirst()
                    .get();

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        CommandLine.run(new Git(), secondCommit);

        assertNotEquals("", firstCommit);
        String[] command4 = {"reset", firstCommitVal};
        CommandLine.run(new Git(), command4);
        CommandLine.run(new Git(), checkoutRev);
        String now = "";
        try {
            now = new String(Files.readAllBytes(testFileOne.toPath()));
        } catch (IOException e) {
            fail();
        }
        assertEquals("java <3 1", now);
    }


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
            deleter(path);
            //deleter(pathGit);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}