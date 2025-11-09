package by.it.a_khmelev.lesson12;

import java.util.*;

public class MySplayMap implements NavigableMap<Integer, String> {

    private static class SplayNode {
        Integer key;
        String value;
        SplayNode left;
        SplayNode right;
        SplayNode parent;

        SplayNode(Integer key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    private SplayNode root;
    private int size;

    public MySplayMap() {
        root = null;
        size = 0;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (size == 0) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        inOrderToString(root, sb);
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2); // Удаляем последнюю запятую с пробелом
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String put(Integer key, String value) {
        if (key == null) {
            throw new NullPointerException();
        }

        String[] oldValue = new String[1];
        root = put(root, key, value, oldValue);
        root = splay(root, key);
        return oldValue[0];
    }

    @Override
    public String remove(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        if (!containsKey(key)) {
            return null;
        }

        root = splay(root, (Integer) key);
        String removedValue = root.value;

        if (root.left == null) {
            root = root.right;
        } else {
            SplayNode newRoot = root.right;
            newRoot = splay(newRoot, (Integer) key);
            if (newRoot != null) {
                newRoot.left = root.left;
            }
            root = newRoot;
        }

        size--;
        return removedValue;
    }

    @Override
    public String get(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        root = splay(root, (Integer) key);
        return (root != null && root.key.equals(key)) ? root.value : null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }

        root = splay(root, (Integer) key);
        return root != null && root.key.equals(key);
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return containsValue(root, value.toString());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Integer firstKey() {
        if (root == null) {
            throw new java.util.NoSuchElementException();
        }
        SplayNode minNode = findMin(root);
        root = splay(root, minNode.key);
        return minNode.key;
    }

    @Override
    public Integer lastKey() {
        if (root == null) {
            throw new java.util.NoSuchElementException();
        }
        SplayNode maxNode = findMax(root);
        root = splay(root, maxNode.key);
        return maxNode.key;
    }

    @Override
    public Integer lowerKey(Integer key) {
        SplayNode node = lowerNode(root, key);
        if (node != null) {
            root = splay(root, node.key);
            return node.key;
        }
        return null;
    }

    @Override
    public Integer floorKey(Integer key) {
        SplayNode node = floorNode(root, key);
        if (node != null) {
            root = splay(root, node.key);
            return node.key;
        }
        return null;
    }

    @Override
    public Integer ceilingKey(Integer key) {
        SplayNode node = ceilingNode(root, key);
        if (node != null) {
            root = splay(root, node.key);
            return node.key;
        }
        return null;
    }

    @Override
    public Integer higherKey(Integer key) {
        SplayNode node = higherNode(root, key);
        if (node != null) {
            root = splay(root, node.key);
            return node.key;
        }
        return null;
    }

    @Override
    public NavigableMap<Integer, String> headMap(Integer toKey) {
        return headMap(toKey, false);
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey) {
        return tailMap(fromKey, true);
    }

    @Override
    public NavigableMap<Integer, String> headMap(Integer toKey, boolean inclusive) {
        MySplayMap headMap = new MySplayMap();
        headMap(root, toKey, inclusive, headMap);
        return headMap;
    }

    @Override
    public NavigableMap<Integer, String> tailMap(Integer fromKey, boolean inclusive) {
        MySplayMap tailMap = new MySplayMap();
        tailMap(root, fromKey, inclusive, tailMap);
        return tailMap;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Вспомогательные методы Splay-дерева        ///////
    /////////////////////////////////////////////////////////////////////////

    private void inOrderToString(SplayNode node, StringBuilder sb) {
        if (node != null) {
            inOrderToString(node.left, sb);
            sb.append(node.key).append("=").append(node.value).append(", ");
            inOrderToString(node.right, sb);
        }
    }

    private SplayNode put(SplayNode node, Integer key, String value, String[] oldValue) {
        if (node == null) {
            size++;
            return new SplayNode(key, value);
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value, oldValue);
            if (node.left != null) node.left.parent = node;
        } else if (cmp > 0) {
            node.right = put(node.right, key, value, oldValue);
            if (node.right != null) node.right.parent = node;
        } else {
            oldValue[0] = node.value;
            node.value = value;
        }

        return node;
    }

    private boolean containsValue(SplayNode node, String value) {
        if (node == null) {
            return false;
        }
        if (value.equals(node.value)) {
            return true;
        }
        return containsValue(node.left, value) || containsValue(node.right, value);
    }

    private SplayNode findMin(SplayNode node) {
        while (node != null && node.left != null) {
            node = node.left;
        }
        return node;
    }

    private SplayNode findMax(SplayNode node) {
        while (node != null && node.right != null) {
            node = node.right;
        }
        return node;
    }

    // Splay операции
    private SplayNode splay(SplayNode node, Integer key) {
        if (node == null) {
            return null;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            if (node.left == null) {
                return node;
            }
            int cmp2 = key.compareTo(node.left.key);
            if (cmp2 < 0) {
                node.left.left = splay(node.left.left, key);
                node = rotateRight(node);
            } else if (cmp2 > 0) {
                node.left.right = splay(node.left.right, key);
                if (node.left.right != null) {
                    node.left = rotateLeft(node.left);
                }
            }
            return (node.left == null) ? node : rotateRight(node);
        } else if (cmp > 0) {
            if (node.right == null) {
                return node;
            }
            int cmp2 = key.compareTo(node.right.key);
            if (cmp2 < 0) {
                node.right.left = splay(node.right.left, key);
                if (node.right.left != null) {
                    node.right = rotateRight(node.right);
                }
            } else if (cmp2 > 0) {
                node.right.right = splay(node.right.right, key);
                node = rotateLeft(node);
            }
            return (node.right == null) ? node : rotateLeft(node);
        } else {
            return node;
        }
    }

    private SplayNode rotateRight(SplayNode h) {
        SplayNode x = h.left;
        h.left = x.right;
        if (x.right != null) x.right.parent = h;
        x.right = h;
        x.parent = h.parent;
        h.parent = x;
        return x;
    }

    private SplayNode rotateLeft(SplayNode h) {
        SplayNode x = h.right;
        h.right = x.left;
        if (x.left != null) x.left.parent = h;
        x.left = h;
        x.parent = h.parent;
        h.parent = x;
        return x;
    }

    // Методы для навигации
    private SplayNode lowerNode(SplayNode node, Integer key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp <= 0) {
            return lowerNode(node.left, key);
        } else {
            SplayNode rightResult = lowerNode(node.right, key);
            return (rightResult != null) ? rightResult : node;
        }
    }

