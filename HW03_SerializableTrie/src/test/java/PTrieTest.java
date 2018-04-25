import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PTrieTest {
    @Test
    public void testAdd() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("hello"));
        assertTrue(trie.add("world"));
        assertTrue(trie.add("HeLL"));
        assertTrue(trie.add("he"));
        assertTrue(trie.add("her"));
        assertTrue(trie.add("work"));
        assertEquals(6, trie.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWrongSymbol() {
        PTrie trie = new PTrie();
        trie.add("Hi!");
    }

    @Test
    public void testSearchWrongSymbol() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Hi"));
        assertFalse(trie.contains("Hi!"));
    }

    @Test
    public void testAddRemoveCheck() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Java"));
        assertTrue(trie.contains("Java"));
        assertEquals(1, trie.size());
        assertTrue(trie.remove("Java"));
        assertFalse(trie.contains("Java"));
        assertEquals(0, trie.size());
    }

    @Test
    public void testAddRemovePrefixCheck() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Java"));
        assertTrue(trie.add("Ja"));
        assertTrue(trie.contains("Ja"));
        assertTrue(trie.contains("Java"));
        assertEquals(2, trie.size());
        assertTrue(trie.remove("Ja"));
        assertFalse(trie.contains("Ja"));
        assertTrue(trie.contains("Java"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testAddRemovePrefixBCheck() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Java"));
        assertTrue(trie.add("Ja"));
        assertTrue(trie.contains("Ja"));
        assertTrue(trie.contains("Java"));
        assertEquals(2, trie.size());
        assertTrue(trie.remove("Java"));
        assertFalse(trie.contains("Java"));
        assertTrue(trie.contains("Ja"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testStartsWith() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Hello"));
        assertTrue(trie.add("Hell"));
        assertTrue(trie.add("He"));
        assertTrue(trie.add("Her"));
        assertTrue(trie.add("Haha"));
        assertEquals(2, trie.howManyStartsWithPrefix("Hell"));
        assertEquals(4, trie.howManyStartsWithPrefix("He"));
    }

    @Test
    public void testAddWordNotPrefix() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Hell"));
        assertTrue(trie.add("Hello"));
        assertTrue(trie.contains("Hell"));
        assertTrue(trie.contains("Hello"));
        assertEquals(2, trie.howManyStartsWithPrefix("Hell"));
        assertEquals(2, trie.size());
    }

    @Test
    public void testRemoveWordNotPrefix() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Hell"));
        assertTrue(trie.add("Hello"));
        assertTrue(trie.add("Hellow"));
        assertTrue(trie.remove("Hellow"));
        assertTrue(trie.contains("Hell"));
        assertTrue(trie.contains("Hello"));
        assertFalse(trie.contains("Hellow"));
        assertEquals(2, trie.howManyStartsWithPrefix("Hell"));
    }

    @Test
    public void testDoubleAdd() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Hello"));
        assertFalse(trie.add("Hello"));
        assertTrue(trie.contains("Hello"));
        assertEquals(1, trie.howManyStartsWithPrefix("Hel"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testDoubleRemove() {
        PTrie trie = new PTrie();
        assertTrue(trie.add("Hello"));
        assertTrue(trie.remove("Hello"));
        assertFalse(trie.remove("Hello"));
        assertFalse(trie.contains("Hello"));
        assertEquals(0, trie.howManyStartsWithPrefix("Hel"));
        assertEquals(0, trie.size());
    }
}