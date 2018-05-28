import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecondPartTasksTest {

    @Test
    public void testFindQuotes() {
        File file1 = new File("src/main/resources/testFindQuotes.txt");
        File file2 = new File("src/main/resources/testFindQuotes2.txt");
        List<String> paths = Arrays.asList(file1.getAbsolutePath(), file2.getAbsolutePath());
        List<String> expected = Arrays.asList("Hey, do you like Java?", "Java forever");
        assertEquals(expected, SecondPartTasks.findQuotes(paths, "Java"));
    }

    @Test
    public void testPiDividedBy4() {
        assertTrue(SecondPartTasks.piDividedBy4() < 1);
        assertEquals(Math.PI / 4, SecondPartTasks.piDividedBy4(), 0.1);
    }

    @Test
    public void testFindPrinter() {
        Map<String, List<String>> authors = new HashMap<>();
        authors.put("Donald Knuth", Arrays.asList("KMP algorithm", "The Art Of Programming"));
        authors.put("Vasya Pupkin", Arrays.asList("long long long long the longest name you have ever seen",
                "Java 8"));
        authors.put("Stephen King", Arrays.asList("Carry", "It", "Shining", "Dark Tower"));
        assertEquals("Vasya Pupkin", SecondPartTasks.findPrinter(authors));
    }

    @Test
    public void testCalculateGlobalOrder() {
        Map<String, Integer> first = new HashMap<>();
        Map<String, Integer> second = new HashMap<>();
        Map<String, Integer> third = new HashMap<>();

        first.put("iPhone", 1);
        first.put("Nokia 3310", 100);
        first.put("Sony Xperia", 3);
        second.put("iPhone", 5);
        second.put("Nokia 3310", 1);
        third.put("iPhone", 1);
        third.put("Nokia 3310", 10);

        List<Map<String, Integer>> input = Arrays.asList(first, second, third);
        Map<String, Integer> res = SecondPartTasks.calculateGlobalOrder(input);
        Integer i = 7;
        Integer n = 111;
        Integer sx = 3;
        assertEquals(i, res.get("iPhone"));
        assertEquals(n, res.get("Nokia 3310"));
        assertEquals(sx, res.get("Sony Xperia"));
    }

}