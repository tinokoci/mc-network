package net.exemine.core;

import com.execets.spigot.ExeSpigot;
import lombok.Getter;
import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.environment.Environment;
import net.exemine.api.controller.platform.Platform;
import net.exemine.api.cosmetic.tag.TagService;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.link.LinkService;
import net.exemine.api.log.LogService;
import net.exemine.api.match.MatchService;
import net.exemine.api.permission.PermissionService;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.proxy.ProxyService;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.texture.TextureService;
import net.exemine.api.util.Executor;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.command.DayCommand;
import net.exemine.core.command.DiscordCommand;
import net.exemine.core.command.HubCommand;
import net.exemine.core.command.IgnoreCommand;
import net.exemine.core.command.JoinCommand;
import net.exemine.core.command.LinkCommand;
import net.exemine.core.command.ListCommand;
import net.exemine.core.command.MessageCommand;
import net.exemine.core.command.NightCommand;
import net.exemine.core.command.PingCommand;
import net.exemine.core.command.ReplyCommand;
import net.exemine.core.command.ReportCommand;
import net.exemine.core.command.ResetTimeCommand;
import net.exemine.core.command.SettingsCommand;
import net.exemine.core.command.StoreCommand;
import net.exemine.core.command.SunsetCommand;
import net.exemine.core.command.TwitterCommand;
import net.exemine.core.command.UnlinkCommand;
import net.exemine.core.command.base.CommandService;
import net.exemine.core.command.staff.AdminChatCommand;
import net.exemine.core.command.staff.AdventureCommand;
import net.exemine.core.command.staff.BroadcastMeCommand;
import net.exemine.core.command.staff.BroadcastRawCommand;
import net.exemine.core.command.staff.CreativeCommand;
import net.exemine.core.command.staff.GamemodeCommand;
import net.exemine.core.command.staff.GlobalListCommand;
import net.exemine.core.command.staff.InstanceCommand;
import net.exemine.core.command.staff.LinkLockCommand;
import net.exemine.core.command.staff.MaintenanceCommand;
import net.exemine.core.command.staff.MotdCommand;
import net.exemine.core.command.staff.MuteChatCommand;
import net.exemine.core.command.staff.PermissionCommand;
import net.exemine.core.command.staff.ProviderCommand;
import net.exemine.core.command.staff.RebootCommand;
import net.exemine.core.command.staff.ReportsCommand;
import net.exemine.core.command.staff.SlowChatCommand;
import net.exemine.core.command.staff.StaffChatCommand;
import net.exemine.core.command.staff.SurvivalCommand;
import net.exemine.core.command.staff.UserImportCommand;
import net.exemine.core.command.staff.VPNCommand;
import net.exemine.core.command.staff.VersionCommand;
import net.exemine.core.command.staff.WhitelistCommand;
import net.exemine.core.command.staff.toggle.ToggleInstanceAlertsCommand;
import net.exemine.core.command.staff.toggle.ToggleStaffMessagesCommand;
import net.exemine.core.command.staff.toggle.ToggleStaffReportsCommand;
import net.exemine.core.command.staff.toggle.ToggleStaffServerSwitchCommand;
import net.exemine.core.command.staff.toggle.ToggleStaffSocialSpyCommand;
import net.exemine.core.command.toggle.ToggleBossBarCommand;
import net.exemine.core.command.toggle.ToggleGameBroadcastsCommand;
import net.exemine.core.command.toggle.ToggleParticlesCommand;
import net.exemine.core.command.toggle.TogglePrivateMessagesCommand;
import net.exemine.core.command.toggle.ToggleServerTipsCommand;
import net.exemine.core.command.toggle.ToggleSoundsCommand;
import net.exemine.core.command.toggle.lunar.ToggleBorderCommand;
import net.exemine.core.command.toggle.lunar.ToggleNametagsCommand;
import net.exemine.core.command.toggle.lunar.ToggleTeamViewCommand;
import net.exemine.core.command.toggle.lunar.ToggleTitlesCommand;
import net.exemine.core.command.toggle.lunar.ToggleWaypointsCommand;
import net.exemine.core.cosmetic.CosmeticCommand;
import net.exemine.core.cosmetic.CosmeticListener;
import net.exemine.core.cosmetic.bow.BowTrailsCommand;
import net.exemine.core.cosmetic.color.ColorCommand;
import net.exemine.core.cosmetic.rod.RodTrailsCommand;
import net.exemine.core.cosmetic.tag.command.TagAdminCommand;
import net.exemine.core.cosmetic.tag.command.TagsCommand;
import net.exemine.core.disguise.DisguiseService;
import net.exemine.core.disguise.command.DisguiseAdminCommand;
import net.exemine.core.disguise.command.DisguiseCheckCommand;
import net.exemine.core.disguise.command.DisguiseCommand;
import net.exemine.core.disguise.command.DisguiseListCommand;
import net.exemine.core.disguise.command.UndisguiseCommand;
import net.exemine.core.logs.LogsCommand;
import net.exemine.core.logs.LogsListener;
import net.exemine.core.match.MatchHistoryCommand;
import net.exemine.core.match.UpcomingGamesCommand;
import net.exemine.core.match.host.HostHistoryCommand;
import net.exemine.core.menu.button.ButtonListener;
import net.exemine.core.nms.NMSListener;
import net.exemine.core.playtime.PlayTimeCommand;
import net.exemine.core.profile.ProfileCommand;
import net.exemine.core.punishment.command.AltsCommand;
import net.exemine.core.punishment.command.HistoryCommand;
import net.exemine.core.punishment.command.PunishCommand;
import net.exemine.core.punishment.command.impl.BanCommand;
import net.exemine.core.punishment.command.impl.BlacklistCommand;
import net.exemine.core.punishment.command.impl.IPBanCommand;
import net.exemine.core.punishment.command.impl.KickCommand;
import net.exemine.core.punishment.command.impl.MuteCommand;
import net.exemine.core.punishment.command.impl.UnbanCommand;
import net.exemine.core.punishment.command.impl.UnblacklistCommand;
import net.exemine.core.punishment.command.impl.UnmuteCommand;
import net.exemine.core.punishment.task.PunishmentTask;
import net.exemine.core.rank.command.RankHistoryCommand;
import net.exemine.core.rank.command.SetRankCommand;
import net.exemine.core.rank.task.RankTask;
import net.exemine.core.report.ReportService;
import net.exemine.core.server.ServerListener;
import net.exemine.core.server.ServerService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.UserListener;
import net.exemine.core.user.UserSubscriber;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.ServerUtil;
import net.exemine.core.util.item.Glow;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class Core extends JavaPlugin {

    private static Core instance;

    public static Core get() {
        return instance;
    }

    private ConfigFile configFile;
    private String instanceName;
    private InstanceType instanceType;

    private BulkDataService bulkDataService;
    private DataService dataService;
    private DatabaseService databaseService;
    private DisguiseService disguiseService;
    private InstanceService instanceService;
    private LinkService linkService;
    private LogService logService;
    private MatchService matchService;
    private PermissionService permissionService;
    private PropertiesService propertiesService;
    private ProxyService proxyService;
    private PunishmentService punishmentService;
    private RankService rankService;
    private RedisService redisService;
    private ReportService reportService;
    private ServerService serverService;
    private TagService tagService;
    private TextureService textureService;
    private UserService<CoreUser, CoreData> userService;

    @Override
    public void onEnable() {
        instance = this;

        loadConfiguration();
        loadApi();
        loadServices();
        loadListeners();
        loadCommands();
        loadOther();
        loadStartupHook();
    }

    @Override
    public void onDisable() {
        instanceService.unregisterInstance();
    }

    private void loadConfiguration() {
        configFile = new ConfigFile(Core.class, getDataFolder(), "config.yml");
        instanceName = configFile.getString("instance.name");
        instanceType = InstanceType.get(configFile.getString("instance.type"), true);
    }

    private void loadApi() {
        ApiController.getInstance()
                .setEnvironment(Environment.get(configFile.getString("environment")))
                .setPlatform(Platform.MINECRAFT)
                .setMainMinecraftThread(Thread.currentThread())
                .setSyncExecutorCallback((callback, delay, period) -> Bukkit.getScheduler().runTaskTimer(this, callback::run, delay, period).getTaskId())
                .setHastebinUrl(configFile.getString("hastebin-address"));
    }

    private void loadServices() {
        databaseService = new DatabaseService(configFile.getString("mongodb-uri"));
        dataService = new DataService(databaseService);
        redisService = new RedisService(configFile.getString("redis-uri"));
        tagService = new TagService(databaseService, redisService);
        textureService = new TextureService(databaseService, redisService);
        instanceService = new InstanceService(redisService);
        permissionService = new PermissionService(databaseService, redisService);
        proxyService = new ProxyService(databaseService, redisService, configFile.getString("proxy-key"));
        punishmentService = new PunishmentService(databaseService, redisService);
        linkService = new LinkService(dataService, bulkDataService, redisService);
        logService = new LogService(dataService, databaseService);
        rankService = new RankService(databaseService, redisService);
        bulkDataService = new BulkDataService(databaseService, permissionService, punishmentService, rankService);
        userService = new UserService<>(this, databaseService, () -> new CoreUser(CoreData.class, this), CoreData::new, DatabaseCollection.USERS_CORE);
        serverService = new ServerService(this);
        reportService = new ReportService(userService, redisService);
        propertiesService = new PropertiesService(databaseService, redisService);
        disguiseService = new DisguiseService(this);
        matchService = new MatchService(databaseService);
        rankService.init(linkService);
    }

    private void loadListeners() {
        Stream.of(
                new ButtonListener(userService),
                new CosmeticListener(userService),
                new LogsListener(userService, logService),
                new NMSListener(userService),
                new ServerListener(),
                new UserListener(userService)
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void loadCommands() {
        new CommandService<>(this, userService).register(
                new AdminChatCommand(redisService),
                new AdventureCommand(),
                new AltsCommand(userService),
                new BanCommand(punishmentService, userService),
                new BlacklistCommand(punishmentService, userService),
                new BowTrailsCommand(),
                new BroadcastMeCommand(),
                new BroadcastRawCommand(),
                new ColorCommand(),
                new CosmeticCommand(),
                new CreativeCommand(),
                new DayCommand(),
                new DiscordCommand(),
                new DisguiseAdminCommand(disguiseService),
                new DisguiseCheckCommand(),
                new DisguiseCommand(disguiseService, textureService),
                new DisguiseListCommand(),
                new GamemodeCommand(),
                new GlobalListCommand(instanceService),
                new HistoryCommand(userService),
                new HostHistoryCommand(matchService),
                new HubCommand(),
                new IPBanCommand(punishmentService, userService),
                new IgnoreCommand(),
                new InstanceCommand(instanceService),
                new JoinCommand(instanceService),
                new KickCommand(punishmentService, userService),
                new LinkCommand(redisService),
                new LinkLockCommand(),
                new ListCommand(serverService),
                new LogsCommand(),
                new MaintenanceCommand(propertiesService),
                new MatchHistoryCommand(matchService),
                new MessageCommand(),
                new MotdCommand(propertiesService),
                new MuteChatCommand(serverService),
                new MuteCommand(punishmentService, userService),
                new NightCommand(),
                new PermissionCommand(permissionService),
                new PlayTimeCommand(bulkDataService, userService),
                new ProfileCommand(),
                new ProviderCommand(),
                new PunishCommand(userService),
                new RankHistoryCommand(),
                new RebootCommand(this),
                new PingCommand(),
                new ReplyCommand(),
                new ReportCommand(reportService),
                new ReportsCommand(reportService),
                new ResetTimeCommand(),
                new RodTrailsCommand(),
                new SetRankCommand(rankService),
                new SettingsCommand(),
                new SlowChatCommand(serverService, redisService),
                new StaffChatCommand(redisService),
                new StoreCommand(),
                new SunsetCommand(),
                new SurvivalCommand(),
                new TagAdminCommand(tagService),
                new TagsCommand(),
                new ToggleBorderCommand(),
                new ToggleBossBarCommand(),
                new ToggleGameBroadcastsCommand(),
                new ToggleInstanceAlertsCommand(),
                new ToggleNametagsCommand(),
                new ToggleParticlesCommand(),
                new TogglePrivateMessagesCommand(),
                new ToggleServerTipsCommand(),
                new ToggleSoundsCommand(),
                new ToggleStaffMessagesCommand(),
                new ToggleStaffReportsCommand(),
                new ToggleStaffServerSwitchCommand(),
                new ToggleStaffSocialSpyCommand(),
                new ToggleTeamViewCommand(),
                new ToggleTitlesCommand(),
                new ToggleWaypointsCommand(),
                new TwitterCommand(),
                new UnbanCommand(punishmentService, userService),
                new UnblacklistCommand(punishmentService, userService),
                new UndisguiseCommand(disguiseService),
                new UnlinkCommand(linkService),
                new UnmuteCommand(punishmentService, userService),
                new UpcomingGamesCommand(matchService),
                new UserImportCommand(dataService),
                new VersionCommand(),
                new VPNCommand(this),
                new WhitelistCommand(serverService)
        );
    }

    private void loadOther() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        new RankTask(this);
        new PunishmentTask(this);
        new UserSubscriber(redisService, userService);
        new Glow();
    }

    private void loadStartupHook() {
        ExeSpigot.INSTANCE.setStartupHook(() -> {
            instanceService.registerInstance(instanceName, instanceType, instance -> redisService.getPublisher().sendInstanceHeartbeat(
                    instance.getName(),
                    instance.getType(),
                    userService.getOnlineUsers()
                            .stream()
                            .map(CoreUser::getDisplayName)
                            .collect(Collectors.toList()),
                    Bukkit.getMaxPlayers(),
                    Bukkit.spigot().getTPS()[0],
                    Bukkit.spigot().getTPS()[1],
                    Bukkit.spigot().getTPS()[2],
                    Rank.get(configFile.getString("instance.whitelist")),
                    instance.getExtra()
            ), command -> Executor.schedule(() -> ServerUtil.performCommand(command)).runSync());
            ApiController.getInstance().setBooted(true);
        });
    }
}
