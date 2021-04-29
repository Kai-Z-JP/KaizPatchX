package jp.ngt.ngtlib.util;

import java.util.LinkedList;
import java.util.List;

public class Stack<E> {
    private final List<E> list = new LinkedList<>();
    private final int maxSize;

    public Stack(int size) {
        this.maxSize = size;
    }

    public void push(E element) {
        while (this.list.size() > this.maxSize) {
            this.list.remove(this.list.size() - 1);
        }
        this.list.add(0, element);
    }

    public E pop() {
        if (!this.list.isEmpty()) {
            E element = this.list.get(0);
            this.list.remove(0);
            return element;
        }
        return null;
    }
}