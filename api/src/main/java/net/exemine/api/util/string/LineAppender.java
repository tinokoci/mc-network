package net.exemine.api.util.string;

public class LineAppender {

    private final StringBuilder builder = new StringBuilder();

    public LineAppender append(String str) {
        builder.append(str).append(System.lineSeparator());
        return this;
    }

    public String toString() {
        return builder.toString();
    }
}