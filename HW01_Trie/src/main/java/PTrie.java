public class PTrie implements Trie {
    static final int LETTERS = 52;

    static class Vertex {
        Vertex[] next = new Vertex[LETTERS];
        boolean isTerminal;
        int howManyWords;
        int howManyWordsEnds;

        Vertex() {
            int i;
            for (i = 0; i < LETTERS; i++) {
                next[i] = null;
            }
            isTerminal = false;
            howManyWords = 0;
        }
    }

    public Vertex root;

    PTrie() {
        root = new Vertex();
    }

    public static int get_index(char letter) {
        if (Character.isUpperCase(letter)) {
            return LETTERS / 2 + letter - 'A';
        } else {
            return letter - 'a';
        }
    }

    public static boolean checkWrongSymbol(String element) {
        int len = element.length();
        int i;
        int index;
        for (i = 0; i < len; i++) {
            index = get_index(element.charAt(i));
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
            return false;
        }
        int len = element.length();
        Vertex current = root;
        int i;
        int index;
        current.howManyWords++;
        for (i = 0; i < len; i++) {
            index = get_index(element.charAt(i));
            if (current.next[index] == null) { //no such prefix, create one
                current.next[index] = new Vertex();
            }
            current = current.next[index];
            current.howManyWords++;
        }
        current.isTerminal = true;
        current.howManyWordsEnds++;
        return true;
    }


    public boolean contains(String element) {
        if (checkWrongSymbol(element)) {
            return false;
        }
        int len = element.length();
        Vertex current = root;
        int i;
        int index;
        for (i = 0; i < len; i++) {
            index = get_index(element.charAt(i));
            if (current.next[index] == null) {
                return false;
            }
            current = current.next[index];
        }
        if (current != null && current.isTerminal) {
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(String element) {
        if (contains(element)) {
            int len = element.length();
            Vertex current = root;
            Vertex next;
            int i;
            int index;
            root.howManyWords--;
            for (i = 0; i < len; i++) {
                index = get_index(element.charAt(i));
                next = current.next[index];
                next.howManyWords--;
                if (next.howManyWords == 0) {
                    current.next[index] = null;
                }
                current = next;
            }
            current.howManyWordsEnds--;
            if (current.isTerminal == true && current.howManyWordsEnds == 0) {
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
        int len = prefix.length();
        Vertex current = root;
        int i;
        int index;
        try {
            for (i = 0; i < len; i++) {
                index = get_index(prefix.charAt(i));
                if (current.next[index] == null) {
                    return 0;
                }
                current = current.next[index];
            }
            return current.howManyWords;
        } catch (IndexOutOfBoundsException e) { //unexpected char
            return 0;
        }
    }

    public static void main(String[] args) {
        PTrie trie = new PTrie();
    }
}
