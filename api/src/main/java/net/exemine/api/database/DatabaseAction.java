package net.exemine.api.database;

import lombok.RequiredArgsConstructor;
import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.exception.IllegalMinecraftThreadException;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DatabaseAction<T> {

    private final Supplier<T> callback;
    private boolean ignoreThreadOverload;

    public DatabaseAction<T> ignoreThreadOverload() {
        ignoreThreadOverload = true;
        return this;
    }

    public T run() {
        if (!ignoreThreadOverload
                && ApiController.getInstance().isMainMinecraftThread()
                && ApiController.getInstance().isBooted()) {
            throw new IllegalMinecraftThreadException();
        }
        return callback.get();
    }
}
