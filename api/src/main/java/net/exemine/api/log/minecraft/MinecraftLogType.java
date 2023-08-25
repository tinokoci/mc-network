package net.exemine.api.log.minecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MinecraftLogType {

    PUBLIC("Public"),
    PRIVATE("Private"),
    COMMAND("Command");

    private final String name;
}
