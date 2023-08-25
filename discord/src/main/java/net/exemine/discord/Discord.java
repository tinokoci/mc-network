package net.exemine.discord;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.exemine.api.controller.ApiController;
import net.exemine.api.controller.environment.Environment;
import net.exemine.api.controller.platform.Platform;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.link.LinkService;
import net.exemine.api.log.LogService;
import net.exemine.api.match.MatchService;
import net.exemine.api.permission.PermissionService;
import net.exemine.api.properties.PropertiesService;
import net.exemine.api.proxy.ProxyService;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.rank.RankService;
import net.exemine.api.redis.RedisService;
import net.exemine.api.twitter.TwitterService;
import net.exemine.api.util.config.ConfigFile;
import net.exemine.discord.changelog.ChangelogCommand;
import net.exemine.discord.claim.ClaimCommand;
import net.exemine.discord.claim.UHCClaimService;
import net.exemine.discord.claim.UnclaimCommand;
import net.exemine.discord.command.CommandService;
import net.exemine.discord.command.impl.LinkCommand;
import net.exemine.discord.command.impl.ToggleAlertsCommand;
import net.exemine.discord.command.impl.staff.ClearCommand;
import net.exemine.discord.command.impl.staff.DiscordLogsCommand;
import net.exemine.discord.command.impl.staff.GeoCheckCommand;
import net.exemine.discord.command.impl.staff.GeoCompareCommand;
import net.exemine.discord.command.impl.staff.ManualLinkCommand;
import net.exemine.discord.command.impl.staff.ManualUnlinkCommand;
import net.exemine.discord.command.impl.staff.McLogsCommand;
import net.exemine.discord.command.impl.staff.MotdCommand;
import net.exemine.discord.command.impl.staff.NetworkTipsCommand;
import net.exemine.discord.command.impl.staff.RestartCommand;
import net.exemine.discord.command.impl.staff.VPNCommand;
import net.exemine.discord.message.MessageLogListener;
import net.exemine.discord.stafflist.StaffListCommand;
import net.exemine.discord.stafflist.StaffListService;
import net.exemine.discord.ticket.impl.appeal.AppealTicketService;
import net.exemine.discord.ticket.impl.appeal.command.AppealCommand;
import net.exemine.discord.ticket.impl.appeal.command.CloseAppealCommand;
import net.exemine.discord.ticket.impl.appeal.command.DeescalateCommand;
import net.exemine.discord.ticket.impl.appeal.command.EscalateCommand;
import net.exemine.discord.ticket.impl.application.ApplicationTicketService;
import net.exemine.discord.ticket.impl.application.command.ApplyCommand;
import net.exemine.discord.ticket.impl.application.command.CloseApplicationCommand;
import net.exemine.discord.ticket.impl.support.SupportTicketService;
import net.exemine.discord.ticket.impl.support.command.CloseTicketCommand;
import net.exemine.discord.ticket.impl.support.command.TicketCommand;
import net.exemine.discord.uhc.UHCMatchTask;
import net.exemine.discord.user.UserListener;
import net.exemine.discord.user.UserSubscriber;
import net.exemine.discord.util.DiscordUtil;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.NONE)
@Getter
public class Discord {

    private static final Discord INSTANCE = new Discord();

    public static Discord get() {
        return INSTANCE;
    }

    private ConfigFile config;
    private Guild guild;
    private JDA jda;

    private AppealTicketService appealTicketService;
    private ApplicationTicketService applicationTicketService;
    private BulkDataService bulkDataService;
    private DataService dataService;
    private DatabaseService databaseService;
    private LinkService linkService;
    private LogService logService;
    private MatchService matchService;
    private PermissionService permissionService;
    private PropertiesService propertiesService;
    private ProxyService proxyService;
    private PunishmentService punishmentService;
    private RankService rankService;
    private RedisService redisService;
    private StaffListService staffListService;
    private SupportTicketService supportTicketService;
    private TwitterService twitterService;
    private UHCClaimService uhcClaimService;

    void start() {
        loadConfiguration();
        loadApi();
        connect();
        loadServices();
        loadListeners();
        loadCommands();
        loadOther();
    }

    private void loadConfiguration() {
        config = new ConfigFile(new File("discord"), "config.yml");
    }

    private void loadApi() {
        ApiController.getInstance()
                .setEnvironment(Environment.get(config.getString("environment")))
                .setPlatform(Platform.DISCORD)
                .setHastebinUrl(config.getString("hastebin-address"));
    }

    private void loadServices() {
        databaseService = new DatabaseService(config.getString("mongodb-uri"));
        redisService = new RedisService(config.getString("redis-uri"));
        dataService = new DataService(databaseService);
        permissionService = new PermissionService(databaseService, redisService);
        punishmentService = new PunishmentService(databaseService, redisService);
        rankService = new RankService(databaseService, redisService);
        bulkDataService = new BulkDataService(databaseService, permissionService, punishmentService, rankService);
        linkService = new LinkService(dataService, bulkDataService, redisService);
        appealTicketService = new AppealTicketService(jda, databaseService, dataService, redisService, punishmentService);
        applicationTicketService = new ApplicationTicketService(jda, databaseService);
        logService = new LogService(dataService, databaseService);
        matchService = new MatchService(databaseService);
        propertiesService = new PropertiesService(databaseService, redisService);
        proxyService = new ProxyService(databaseService, redisService, config.getString("proxy-key"));
        staffListService = new StaffListService(this);
        supportTicketService = new SupportTicketService(jda, databaseService);
        uhcClaimService = new UHCClaimService(this);
        twitterService = new TwitterService(config);
        rankService.init(linkService);
    }

    private void loadListeners() {
        Stream.of(
                new MessageLogListener(logService),
                new UserListener(linkService)
        ).forEach(listener -> jda.addEventListener(listener));
    }

    private void loadCommands() {
        new CommandService(jda).register(
                new AppealCommand(appealTicketService),
                new ApplyCommand(applicationTicketService),
                new ChangelogCommand(),
                new ClaimCommand(uhcClaimService),
                new ClearCommand(),
                new CloseAppealCommand(appealTicketService, punishmentService),
                new CloseApplicationCommand(applicationTicketService),
                new CloseTicketCommand(supportTicketService),
                new DeescalateCommand(appealTicketService),
                new DiscordLogsCommand(logService),
                new EscalateCommand(appealTicketService),
                new GeoCheckCommand(dataService, proxyService),
                new GeoCompareCommand(dataService, proxyService),
                new LinkCommand(dataService, redisService),
                new ManualLinkCommand(bulkDataService, dataService, linkService),
                new ManualUnlinkCommand(bulkDataService, dataService, linkService),
                new McLogsCommand(this),
                new MotdCommand(propertiesService),
                new NetworkTipsCommand(propertiesService),
                new RestartCommand(),
                new StaffListCommand(dataService, staffListService),
                new TicketCommand(supportTicketService),
                new ToggleAlertsCommand(),
                new UnclaimCommand(matchService),
                new VPNCommand(dataService, proxyService)
        );
    }

    private void loadOther() {
        new UHCMatchTask(matchService, redisService, propertiesService, dataService, twitterService);
        new UserSubscriber(redisService);
    }

    private void connect() {
        try {
            jda = JDABuilder.create(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                    .setToken(config.getString("discord-token"))
                    .setActivity(DiscordUtil.getActivity(config.getString("activity.type"), config.getString("activity.message")))
                    .build()
                    .awaitReady(); // block main thread until connection to discord is fully established
            guild = jda.getGuilds().get(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }
}
