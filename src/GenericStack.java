import java.util.EmptyStackException;

public class GenericStack<T> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elements;
    private int size;

    public GenericStack() {
        this(DEFAULT_CAPACITY);
    }

    public GenericStack(int initialCapacity) {
        if (initialCapacity <= 0) throw new IllegalArgumentException("Capacity must be >0");
        elements = new Object[initialCapacity];
        size = 0;
    }

    public void push(T item) {
        if (size == elements.length) {
            Object[] newArr = new Object[elements.length * 2];
            System.arraycopy(elements, 0, newArr, 0, size);
            elements = newArr;
        }
        elements[size++] = item;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T item = (T) elements[--size];
        elements[size] = null;
        return item;
    }

    public boolean isEmpty() {
        return size == 0;
    }


//    @SuppressWarnings("unchecked")
//    public T peek() {
//        if (isEmpty()) throw new EmptyStackException();
//        return (T) elements[size - 1];
//    }
//    public int size() {
//        return size;
//    }
}