package net.exemine.api.util.callable;

@FunctionalInterface
public interface Callback {

    void run();

    Callback EMPTY = () -> {};
}
