import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class StringsMergeTest {
    @Test
    public void mergeDifferent() {
        List<String> a = Arrays.asList("test", "lalala", "hello");
        List<String> b = Arrays.asList("lalala");
        List<String> c = Arrays.asList("test");

        List<String> bc = FileMerger.mergeLists(b, c);
        List<String> ab = FileMerger.mergeLists(b, a);
        assertEquals("[" +
                "<<<<<<< HEAD, " +
                "lalala, =======, " +
                "test, " +
                ">>>>>>> new branch to commit" +
                "]", bc.toString());
        assertEquals("[" +
                        "<<<<<<< HEAD" +
                        ", lalala," +
                        " =======," +
                        " test, lalala, hello, " +
                        ">>>>>>> new branch to commit" +
                        "]",
                ab.toString());
    }



    @Test
    public void mergeStringsComplex() {
        List<String> a = Arrays.asList("test just", "test", "try it again", "again");
        List<String> b = Arrays.asList("all about", "test", "again", "once again");
        List<String> ab = FileMerger.mergeLists(a, b);
        assertEquals("[" +
                "<<<<<<< HEAD, " +
                "test just," +
                " =======," +
                " all about, " +
                ">>>>>>> new branch to commit, " +
                "test, " +
                "<<<<<<< HEAD, " +
                "try it again, " +
                "=======, " +
                ">>>>>>> new branch to commit, " +
                "again, " +
                "<<<<<<< HEAD, " +
                "=======, " +
                "once again, " +
                ">>>>>>> new branch to commit]",
                ab.toString());
    }
}
