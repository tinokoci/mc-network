package net.exemine.api.controller.platform.exception;

import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.platform.Platform;

public class IllegalPlatformException extends IllegalStateException {

    public IllegalPlatformException(Platform platform) {
        super("This is not supported on the " + platform.name() + " platform");
    }

    public IllegalPlatformException(String message) {
        super(message);
    }

    public IllegalPlatformException() {
        this(ApiController.getInstance().getPlatform());
    }
}
