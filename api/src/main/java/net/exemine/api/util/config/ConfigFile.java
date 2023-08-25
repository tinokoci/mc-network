package net.exemine.api.util.config;

import net.exemine.api.util.Executor;
import net.exemine.api.util.config.configuration.InvalidConfigurationException;
import net.exemine.api.util.config.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile extends YamlConfiguration {

    private final File file;

    public ConfigFile(Class<?> clazz, File dataFolder, String name) {
        this.file = new File(dataFolder, name);

        if (!file.exists()) {
            if (clazz == null) clazz = getClass();
            saveResource(clazz.getClassLoader(), dataFolder, name, false);
        }
        load();
    }

    public ConfigFile(File dataFolder, String name) {
        this(null, dataFolder, name);
    }

    public void load() {
        try {
            load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        save(true);
    }

    public void save(boolean async) {
        Executor.schedule(() -> {
            try {
                save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).run(async);
    }
}
