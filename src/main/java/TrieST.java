import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A 256-way trie that represents a symbol table of key - value pairs, where keys are strings.
 * It provides put(String key, Value val), get(String key), contains(String key), delete(String key),
 * keysWithPrefix(String prefix), and keysMatch(String key) methods. The keysWithPrefix
 * method returns all strings in the table that start with the given prefix. The keysMatch method returns
 * all strings in teh symbol table that match a given pattern.
 */
public class TrieST<Value> {
    private static final int R = 256;            //extended ASCII
    private TrieNode root;

    /**
     * 256-way trie node class.
     */
    private static class TrieNode<Value> {
        private List<Value> values = new ArrayList<>();
        private TrieNode[] children = new TrieNode[R];
        private String name;
    }

    /**
     * Constructs an empty string symbol table.
     */
    public TrieST() {
    }

    /**
     * Inserts the key - value pair into the symbol table. If multiple values share
     * the same location name, put them into a list.
     * @param key
     * @param value
     */
    public void put(String key, String name, Value value) {
        if (key == null) {
            throw new IllegalArgumentException("The first argument is null.");
        }
        root = put(root, key, name, value, 0);
    }

    private TrieNode put(TrieNode node, String key, String name, Value value, int d) {
        if (node == null) {
            node = new TrieNode();
        }
        if (d == key.length()) {
            node.name = name;
            node.values.add(value);
            return node;
        }

        char c = key.charAt(d);
        node.children[c] = put(node.children[c], key, name, value, d + 1);
        return node;
    }

    /**
     * Returns a list of nodes with the given key if there is any node associated with the given key, otherwise null.
     * @param key
     * @return a list of nodes or null
     */
    public List<Value> get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("The argument is null.");
        }
        TrieNode target = get(root, key, 0);
        if (target == null || target.name == null) {
            return null;
        } else {
            return target.values;
        }
    }

    private TrieNode get(TrieNode node, String key, int d) {
        if (node == null) {
            return null;
        }
        if (d == key.length()) {
            return node;
        }
        char c = key.charAt(d);
        return get(node.children[c], key, d + 1);
    }

    /**
     * Returns true if there is node or name associate with the key, otherwise false
     * @param key
     * @return
     */
    public boolean contains(String key) {
        if (key == null) {
            throw new IllegalArgumentException("The argument is null.");
        }
        return get(key) != null;
    }

    /**
     * Returns all of the valid TrieNode names in the symbol table that start with the given prefix.
     * @param prefix
     * @return an iterable list of keys (location names)
     */
    public List<String> keysWithPrefix(String prefix) {
        List<String> res = new ArrayList<>();
        TrieNode prefixNode = get(root, prefix, 0);
        collectKeysWithPrefix(prefixNode, new StringBuilder(prefix), res);
        return res;
    }

    private void collectKeysWithPrefix(TrieNode node, StringBuilder prefix, List<String> res) {
        if (node == null) {
            return;
        }
        if (node.name != null) {
            res.add(node.name);
        }
        for (char c = 0; c < R; c++) {
            prefix.append(c);
            collectKeysWithPrefix(node.children[c], prefix, res);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /**
     * Returns all of the keys in the symbol table that match pattern, where . symbol is treated as a wildcard
     * character.
     * @param pattern
     * @return all of the keys in the symbol table that match the pattern.
     */
    public List<String> keysMatch(String pattern) {
        List<String> res = new ArrayList<>();
        collectKeysMatch(root, new StringBuilder(), pattern, res);
        return res;
    }

    private void collectKeysMatch(TrieNode node, StringBuilder prefix, String pattern, List<String> res) {
        if (node == null) {
            return;
        }
        int d = prefix.length();
        if (d == pattern.length() && node.name != null) {
            res.add(node.name);
        }

        if (d == pattern.length()) {
            return;
        }

        char c = pattern.charAt(d);
        if (c == '.') {
            for (char ch = 0; ch < R; ch++) {
                prefix.append(ch);
                collectKeysMatch(node.children[ch], prefix, pattern, res);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        } else {
            prefix.append(c);
            collectKeysMatch(node.children[c], prefix, pattern, res);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    public static void main(String[] args) {
        TrieST trie = new TrieST();
        trie.put("a", "A", 1);
        trie.put("banana", "Banana", 2);
        trie.put("cat", "Cat", 3);
        trie.put("dog", "Dog", 4);
        trie.put("dog", "Dog", 5);
        trie.put("door", "Door", 6);
        trie.put("dig", "Dig", 7);
        System.out.println(trie.contains("dog"));
        System.out.println(trie.contains("banan"));
        List<Long> res = trie.get("dog");
        System.out.println(Arrays.toString(res.toArray()));
        List<String> res1 = trie.keysWithPrefix("d");
        System.out.println(Arrays.toString(res1.toArray()));
    }
}
