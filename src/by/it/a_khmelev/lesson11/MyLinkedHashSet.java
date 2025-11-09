package by.it.a_khmelev.lesson11;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;

public class MyLinkedHashSet<E> implements Set<E> {

    private static class Node<E> {
        E data;
        Node<E> next; // для цепочки в хеш-таблице
        Node<E> after; // для поддержания порядка добавления
        Node<E> before; // для поддержания порядка добавления

        Node(E data) {
            this.data = data;
        }
    }

    private Node<E>[] table;
    private Node<E> head; // первый добавленный элемент
    private Node<E> tail; // последний добавленный элемент
    private int size;
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    @SuppressWarnings("unchecked")
    public MyLinkedHashSet() {
        table = (Node<E>[]) new Node[DEFAULT_CAPACITY];
        head = null;
        tail = null;
        size = 0;
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Обязательные к реализации методы             ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        Node<E> current = head;
        int count = 0;

        while (current != null) {
            sb.append(current.data);
            count++;
            if (count < size) {
                sb.append(", ");
            }
            current = current.after;
        }

        sb.append("]");
        return sb.toString();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(E element) {
        if (element == null) {
            throw new NullPointerException();
        }

        if (size >= table.length * LOAD_FACTOR) {
            resize();
        }

        int index = getIndex(element);
        Node<E> current = table[index];

        // Проверяем, есть ли уже такой элемент
        while (current != null) {
            if (element.equals(current.data)) {
                return false; // Элемент уже существует
            }
            current = current.next;
        }

        // Создаем новый узел
        Node<E> newNode = new Node<>(element);

        // Добавляем в хеш-таблицу
        newNode.next = table[index];
        table[index] = newNode;

        // Добавляем в цепочку порядка добавления
        linkLast(newNode);

        size++;
        return true;
    }

    @Override
    public boolean remove(Object element) {
        if (element == null) {
            throw new NullPointerException();
        }

        int index = getIndex(element);
        Node<E> current = table[index];
        Node<E> prev = null;

        while (current != null) {
            if (element.equals(current.data)) {
                // Удаляем из хеш-таблицы
                if (prev == null) {
                    table[index] = current.next;
                } else {
                    prev.next = current.next;
                }

                // Удаляем из цепочки порядка добавления
                unlink(current);

                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }

        return false;
    }

    @Override
    public boolean contains(Object element) {
        if (element == null) {
            throw new NullPointerException();
        }

        int index = getIndex(element);
        Node<E> current = table[index];

        while (current != null) {
            if (element.equals(current.data)) {
                return true;
            }
            current = current.next;
        }

        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E element : c) {
            if (add(element)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object element : c) {
            if (remove(element)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;

        // Проходим по всем элементам в порядке добавления
        Node<E> current = head;
        while (current != null) {
            Node<E> next = current.after; // Сохраняем ссылку на следующий элемент перед возможным удалением
            if (!c.contains(current.data)) {
                // Удаляем элемент, которого нет в коллекции c
                remove(current.data);
                modified = true;
            }
            current = next;
        }

        return modified;
    }

    // Вспомогательные методы

    private int getIndex(Object element) {
        return Math.abs(element.hashCode()) % table.length;
    }

    // Добавляет узел в конец цепочки порядка добавления
    private void linkLast(Node<E> node) {
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.after = node;
            node.before = tail;
            tail = node;
        }
    }

    // Удаляет узел из цепочки порядка добавления
    private void unlink(Node<E> node) {
        Node<E> before = node.before;
        Node<E> after = node.after;

        if (before == null) {
            head = after;
        } else {
            before.after = after;
            node.before = null;
        }

        if (after == null) {
            tail = before;
        } else {
            after.before = before;
            node.after = null;
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<E>[] oldTable = table;
        table = (Node<E>[]) new Node[oldTable.length * 2];

        // Сохраняем порядок добавления
        Node<E> current = head;
        head = null;
        tail = null;
        size = 0;

        // Перехешируем все элементы
        while (current != null) {
            add(current.data);
            current = current.after;
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Остальные методы - заглушки                ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
}