import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;

public class PTrie implements Trie, StreamSerializable {
    private final int LETTERS = 52;

    private class Vertex {
        Vertex[] next = new Vertex[LETTERS];
        boolean isTerminal;
        int howManyWords;
        int counter;
    }

    private Vertex root;

    PTrie() {
        root = new Vertex();
    }

    private int getIndex(char letter) {
        if (Character.isUpperCase(letter)) {
            return LETTERS / 2 + letter - 'A';
        } else {
            return letter - 'a';
        }
    }

    private boolean checkWrongSymbol(String element) {
        for (int i = 0; i < element.length(); i++) {
            int index = getIndex(element.charAt(i));
            if (index < 0 || index >= LETTERS) {
                return true;
            }
        }
        return false;
    }

    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }
        if (checkWrongSymbol(element)) {
            throw new IllegalArgumentException();
        }
        Vertex current = root;
        current.howManyWords++;
        for (int i = 0; i < element.length(); i++) {
            int index = getIndex(element.charAt(i));
            if (current.next[index] == null) { //no such prefix, create one
                current.next[index] = new Vertex();
            }
            current = current.next[index];
            current.howManyWords++;
        }
        current.isTerminal = true;
        return true;
    }

    public boolean contains(String element) {
        if (checkWrongSymbol(element)) {
            return false;
        }
        Vertex current = root;
        for (int i = 0; i < element.length(); i++) {
            int index = getIndex(element.charAt(i));
            if (current.next[index] == null) {
                return false;
            }
            current = current.next[index];
        }
        return current != null && current.isTerminal;
    }

    public boolean remove(String element) {
        if (contains(element)) {
            Vertex current = root;
            Vertex next;
            root.howManyWords--;
            for (int i = 0; i < element.length(); i++) {
                int index = getIndex(element.charAt(i));
                next = current.next[index];
                next.howManyWords--;
                if (next.howManyWords == 0) {
                    current.next[index] = null;
                    index = getIndex(element.charAt(element.length() - 1));
                }
                current = next;
            }
            if (current.isTerminal) {
                current.isTerminal = false;
            }
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return root.howManyWords;
    }

    public int howManyStartsWithPrefix(String prefix) {
        if (checkWrongSymbol(prefix)) {
            return 0;
        }
        Vertex current = root;
        for (int i = 0; i < prefix.length(); i++) {
            int index = getIndex(prefix.charAt(i));
            if (current.next[index] == null) {
                return 0;
            }
            current = current.next[index];
        }
        return current.howManyWords;
    }

    public void serialize(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeChar('$');
        serializeVertex(root, dos);
        dos.writeChar('#');
    }

    /**
     * Replace current state with data from input stream
     */
    public void deserialize(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        DeserializedVertex newRoot = null;
        DeserializedVertex currentParent = null;
        DeserializedVertex current = null;
        while (true) {
            char symb = dis.readChar();
            if (symb == '$') {
                newRoot = addVertex(null, dis);
                current = newRoot;
                currentParent = current;
            } else if (symb == '(') {
                currentParent = current;
                current = addVertex(currentParent, dis);

            } else if (symb == '|') {
                current = addVertex(currentParent, dis);
            } else if (symb == ')') {
                if (dis.readChar() == '#') {
                    break;
                }
                currentParent = currentParent.parent;
                current = addVertex(currentParent, dis);
            } else {
                throw new IOException();
            }
        }
        root = newRoot;
    }

    private void serializeVertex(Vertex v, DataOutputStream out) throws IOException {
        if (v != null) {
            out.writeInt(v.howManyWords);
            out.writeBoolean(v.isTerminal);
            out.writeChar('(');
            for (int i = 0; i < LETTERS; i++) {
                serializeVertex(v.next[i], out);
                if (i != LETTERS - 1) {
                    out.writeChar('|');
                }
            }
            out.writeChar(')');
        } else {
            out.writeInt(0);
            out.writeBoolean(false);
        }
    }

    private class DeserializedVertex extends Vertex {
        int counter;
        DeserializedVertex parent;
    }

    private DeserializedVertex addVertex(DeserializedVertex currentParent, DataInputStream in) throws IOException {
        DeserializedVertex el = new DeserializedVertex();
        el.howManyWords = in.readInt();
        el.isTerminal = in.readBoolean();
        el.parent = currentParent;
        if (el.howManyWords == 0 && currentParent != null) {
            el = null;
        } else {
            el.next = new Vertex[LETTERS];
        }
        if (currentParent != null) {
            currentParent.next[currentParent.counter] = el;
            currentParent.counter++;
        }
        return el;
    }

    public static void main(String[] args) {
        PTrie trie = new PTrie();
    }
}
