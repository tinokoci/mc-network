package net.exemine.discord.ticket;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.discord.ticket.state.TicketState;
import net.exemine.discord.util.DiscordUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class Ticket {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private String id;

    private long timestamp;
    private long closedAt;
    private boolean locked;
    private String userId;
    private String channelId;
    private TicketState state;

    private final List<String> messages = new ArrayList<>();

    public boolean isState(TicketState state) {
        return this.state == state;
    }

    public boolean availableToClose(long closingDuration) {
        return getTextChannel() == null || state == TicketState.CLOSING && closedAt + closingDuration < System.currentTimeMillis();
    }

    public boolean isInProgress() {
        return state.ordinal() < TicketState.CLOSED.ordinal();
    }

    public boolean isOpen() {
        return isState(TicketState.OPEN) || isState(TicketState.CREATING);
    }

    public User getUser() {
        return DiscordUtil.getUser(userId);
    }

    @Nullable
    public Member getMember() {
        return DiscordUtil.getGuild().getMember(getUser());
    }

    @Nullable
    public TextChannel getTextChannel() {
        return DiscordUtil.getTextChannelById(channelId);
    }
}
