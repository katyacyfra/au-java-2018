import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class SerializeTrieTest {
    @Test
    public void testSerializeDeserialize() throws IOException {
        PTrie trie = new PTrie();
        assertTrue(trie.add("hello"));
        assertTrue(trie.add("world"));
        assertTrue(trie.add("HeLL"));
        assertTrue(trie.add("he"));
        assertTrue(trie.add("her"));
        assertTrue(trie.add("work"));
        assertEquals(6, trie.size());
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        trie.serialize(bs);
        PTrie newTrie = new PTrie();
        ByteArrayInputStream bi = new ByteArrayInputStream(bs.toByteArray());
        newTrie.deserialize(bi);
        assertEquals(6, newTrie.size());
        assertTrue(newTrie.contains("world"));
        assertTrue(newTrie.contains("HeLL"));
        assertTrue(newTrie.contains("he"));
        assertTrue(newTrie.contains("her"));
        assertTrue(newTrie.contains("work"));
        assertFalse(newTrie.contains("h"));
        bs.close();
        bi.close();
    }

    @Test
    public void testEmptySerializeDeserialize() throws IOException {
        PTrie trie = new PTrie();
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        trie.serialize(bs);
        PTrie newTrie = new PTrie();
        ByteArrayInputStream bi = new ByteArrayInputStream(bs.toByteArray());
        newTrie.deserialize(bi);
        assertEquals(0, newTrie.size());
        bs.close();
        bi.close();
    }
}
