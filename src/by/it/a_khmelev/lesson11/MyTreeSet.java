package by.it.a_khmelev.lesson11;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;

public class MyTreeSet<E> implements Set<E> {

    private E[] elements;
    private int size;
    private final Comparator<? super E> comparator;

    @SuppressWarnings("unchecked")
    public MyTreeSet() {
        elements = (E[]) new Object[10];
        size = 0;
        comparator = null;
    }

    @SuppressWarnings("unchecked")
    public MyTreeSet(Comparator<? super E> comparator) {
        elements = (E[]) new Object[10];
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
            sb.append(elements[i]);
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
            elements[i] = null;
        }
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

        // Поиск позиции для вставки
        int index = binarySearch(element);

        // Если элемент уже существует
        if (index >= 0) {
            return false;
        }

        // Вычисляем реальную позицию для вставки
        int insertIndex = -index - 1;

        // Увеличиваем массив если нужно
        if (size == elements.length) {
            resize();
        }

        // Сдвигаем элементы вправо
        for (int i = size; i > insertIndex; i--) {
            elements[i] = elements[i - 1];
        }

        // Вставляем новый элемент
        elements[insertIndex] = element;
        size++;
        return true;
    }

    @Override
    public boolean remove(Object element) {
        if (element == null) {
            throw new NullPointerException();
        }

        int index = binarySearch(element);
        if (index < 0) {
            return false; // Элемент не найден
        }

        // Сдвигаем элементы влево
        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }

        elements[size - 1] = null;
        size--;
        return true;
    }

    @Override
    public boolean contains(Object element) {
        if (element == null) {
            throw new NullPointerException();
        }

        return binarySearch(element) >= 0;
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

        // Создаем новый массив только с элементами, которые не нужно удалять
        @SuppressWarnings("unchecked")
        E[] newElements = (E[]) new Object[elements.length];
        int newSize = 0;

        for (int i = 0; i < size; i++) {
            if (!c.contains(elements[i])) {
                newElements[newSize++] = elements[i];
            } else {
                modified = true;
            }
        }

        elements = newElements;
        size = newSize;
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;

        // Создаем новый массив только с элементами, которые нужно сохранить
        @SuppressWarnings("unchecked")
        E[] newElements = (E[]) new Object[elements.length];
        int newSize = 0;

        for (int i = 0; i < size; i++) {
            if (c.contains(elements[i])) {
                newElements[newSize++] = elements[i];
            } else {
                modified = true;
            }
        }

        elements = newElements;
        size = newSize;
        return modified;
    }

    // Вспомогательные методы

    @SuppressWarnings("unchecked")
    private int binarySearch(Object key) {
        int low = 0;
        int high = size - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            E midVal = elements[mid];
            int cmp = compare((E) key, midVal);

            if (cmp < 0) {
                high = mid - 1;
            } else if (cmp > 0) {
                low = mid + 1;
            } else {
                return mid; // ключ найден
            }
        }
        return -(low + 1); // ключ не найден
    }

    @SuppressWarnings("unchecked")
    private int compare(E a, E b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            return ((Comparable<? super E>) a).compareTo(b);
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        E[] newElements = (E[]) new Object[elements.length * 2];
        System.arraycopy(elements, 0, newElements, 0, size);
        elements = newElements;
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
        Object[] result = new Object[size];
        System.arraycopy(elements, 0, result, 0, size);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        }
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
}