import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class DictionaryTest {
    @Test
    public void testPut() {
        DictionaryImpl dict = new DictionaryImpl();
        assertEquals(null, dict.put("Java", "A"));
        assertEquals(1, dict.size());
        assertEquals(null, dict.put("Python", "Test"));
        assertEquals(2, dict.size());
        assertEquals("A", dict.put("Java", "B"));
        assertEquals(2, dict.size());
    }

    @Test
    public void testPutAndRehash() {
        DictionaryImpl dict = new DictionaryImpl();
        int capacity = dict.getCapacity();
        assertEquals(null, dict.put("Java", "Java"));
        assertEquals(null, dict.put("Python", "Test"));
        assertEquals(null, dict.put("Prolog", "Test"));
        assertEquals(null, dict.put("Haskell", "Monad"));
        assertEquals(null, dict.put("ALGOL", "SAP"));
        assertEquals(null, dict.put("PHP", "Sucks"));
        assertEquals("Java", dict.put("Java", "Love"));
        assertEquals(null, dict.put("F", "Java"));
        assertEquals(null, dict.put("Coq", "Koune"));
        assertTrue((double) dict.size() / capacity > 0.75);
        assertEquals(capacity * 2, dict.getCapacity());
    }

    @Test
    public void testContains() {
        DictionaryImpl dict = new DictionaryImpl();
        assertEquals(null, dict.put("Java", "Java"));
        assertEquals(null, dict.put("Python", "Test"));
        assertTrue(dict.contains("Python"));
        assertFalse(dict.contains("Haskell"));
    }

    @Test
    public void testGet() {
        DictionaryImpl dict = new DictionaryImpl();
        assertEquals(null, dict.put("Java", "Just"));
        assertEquals(null, dict.put("Python", "Test"));
        assertEquals("Test", dict.put("Python", "B"));
        assertEquals("Just", dict.get("Java"));
        assertEquals("B", dict.get("Python"));
        assertEquals(null, dict.get("Haskell"));
    }

    @Test
    public void testRemove() {
        DictionaryImpl dict = new DictionaryImpl();
        assertEquals(null, dict.put("Java", "Just"));
        assertEquals(null, dict.put("Python", "Test"));
        assertEquals(2, dict.size());
        assertEquals("Just", dict.remove("Java"));
        assertFalse(dict.contains("Java"));
        assertEquals(null, dict.remove("Haskell"));
        assertEquals(1, dict.size());
    }

    @Test
    public void testRemoveAndRehash() {
        DictionaryImpl dict = new DictionaryImpl();
        int capacity = dict.getCapacity();
        assertEquals(null, dict.put("Java", "Java"));
        assertEquals(null, dict.put("Python", "Test"));
        assertEquals(null, dict.put("Prolog", "Test"));
        assertEquals("Java", dict.remove("Java"));
        assertEquals("Test", dict.remove("Python"));
        assertEquals("Test", dict.remove("Prolog"));
        assertTrue((double) dict.size() / capacity < 0.1);
        assertEquals(capacity / 2, dict.getCapacity());
    }

    @Test
    public void testClear() {
        DictionaryImpl dict = new DictionaryImpl();
        assertEquals(null, dict.put("Java", "Just"));
        assertEquals(null, dict.put("Python", "Test"));
        assertEquals(2, dict.size());
        dict.clear();
        assertEquals(0, dict.size());
        assertFalse(dict.contains("Java"));
        assertFalse(dict.contains("Python"));
    }

}
