package net.exemine.api.util.callable;

@FunctionalInterface
public interface TypeCallback<T> {

    void run(T type);

    static <T> TypeCallback<T> EMPTY() {
        return type -> {};
    }
}
