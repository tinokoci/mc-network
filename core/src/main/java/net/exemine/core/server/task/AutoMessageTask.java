package net.exemine.core.server.task;

import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AutoMessageTask extends BukkitRunnable {

    private final Core plugin;

    private boolean started;
    private int index = 0;

    public AutoMessageTask(Core plugin) {
        this.plugin = plugin;
        runTaskTimerAsynchronously(plugin, 0, 20 * 60 * 3);
    }

    @Override
    public void run() {
        List<String> messages = new ArrayList<>(getMessages());
        if (messages.isEmpty()) return;

        int index = getIndex();
        String[] text = CC.translate(messages.get(index)).split("<nl>");

        plugin.getUserService().getOnlineUsers()
                .stream()
                .filter(user -> user.getData().isServerTips())
                .forEach(user -> {
                    user.sendMessage();
                    user.sendMessage(text);
                    user.sendMessage();
                });

        if (!started) started = true;
    }

    private int getIndex() {
        int size = getMessages().size();
        int index = ThreadLocalRandom.current().nextInt(size);

        if (!started || index != this.index || size == 1) {
            this.index = index;
            return index;
        }
        return getIndex();
    }

    public Collection<String> getMessages() {
        return plugin.getPropertiesService().getProperties().getListOfNetworkTips();
    }
}
