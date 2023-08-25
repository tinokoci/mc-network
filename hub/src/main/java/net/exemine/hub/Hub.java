package net.exemine.hub;

import com.execets.spigot.ExeSpigot;
import lombok.Getter;
import net.exemine.api.data.impl.HubData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.Core;
import net.exemine.core.command.base.CommandService;
import net.exemine.core.provider.chat.ChatService;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.provider.scoreboard.ScoreboardService;
import net.exemine.core.settings.SettingsProvider;
import net.exemine.hub.command.EditCommand;
import net.exemine.hub.command.FlyCommand;
import net.exemine.hub.command.LocationCommand;
import net.exemine.hub.command.VisibilityCommand;
import net.exemine.hub.location.LocationService;
import net.exemine.hub.nms.NMSService;
import net.exemine.hub.provider.HubChatProvider;
import net.exemine.hub.provider.HubNametagProvider;
import net.exemine.hub.provider.HubScoreboardProvider;
import net.exemine.hub.user.HubUser;
import net.exemine.hub.user.HubUserService;
import net.exemine.hub.user.UserListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Stream;

@Getter
public class Hub extends JavaPlugin {

    private static Hub instance;

    public static Hub get() {
        return instance;
    }

    public Core getCore() {
        return Core.get();
    }

    private ConfigFile configFile;

    private HubUserService userService;
    private LocationService locationService;
    private NMSService nmsService;

    @Override
    public void onEnable() {
        instance = this;

        loadConfiguration();
        loadServices();
        loadListeners();
        loadProviders();
        loadCommands();
        loadOther();
    }

    private void loadConfiguration() {
        configFile = new ConfigFile(Hub.class, getDataFolder(), "config.yml");
    }

    private void loadServices() {
        userService = new HubUserService(this, () -> new HubUser(HubData.class, this), HubData::new, DatabaseCollection.USERS_LOBBY);
        locationService = new LocationService(configFile);
        nmsService = new NMSService(this, locationService);
        locationService.init(nmsService);
    }

    private void loadListeners() {
        Stream.of(
                new UserListener(this)
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void loadProviders() {
        new ChatService<>(this, userService, new HubChatProvider(), getCore().getRedisService(), getCore().getServerService());
        new NametagService<>(this, userService, new HubNametagProvider());
        new ScoreboardService<>(this, userService, new HubScoreboardProvider(getCore().getInstanceService()));
        SettingsProvider.setModuleUserService(userService);
    }

    private void loadCommands() {
        new CommandService<>(this, userService).register(
                new EditCommand(),
                new FlyCommand(),
                new LocationCommand(locationService),
                new VisibilityCommand()
        );
    }

    private void loadOther() {
        ExeSpigot.INSTANCE.getConfig().setHidePlayersFromTab(false);
    }
}