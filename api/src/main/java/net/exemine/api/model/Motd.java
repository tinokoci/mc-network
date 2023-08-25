package net.exemine.api.model;

import lombok.Setter;

import java.util.Optional;

@Setter
public class Motd {

    private String line1;
    private String line2;

    public String getLine1() {
        return Optional.ofNullable(line1).orElse("Line 1");
    }

    public String getLine2() {
        return Optional.ofNullable(line2).orElse("Line 2");
    }

    public String getCombined() {
        return getLine1() + '\n' + getLine2();
    }
}
