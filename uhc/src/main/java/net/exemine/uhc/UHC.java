package net.exemine.uhc;

import com.execets.spigot.ExeSpigot;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.impl.UHCData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.leaderboard.LeaderboardService;
import net.exemine.api.match.MatchService;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.core.Core;
import net.exemine.core.command.base.CommandService;
import net.exemine.core.provider.bossbar.BossBarService;
import net.exemine.core.provider.chat.ChatService;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.provider.scoreboard.ScoreboardService;
import net.exemine.core.settings.SettingsProvider;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.border.BorderListener;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.border.glass.GlassBorderService;
import net.exemine.uhc.command.BackpackCommand;
import net.exemine.uhc.command.ConfigCommand;
import net.exemine.uhc.command.CrossTeamCommand;
import net.exemine.uhc.command.HealthCommand;
import net.exemine.uhc.command.HelpOpCommand;
import net.exemine.uhc.command.JoinMeCommand;
import net.exemine.uhc.command.KillCountCommand;
import net.exemine.uhc.command.LateScatterCommand;
import net.exemine.uhc.command.LeaderboardsCommand;
import net.exemine.uhc.command.PracticeCommand;
import net.exemine.uhc.command.PracticeLayoutCommand;
import net.exemine.uhc.command.ScenariosCommand;
import net.exemine.uhc.command.SendCoordsCommand;
import net.exemine.uhc.command.StatsCommand;
import net.exemine.uhc.command.TeamChatCommand;
import net.exemine.uhc.command.TeamCommand;
import net.exemine.uhc.command.TeamListCommand;
import net.exemine.uhc.command.ThanksCommand;
import net.exemine.uhc.command.TopKillsCommand;
import net.exemine.uhc.command.staff.AutoStartCommand;
import net.exemine.uhc.command.staff.HostChatCommand;
import net.exemine.uhc.command.staff.InventorySeeCommand;
import net.exemine.uhc.command.staff.LocationCommand;
import net.exemine.uhc.command.staff.MuteSpectatorChatCommand;
import net.exemine.uhc.command.staff.SpectatorChatCommand;
import net.exemine.uhc.command.staff.TeleportCommand;
import net.exemine.uhc.command.staff.TeleportHereCommand;
import net.exemine.uhc.command.staff.UHCCommand;
import net.exemine.uhc.command.staff.WhitelistCommand;
import net.exemine.uhc.command.staff.toggle.ToggleHelpopAlertsCommand;
import net.exemine.uhc.command.staff.toggle.ToggleHostChatMessagesCommand;
import net.exemine.uhc.command.staff.toggle.ToggleSpectatorChatCommand;
import net.exemine.uhc.command.staff.toggle.ToggleStaffSpectatorsCommand;
import net.exemine.uhc.command.staff.toggle.ToggleXrayAlertsCommand;
import net.exemine.uhc.command.toggle.ToggleDeathMessagesCommand;
import net.exemine.uhc.command.toggle.ToggleSpectatorsCommand;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.game.GameListener;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.hook.HookService;
import net.exemine.uhc.leaderboard.npc.NPCLeaderboardService;
import net.exemine.uhc.lobby.LobbyListener;
import net.exemine.uhc.location.LocationService;
import net.exemine.uhc.logger.CombatLoggerService;
import net.exemine.uhc.logger.meta.MetaService;
import net.exemine.uhc.provider.UHCBossBarProvider;
import net.exemine.uhc.provider.UHCChatProvider;
import net.exemine.uhc.provider.UHCNametagProvider;
import net.exemine.uhc.provider.UHCScoreboardProvider;
import net.exemine.uhc.scatter.ScatterService;
import net.exemine.uhc.scenario.BlockBreakListener;
import net.exemine.uhc.scenario.loot.LootChestListener;
import net.exemine.uhc.spectator.SpectatorListener;
import net.exemine.uhc.team.TeamService;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UserListener;
import net.exemine.uhc.vape.AntiVapeSpecPacketHandler;
import net.exemine.uhc.vote.VoteCommand;
import net.exemine.uhc.vote.VoteService;
import net.exemine.uhc.vote.create.CreateVoteCommand;
import net.exemine.uhc.world.WorldService;
import net.exemine.uhc.world.antixray.AntiXrayListener;
import net.exemine.uhc.world.antixray.AntiXrayService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.stream.Stream;

@Getter
public class UHC extends JavaPlugin {

    private static UHC instance;

    @Setter
    private boolean shuttingDown;

    public static UHC get() {
        return instance;
    }

    public Core getCore() {
        return Core.get();
    }

    private ConfigFile configFile;

