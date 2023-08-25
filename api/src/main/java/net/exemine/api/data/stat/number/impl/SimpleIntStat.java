package net.exemine.api.data.stat.number.impl;

public class SimpleIntStat {

    private int value;

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public void add(int amount) {
        setValue(getValue() + amount);
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