    private SplayNode floorNode(SplayNode node, Integer key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node;
        } else if (cmp < 0) {
            return floorNode(node.left, key);
        } else {
            SplayNode rightResult = floorNode(node.right, key);
            return (rightResult != null) ? rightResult : node;
        }
    }

    private SplayNode ceilingNode(SplayNode node, Integer key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            return node;
        } else if (cmp > 0) {
            return ceilingNode(node.right, key);
        } else {
            SplayNode leftResult = ceilingNode(node.left, key);
            return (leftResult != null) ? leftResult : node;
        }
    }

    private SplayNode higherNode(SplayNode node, Integer key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp >= 0) {
            return higherNode(node.right, key);
        } else {
            SplayNode leftResult = higherNode(node.left, key);
            return (leftResult != null) ? leftResult : node;
        }
    }

    private void headMap(SplayNode node, Integer toKey, boolean inclusive, MySplayMap result) {
        if (node == null) return;

        headMap(node.left, toKey, inclusive, result);

        int cmp = node.key.compareTo(toKey);
        if (cmp < 0 || (inclusive && cmp == 0)) {
            result.put(node.key, node.value);
        }

        if (cmp < 0) {
            headMap(node.right, toKey, inclusive, result);
        }
    }

    private void tailMap(SplayNode node, Integer fromKey, boolean inclusive, MySplayMap result) {
        if (node == null) return;

        tailMap(node.right, fromKey, inclusive, result);

        int cmp = node.key.compareTo(fromKey);
        if (cmp > 0 || (inclusive && cmp == 0)) {
            result.put(node.key, node.value);
        }

        if (cmp > 0) {
            tailMap(node.left, fromKey, inclusive, result);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Остальные методы - заглушки                ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Entry<Integer, String> lowerEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> floorEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> ceilingEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> higherEntry(Integer key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> firstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> lastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> pollFirstEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entry<Integer, String> pollLastEntry() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<Integer, String> descendingMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<Integer> navigableKeySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<Integer> descendingKeySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<Integer, String> subMap(Integer fromKey, Integer toKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableMap<Integer, String> subMap(Integer fromKey, boolean fromInclusive, Integer toKey, boolean toInclusive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<Integer, String>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comparator<? super Integer> comparator() {
        return null;
    }
}