    private BorderService borderService;
    private GlassBorderService glassBorderService;
    private GameService gameService;
    private HookService hookService;
    private NPCLeaderboardService npcLeaderboardService;
    private LocationService locationService;
    private CombatLoggerService combatLoggerService;
    private MetaService metaService;
    private ScatterService scatterService;
    private TeamService teamService;
    private UHCUserService userService;
    private WorldService worldService;
    private VoteService voteService;
    private AntiXrayService antiXrayService;

    private LeaderboardService<UHCData> leaderboardService;
    private NametagService<UHCUser> nametagService;
    private MatchService matchService;

    @Override
    public void onEnable() {
        instance = this;

        loadConfiguration();
        loadServices();
        loadProviders();
        loadCommands();
        loadListeners();
        loadOther();
    }

    private void loadConfiguration() {
        configFile = new ConfigFile(UHC.class, getDataFolder(), "config.yml");
    }

    private void loadServices() {
        userService = new UHCUserService(this, getCore().getDatabaseService(), () -> new UHCUser(this, UHCData.class, userService), UHCData::new, DatabaseCollection.USERS_UHC);
        gameService = new GameService(this);
        hookService = new HookService(this);
        teamService = new TeamService(this, gameService);
        combatLoggerService = new CombatLoggerService(this);
        metaService = new MetaService();
        borderService = new BorderService(this);
        worldService = new WorldService(this, borderService);
        locationService = new LocationService(this);
        glassBorderService = new GlassBorderService(borderService, locationService, worldService, getCore().getUserService());
        scatterService = new ScatterService(this);
        leaderboardService = new LeaderboardService<>(UHCData.class, getCore().getDataService(), getCore().getBulkDataService());
        npcLeaderboardService = new NPCLeaderboardService(this, configFile, leaderboardService);
        matchService = new MatchService(getCore().getDatabaseService());
        voteService = new VoteService();
        antiXrayService = new AntiXrayService(userService, worldService);
    }

    private void loadProviders() {
        new ChatService<>(this, userService, new UHCChatProvider(gameService, userService), getCore().getRedisService(), getCore().getServerService());
        new ScoreboardService<>(this, userService, new UHCScoreboardProvider(borderService, gameService, scatterService, userService));
        nametagService = new NametagService<>(this, userService, new UHCNametagProvider(gameService));
        new BossBarService<>(this, userService, new UHCBossBarProvider(gameService));
        SettingsProvider.setModuleUserService(userService);
    }

    private void loadCommands() {
        new CommandService<>(this, userService).register(
                new BackpackCommand(gameService),
                new ConfigCommand(this),
                new HealthCommand(),
                new HelpOpCommand(userService),
                new JoinMeCommand(getCore().getRedisService(), gameService),
                new KillCountCommand(gameService),
                new LateScatterCommand(gameService),
                new LeaderboardsCommand(userService, leaderboardService),
                new PracticeCommand(gameService, userService),
                new PracticeLayoutCommand(),
                new ScenariosCommand(),
                new SendCoordsCommand(),
                new StatsCommand(),
                new TeamChatCommand(),
                new TeamCommand(gameService, teamService),
                new CrossTeamCommand(gameService, teamService),
                new TeamListCommand(),
                new ThanksCommand(gameService),
                new TopKillsCommand(gameService, userService),
                new ToggleDeathMessagesCommand(),
                new ToggleSpectatorsCommand(),
                new AutoStartCommand(this),
                new HostChatCommand(),
                new InventorySeeCommand(gameService),
                new LocationCommand(locationService),
                new MuteSpectatorChatCommand(gameService),
                new SpectatorChatCommand(gameService),
                new TeleportCommand(),
                new TeleportHereCommand(),
                new UHCCommand(this),
                new WhitelistCommand(gameService),
                new ToggleHelpopAlertsCommand(),
                new ToggleHostChatMessagesCommand(),
                new ToggleSpectatorChatCommand(),
                new ToggleStaffSpectatorsCommand(),
                new ToggleXrayAlertsCommand(),
                new CreateVoteCommand(voteService),
                new VoteCommand(voteService)
        );
    }

    private void loadListeners() {
        Stream.of(
                new AntiXrayListener(antiXrayService, worldService),
                new BorderListener(this),
                new GameListener(this),
                new LobbyListener(userService, locationService, worldService),
                new LootChestListener(this),
                new BlockBreakListener(metaService, userService),
                new SpectatorListener(gameService, userService),
                new UserListener(this)
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    private void loadOther() {
        Arrays.stream(ToggleOption.values()).forEach(option -> option.initialize(this));
        ExeSpigot.INSTANCE.addPacketHandler(new AntiVapeSpecPacketHandler());
        ItemBuilder.addStringFromWoolRecipe();
    }
}
