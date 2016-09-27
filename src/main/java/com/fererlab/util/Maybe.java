package com.fererlab.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * utility class Maybe, simple object wrapper to prevent null pointer exceptions
 *
 * @param <T>
 */
public class Maybe<T> {

    /**
     * internal object
     */
    private T object;

    /**
     * empty state {}
     */
    private boolean empty = true;
    private boolean present;

    private Maybe(T object) {
        this.setObject(object);
    }

    private void setObject(T object) {
        this.object = object;
        if (this.object != null) {
            this.empty = false;
        }
    }

    public static <T> Maybe<T> empty() {
        return new Maybe<>(null);
    }

    public static <T> Maybe<T> create(T t) {
        return new Maybe<>(t);
    }

    public T get() {
        return object;
    }

    public void notEmpty(Consumer<T> consumer) {
        if (!isEmpty()) {
            consumer.accept(object);
        }
    }

    public void notEmpty(Consumer<T> consumer, Lambda empty) {
        if (!isEmpty()) {
            consumer.accept(object);
        } else {
            empty.apply();
        }
    }

    public Object notEmpty(Function<T, Object> function, Object alternative) {
        if (!isEmpty()) {
            return function.apply(object);
        } else {
            return alternative;
        }
    }

    public T orElse(T t) {
        return isEmpty() ? t : get();
    }

    public T orElseThrow(Exception e) throws Exception {
        if (isEmpty()) {
            throw e;
        } else {
            return get();
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

}
