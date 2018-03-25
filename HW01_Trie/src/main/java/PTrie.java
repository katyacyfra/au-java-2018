public class PTrie implements Trie {
    private final int LETTERS = 52;

    private class Vertex {
        final Vertex[] next = new Vertex[LETTERS];
        boolean isTerminal;
        int howManyWords;
    }

    private final Vertex root;

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

    public static void main(String[] args) {
        PTrie trie = new PTrie();
    }
}
