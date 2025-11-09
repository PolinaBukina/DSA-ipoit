package by.it.a_khmelev.lesson10;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
import java.util.NoSuchElementException;

public class MyPriorityQueue<E> implements Queue<E> {

    private E[] heap;
    private int size;
    private final Comparator<? super E> comparator;

    @SuppressWarnings("unchecked")
    public MyPriorityQueue() {
        heap = (E[]) new Object[10];
        size = 0;
        comparator = null;
    }

    @SuppressWarnings("unchecked")
    public MyPriorityQueue(Comparator<? super E> comparator) {
        heap = (E[]) new Object[10];
        size = 0;
        this.comparator = comparator;
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
        for (int i = 0; i < size; i++) {
            sb.append(heap[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
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
        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    @Override
    public boolean add(E element) {
        return offer(element);
    }

    @Override
    public E remove() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return poll();
    }

    @Override
    public boolean contains(Object element) {
        for (int i = 0; i < size; i++) {
            if (element == null ? heap[i] == null : element.equals(heap[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean offer(E element) {
        if (element == null) {
            throw new NullPointerException();
        }

        if (size == heap.length) {
            resize();
        }

        heap[size] = element;
        siftUp(size);
        size++;
        return true;
    }

    @Override
    public E poll() {
        if (size == 0) {
            return null;
        }

        E result = heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null;
        size--;

        if (size > 0) {
            siftDown(0);
        }

        return result;
    }

    @Override
    public E peek() {
        return (size == 0) ? null : heap[0];
    }

    @Override
    public E element() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        return heap[0];
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
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
        if (c.isEmpty()) {
            return false;
        }

        for (E element : c) {
            add(element);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        // Простая реализация - создаем новую кучу без удаляемых элементов
        E[] newHeap = (E[]) new Object[heap.length];
        int newSize = 0;

        for (int i = 0; i < size; i++) {
            if (!c.contains(heap[i])) {
                newHeap[newSize++] = heap[i];
            } else {
                modified = true;
            }
        }

        // Перестраиваем кучу
        heap = newHeap;
        size = newSize;

        // Восстанавливаем свойства кучи
        for (int i = (size / 2) - 1; i >= 0; i--) {
            siftDown(i);
        }

        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        // Простая реализация - создаем новую кучу только с сохраняемыми элементами
        E[] newHeap = (E[]) new Object[heap.length];
        int newSize = 0;

        for (int i = 0; i < size; i++) {
            if (c.contains(heap[i])) {
                newHeap[newSize++] = heap[i];
            } else {
                modified = true;
            }
        }

        // Перестраиваем кучу
        heap = newHeap;
        size = newSize;

        // Восстанавливаем свойства кучи
        for (int i = (size / 2) - 1; i >= 0; i--) {
            siftDown(i);
        }

        return modified;
    }

    // Вспомогательные методы для работы с кучей

    @SuppressWarnings("unchecked")
    private void resize() {
        E[] newHeap = (E[]) new Object[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }

    private void siftUp(int index) {
        E element = heap[index];
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            E parent = heap[parentIndex];

            if (compare(element, parent) >= 0) {
                break;
            }

            heap[index] = parent;
            index = parentIndex;
        }
        heap[index] = element;
    }

    private void siftDown(int index) {
        E element = heap[index];
        int half = size / 2;

        while (index < half) {
            int childIndex = (index * 2) + 1;
            E child = heap[childIndex];
            int rightIndex = childIndex + 1;

            if (rightIndex < size && compare(heap[rightIndex], child) < 0) {
                childIndex = rightIndex;
                child = heap[rightIndex];
            }

            if (compare(element, child) <= 0) {
                break;
            }

            heap[index] = child;
            index = childIndex;
        }
        heap[index] = element;
    }

    @SuppressWarnings("unchecked")
    private int compare(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            return ((Comparable<? super E>) a).compareTo(b);
        }
    }

    /////////////////////////////////////////////////////////////////////////
    //////               Остальные методы - заглушки                ///////
    /////////////////////////////////////////////////////////////////////////

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        System.arraycopy(heap, 0, result, 0, size);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }
        System.arraycopy(heap, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
}