package net.exemine.api.controller;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.api.controller.environment.Environment;
import net.exemine.api.controller.exception.IllegalMinecraftThreadException;
import net.exemine.api.controller.platform.Platform;
import net.exemine.api.controller.platform.exception.IllegalPlatformException;
import net.exemine.api.util.Executor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
public class ApiController {

    private static final ApiController INSTANCE = new ApiController();

    private Environment environment;
    private Platform platform;

    private Executor.SyncCallback syncExecutorCallback;
    private Thread mainMinecraftThread;

    private String hastebinUrl;
    private boolean booted;

    public boolean isEnvironment(Environment environment) {
        Objects.requireNonNull(platform, "Environment is not defined");
        return this.environment == environment;
    }

    public boolean isPlatform(Platform platform) {
        Objects.requireNonNull(platform, "Platform is not defined");
        return this.platform == platform;
    }

    public boolean isMinecraftPlatform() {
        return isPlatform(Platform.MINECRAFT);
    }

    public boolean isMainMinecraftThread() {
        if (!isMinecraftPlatform()) return false;
        Objects.requireNonNull(mainMinecraftThread, "Main Minecraft thread cannot be null on a Minecraft platform");
        return Thread.currentThread() == mainMinecraftThread;
    }

    public static void requireMinecraftPlatform() {
        if (!INSTANCE.isMinecraftPlatform()) {
            throw new IllegalPlatformException("This action can only be completed on the MINECRAFT platform");
        }
    }

    public static void requireAsyncMinecraftThread() {
        if (INSTANCE.isMainMinecraftThread()) {
            throw new IllegalMinecraftThreadException();
        }
    }

    public static ApiController getInstance() {
        return INSTANCE;
    }
}