package net.exemine.discord.ticket;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.util.Executor;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.ReflectionUtil;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.ticket.type.TicketType;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class TicketService<T extends Ticket> extends ListenerAdapter {

    private final DatabaseService databaseService;
    private final TicketType type;
    private final Class<T> typeClass;
    private final TicketState initialState;
    private final boolean denyReadOnClose;

    private final Set<T> tickets = ConcurrentHashMap.newKeySet();

    public TicketService(JDA jda, DatabaseService databaseService, TicketType type, Class<T> typeClass, TicketState initialState, boolean denyReadOnClose) {
        this.type = type;
        this.typeClass = typeClass;
        this.databaseService = databaseService;
        this.initialState = initialState;
        this.denyReadOnClose = denyReadOnClose;

        databaseService.findAll(type.getDatabaseCollection())
                .run()
                .stream()
                .map(document -> GsonUtil.fromDocument(document, typeClass))
                .filter(Ticket::isInProgress)
                .forEach(tickets::add);

        schedule();
        jda.addEventListener(this);
    }

    protected abstract void sendInitialMessage(T ticket, TextChannel channel, Member member);

    protected abstract TextChannel createChannel(T ticket, Category category, Member member);

    protected String getChannelName(Member member) {
        return type.getChannelPrefix() + member.getEffectiveName();
    }

    private Category createCategory() {
        Category category = DiscordUtil.getCategory(type.getCategoryName());

        if (category == null) {
            category = DiscordUtil.getGuild()
                    .createCategory(type.getCategoryName())
                    .setPosition(getCategoryPosition())
                    .complete();
            // We have to manually refresh positions because they get messed up otherwise
            DiscordUtil.refreshCategoryPositions();
        }
        return category;
    }

    private int getCategoryPosition() {
        int startingPosition = DiscordConstants.TICKET_CATEGORIES_STARTING_POSITION;
        int categoryPosition = TicketType.getByCategoryName(type.getCategoryName()).getPosition();

        // We're making sure that categories are always in the same order
        return (int) (startingPosition
                + Arrays.stream(TicketType.values())
                .filter(type -> type.getPosition() < categoryPosition && DiscordUtil.hasCategory(type.getCategoryName()))
                .count());
    }

    public T createTicket(Member member) {
        T ticket = ReflectionUtil.newInstance(typeClass);
        ticket.setId(StringUtil.randomID(6));
        ticket.setTimestamp(System.currentTimeMillis());
        ticket.setUserId(member.getUser().getId());
        ticket.setState(initialState);

        Category category = createCategory();
        TextChannel channel = createChannel(ticket, category, member);

        ticket.setChannelId(channel.getId());
        sendInitialMessage(ticket, channel, member);
        channel.sendMessage("Ghost tagging " + member.getAsMention() + " for them to easily notice the ticket.")
                .complete()
                .delete()
                .queue();

        tickets.add(ticket);
        databaseService.insert(type.getDatabaseCollection(), GsonUtil.toDocument(ticket)).run();
        return ticket;
    }

    public void scheduleToClose(T ticket) {
        ticket.setState(TicketState.CLOSING);
        ticket.setClosedAt(System.currentTimeMillis());
        ticket.setLocked(true);
        updateUserChannelPermissions(ticket.getTextChannel(), ticket.getMember(), true);
        updateTicketInDatabase(ticket);
    }

    public void close(T ticket, TicketState state) {
        if (state != TicketState.CLOSED && state != TicketState.INVALID) {
            throw new IllegalArgumentException("You can only close the ticket with the CLOSED or INVALID state");
        }
        if (ticket.getClosedAt() == 0L) {
            ticket.setClosedAt(System.currentTimeMillis());
        }
        ticket.setState(state);
        updateTicketInDatabase(ticket);
        deleteChannel(ticket);
        tickets.remove(ticket);
    }

    public void addMessageToTicket(T ticket, Message message) {
        Member member = message.getMember();
        String username = member == null ? "Unknown" : member.getNickname() != null ? member.getNickname() : member.getUser().getName();

        ticket.getMessages().add(username + ": " + message.getContentRaw());
        updateTicketInDatabase(ticket);
    }

    private void deleteChannel(T ticket) {
        TextChannel channel = ticket.getTextChannel();
        if (channel == null) return;

        channel.delete().queue(success -> {
            Category category = DiscordUtil.getCategory(type.getCategoryName());
            if (category.getTextChannels().isEmpty()) {
                category.delete().queue();
            }
        });
    }

    private void updateUserChannelPermissions(TextChannel channel, Member member, boolean locked) {
        if (channel == null || member == null) return;

        EnumSet<Permission> allow = EnumSet.noneOf(Permission.class);
        EnumSet<Permission> deny = EnumSet.noneOf(Permission.class);

        addPermissionToEnumSet(allow, deny, Permission.VIEW_CHANNEL, !locked || !denyReadOnClose);
        addPermissionToEnumSet(allow, deny, Permission.MESSAGE_SEND, !locked);

        String topic = locked ? "This ticket is locked." : "";

        channel.getManager()
                .putPermissionOverride(member, allow, deny)
                .setTopic(topic)
                .queue();
    }

    private void addPermissionToEnumSet(EnumSet<Permission> allow, EnumSet<Permission> deny, Permission permission, boolean value) {
        if (value) {
            allow.add(permission);
        } else {
            deny.add(permission);
        }
    }

    public void updateTicketInDatabase(T ticket) {
        Executor.schedule(() -> databaseService.update(
                type.getDatabaseCollection(),
                Filters.eq(DatabaseUtil.PRIMARY_KEY, ticket.getId()),
                GsonUtil.toDocument(ticket)
        ).run()).runAsync();
    }

    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        MessageChannel channel = event.getChannel();
        if (!channel.getName().startsWith(type.getChannelPrefix())) return;

        T ticket = get(event.getChannel());
        if (ticket == null) return;

        addMessageToTicket(ticket, event.getMessage());
    }

    private void schedule() {
        Executor.schedule(() -> {
            Iterator<T> iterator = tickets.iterator();

            while (iterator.hasNext()) {
                T ticket = iterator.next();
                if (!ticket.availableToClose(type.getClosingDuration())) continue;
                close(ticket, TicketState.CLOSED);
                iterator.remove();
            }
        }).runAsyncTimer(0, 1000L);
    }

    @Nullable
    public T get(MessageChannel channel) {
        return tickets
                .stream()
                .filter(appeal -> appeal.getChannelId().equals(channel.getId()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public T get(User user) {
        return tickets
                .stream()
                .filter(ticket -> ticket.getUser().equals(user))
                .findFirst()
                .orElse(null);
    }
}
