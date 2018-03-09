import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PTrieTest {
    @Test
    public void TestAdd() {
        PTrie trie = new PTrie();
        trie.add("hello");
        trie.add("world");
        trie.add("HeLL");
        trie.add("he");
        trie.add("her");
        trie.add("work");
        assertEquals(trie.size(), 6);
    }

    @Test
    public void TestAddWrongSymbol() {
        PTrie trie = new PTrie();
        assertFalse(trie.add("Hi!"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void TestSearchWrongSymbol() {
        PTrie trie = new PTrie();
        trie.add("Hi");
        assertFalse(trie.contains("Hi!"));
        assertEquals(trie.howManyStartsWithPrefix("Hi!"), 0);
    }

    @Test
    public void TestAddRemoveCheck() {
        PTrie trie = new PTrie();
        trie.add("Java");
        assertTrue(trie.contains("Java"));
        assertEquals(trie.size(), 1);
        trie.remove("Java");
        assertFalse(trie.contains("Java"));
        assertEquals(trie.size(), 0);
    }

    @Test
    public void TestAddRemovePrefixCheck() {
        PTrie trie = new PTrie();
        trie.add("Java");
        trie.add("Ja");
        assertTrue(trie.contains("Ja"));
        assertTrue(trie.contains("Java"));
        assertEquals(trie.size(), 2);
        trie.remove("Ja");
        assertFalse(trie.contains("Ja"));
        assertTrue(trie.contains("Java"));
        assertEquals(trie.size(), 1);
    }

    @Test
    public void TestAddRemovePrefixBCheck() {
        PTrie trie = new PTrie();
        trie.add("Java");
        trie.add("Ja");
        assertTrue(trie.contains("Ja"));
        assertTrue(trie.contains("Java"));
        assertEquals(trie.size(), 2);
        trie.remove("Java");
        assertFalse(trie.contains("Java"));
        assertTrue(trie.contains("Ja"));
        assertEquals(trie.size(), 1);
    }

    @Test
    public void TestStartsWith() {
        PTrie trie = new PTrie();
        trie.add("Hello");
        trie.add("Hell");
        trie.add("He");
        trie.add("Her");
        trie.add("Haha");
        assertEquals(trie.howManyStartsWithPrefix("Hell"), 2);
        assertEquals(trie.howManyStartsWithPrefix("He"), 4);
    }

    @Test
    public void TestAddWordNotPrefix() {
        PTrie trie = new PTrie();
        trie.add("Hell");
        trie.add("Hello");
        assertTrue(trie.contains("Hell"));
        assertTrue(trie.contains("Hello"));
        assertEquals(trie.howManyStartsWithPrefix("Hell"), 2);
        assertEquals(trie.size(), 2);
    }

    @Test
    public void TestRemoveWordNotPrefix() {
        PTrie trie = new PTrie();
        trie.add("Hell");
        trie.add("Hello");
        trie.add("Hellow");
        trie.remove("Hellow");
        assertTrue(trie.contains("Hell"));
        assertTrue(trie.contains("Hello"));
        assertFalse(trie.contains("Hellow"));
        assertEquals(trie.howManyStartsWithPrefix("Hell"), 2);
    }

    @Test
    public void TestDoubleAdd() {
        PTrie trie = new PTrie();
        trie.add("Hello");
        assertFalse(trie.add("Hello"));
        assertTrue(trie.contains("Hello"));
        assertEquals(trie.howManyStartsWithPrefix("Hel"), 1);
        assertEquals(trie.size(), 1);
    }

    @Test
    public void TestDoubleRemove() {
        PTrie trie = new PTrie();
        trie.add("Hello");
        assertTrue(trie.remove("Hello"));
        assertFalse(trie.remove("Hello"));
        assertFalse(trie.contains("Hello"));
        assertEquals(trie.howManyStartsWithPrefix("Hel"), 0);
        assertEquals(trie.size(), 0);
    }
}