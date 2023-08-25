package net.exemine.api.controller.exception;

public class IllegalMinecraftThreadException extends IllegalStateException {

    public IllegalMinecraftThreadException() {
        super("Database action ran on the main Minecraft thread");
    }
}
