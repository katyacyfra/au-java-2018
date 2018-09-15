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
        String[] command1 = {"init"};
        String[] command2 = {"commit", "-m", "first commit", "./examples/readme"};
        String[] command3 = {"commit", "-m", "second commit", "./examples/readme"};
        String[] command5 = {"checkout", "master"};


        CommandLine.run(new Git(), command1);
        CommandLine.run(new Git(), command2);
        try (PrintWriter writer = new PrintWriter(testFileOne)) {
            writer.print("java <3 2");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
        }
        String firstCommit = "";
        try {
            firstCommit = Files.walk(Paths.get(pathGit + File.separator + "objects"))
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

        CommandLine.run(new Git(), command3);

        assertNotEquals("", firstCommit);
        String[] command4 = {"reset", firstCommit};
        CommandLine.run(new Git(), command4);
        CommandLine.run(new Git(), command5);
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