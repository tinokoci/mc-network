package net.exemine.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.environment.Environment;
import net.exemine.api.controller.platform.Platform;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.link.LinkService;
import net.exemine.api.permission.PermissionService;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.rank.RankService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.proxy.listener.UserListener;

import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Getter
@Plugin(id = "proxy-manager", name = "Proxy Manager", version = "1.0.0", authors = {"execets"})
public class Proxy {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigFile configFile;

    private BulkDataService bulkDataService;
    private DataService dataService;
    private DatabaseService databaseService;
    private InstanceService instanceService;
    private LinkService linkService;
    private PermissionService permissionService;
    private PropertiesService propertiesService;
    private PunishmentService punishmentService;
    private RankService rankService;
    private RedisService redisService;

    @Inject
    public Proxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfiguration();
        loadApi();
        loadServices();
        loadListeners();
        ApiController.getInstance().setBooted(true);
    }

    private void loadConfiguration() {
        configFile = new ConfigFile(getDataDirectory().toFile(), "config.yml");
    }

    private void loadApi() {
        Environment environment = Environment.get(configFile.getString("environment"));
        ApiController.getInstance()
                .setEnvironment(environment)
                .setPlatform(Platform.MINECRAFT)
                .setMainMinecraftThread(Thread.currentThread());
    }

    private void loadServices() {
        databaseService = new DatabaseService(configFile.getString("mongodb-uri"));
        redisService = new RedisService(configFile.getString("redis-uri"));
        dataService = new DataService(databaseService);
        linkService = new LinkService(dataService, bulkDataService, redisService);
        rankService = new RankService(databaseService, redisService);
        permissionService = new PermissionService(databaseService, redisService);
        propertiesService = new PropertiesService(databaseService, redisService);
        punishmentService = new PunishmentService(databaseService, redisService);
        bulkDataService = new BulkDataService(databaseService, permissionService, punishmentService, rankService);
        instanceService = new InstanceService(redisService);
        rankService.init(linkService);
    }

    private void loadListeners() {
        Stream.of(
                new UserListener(this)
        ).forEach(listener -> server.getEventManager().register(this, new UserListener(this)));
    }
}
