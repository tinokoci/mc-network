package net.exemine.core.server;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.Core;
import net.exemine.core.server.task.AutoMessageTask;
import net.exemine.core.server.task.RebootTask;
import net.exemine.core.util.InstanceUtil;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public class ServerService {

    private final ConfigFile configFile;

    public boolean chatMuted;
    private long chatDelay = 2000L;

    private Supplier<List<String>> listCallback;
    private RebootTask rebootTask;

    public ServerService(Core plugin) {
        new AutoMessageTask(plugin);
        configFile = plugin.getConfigFile();
    }

    public void updateWhitelist(Rank whitelistRank) {
        InstanceUtil.getCurrent().setWhitelistRank(whitelistRank);
        configFile.set("instance.whitelist", whitelistRank.name());
        configFile.save();
    }
}
