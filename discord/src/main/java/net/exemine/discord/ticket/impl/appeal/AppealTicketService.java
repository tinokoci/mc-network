package net.exemine.discord.ticket.impl.appeal;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentService;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.rank.Rank;
import net.exemine.api.redis.RedisService;
import net.exemine.api.util.EnumUtil;
import net.exemine.api.util.Hastebin;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.ticket.TicketService;
import net.exemine.discord.ticket.impl.appeal.data.AppealData;
import net.exemine.discord.ticket.impl.appeal.level.AppealLevel;
import net.exemine.discord.ticket.impl.appeal.stage.AppealStage;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.ticket.type.TicketType;
import net.exemine.discord.user.NetworkUser;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Getter
public class AppealTicketService extends TicketService<AppealTicket> {

    private final DataService dataService;
    private final RedisService redisService;
    private final PunishmentService punishmentService;

    private final String buttonGuiltyId = "guilty";
    private final String buttonInnocentId = "innocent";

    public AppealTicketService(JDA jda, DatabaseService databaseService, DataService dataService, RedisService redisService, PunishmentService punishmentService) {
        super(jda, databaseService, TicketType.APPEAL, AppealTicket.class, TicketState.CREATING, false);
        this.dataService = dataService;
        this.redisService = redisService;
        this.punishmentService = punishmentService;
    }

    @Override
    protected void sendInitialMessage(AppealTicket ticket, TextChannel channel, Member member) {
        channel.sendMessageEmbeds(EmbedUtil.create("Are you guilty?", "Click on the appropriate button."))
                .setActionRows(ActionRow.of(
                        Button.danger(buttonGuiltyId, "Guilty"),
                        Button.success(buttonInnocentId, "Innocent")
                )).queue();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected TextChannel createChannel(AppealTicket ticket, Category category, Member member) {
        EnumSet<Permission> permissions = EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND);
        ChannelAction<TextChannel> action = category.createTextChannel(getChannelName(member));
        Arrays.stream(Rank.values())
                .filter(rank -> rank.isEqualOrAbove(Rank.MOD))
                .forEach(rank -> action.addPermissionOverride(DiscordUtil.getOrCreateRole(rank), permissions, null));

        action.addPermissionOverride(member, permissions, null);
        action.addPermissionOverride(DiscordConstants.getEveryoneRole(), null, permissions);

        return action.complete();
    }

    public void advanceStage(AppealTicket ticket) {
        AppealStage newStage = Arrays.stream(AppealStage.values())
                .filter(stage -> stage.ordinal() - 1 == ticket.getStage().ordinal())
                .findFirst()
                .orElse(null);
        if (newStage == null) return;

        ticket.setStage(newStage);

        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return;

        AppealData data = ticket.getData();
        NetworkUser networkUser = NetworkUser.getOrCreate(ticket.getUser());
        networkUser.loadBulkData();

        BulkData bulkData = networkUser.getBulkData();

        switch (newStage) {
            case TYPE:
                List<Punishment> punishments = bulkData.getActivePunishments(PunishmentType.IP_BAN, PunishmentType.BAN, PunishmentType.MUTE);

                if (punishments.isEmpty()) {
                    close(ticket, TicketState.INVALID);
                    return;
                }
                if (punishments.size() == 1) {
                    data.setPunishmentType(punishments.get(0).getType());
                    advanceStage(ticket);
                    return;
                }
                List<ItemComponent> components = punishments
                        .stream()
                        .map(punishment -> Button.danger(punishment.getType().name(), punishment.getType().getName()))
                        .collect(Collectors.toList());
                channel.sendMessageEmbeds(EmbedUtil.create("Which punishment do you want to appeal?", "Please click the corresponding button."))
                        .setActionRow(components)
                        .queue();
                break;
            case REASON:
                Punishment punishment = bulkData.getActivePunishment(data.getPunishmentType());
                data.setPunishmentId(punishment.getId());
                data.setPunishmentReason(punishment.getAddedReason());
                channel.sendMessageEmbeds(EmbedUtil.create("Why should you get " + ticket.getData().getPunishmentType().getPardon() + '?', "Please don't input false information."))
                        .queue();
                break;
            case FINISHED:
                punishment = bulkData.getActivePunishment(data.getPunishmentType());
                AtomicReference<String> issuerName = new AtomicReference<>("Console");
                UUID addedBy = punishment.getAddedBy();

                if (addedBy != null) {
                    dataService.fetch(CoreData.class, punishment.getAddedBy()).ifPresent(coreData -> issuerName.set(coreData.getName()));
                }
                String reasonForPardon = data.getReasonForPardon();
                String displayedReasonForPardon = reasonForPardon.length() > 900
                        ? Hastebin.paste(reasonForPardon, true)
                        : reasonForPardon;

                channel.sendMessageEmbeds(EmbedUtil.builder("Appeal Information",
                                "Hello " + ticket.getUser().getAsMention() + ", thanks for creating an appeal."
                                        + "\nA staff member will reach out to you shortly."
                                        + "\n" + DiscordConstants.INVISIBLE_CHAR)
                        .setFooter(DiscordUtil.getCurrentDate())
                        .addField("Punishment",
                                "Issuer: " + DiscordUtil.code(issuerName.get())
                                        + "\nType: " + DiscordUtil.code(data.getPunishmentType().getName())
                                        + "\nDuration: " + DiscordUtil.code(TimeUtil.getNormalDuration(punishment.getDuration()))
                                        + "\nReason: " + DiscordUtil.code(data.getPunishmentReason())
                                        + '\n' + DiscordConstants.INVISIBLE_CHAR,
                                false)
                        .addField("User",
                                "Guilty: " + DiscordUtil.code(data.isGuilty() ? "Yes" : "No")
                                        + '\n'
                                        + "\nReason for the punishment removal:"
                                        + "\n" + DiscordUtil.bold(displayedReasonForPardon)
                                        + '\n' + DiscordConstants.INVISIBLE_CHAR,
                                false)
                        .build()
                ).queue();
                ticket.setState(TicketState.OPEN);
        }
        updateTicketInDatabase(ticket);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean changeLevel(AppealTicket ticket, boolean escalate) {
        AppealLevel previousLevel = ticket.getLevel();
        AppealLevel newLevel = EnumUtil.getAdjacent(previousLevel, escalate);

        if (escalate && previousLevel.ordinal() > newLevel.ordinal() ||
                !escalate && previousLevel.ordinal() < newLevel.ordinal()) {
            return false;
        }
        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return false;

        ticket.setLevel(newLevel);
        updateTicketInDatabase(ticket);

        TextChannelManager manager = channel.getManager();
        if (escalate) {
            Arrays.stream(Rank.values())
                    .filter(rank -> rank.isEqualOrAbove(previousLevel.getRank()) && !rank.isEqualOrAbove(newLevel.getRank()))
                    .forEach(rank -> manager.putPermissionOverride(DiscordUtil.getOrCreateRole(rank), EnumSet.of(Permission.VIEW_CHANNEL), EnumSet.of(Permission.MESSAGE_SEND)));
        } else {
            Arrays.stream(Rank.values())
                    .filter(rank -> !rank.isEqualOrAbove(previousLevel.getRank()) && rank.isEqualOrAbove(newLevel.getRank()))
                    .forEach(rank -> manager.putPermissionOverride(DiscordUtil.getOrCreateRole(rank), EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), EnumSet.noneOf(Permission.class)));
        }
        manager.queue();
        return true;
    }
}
