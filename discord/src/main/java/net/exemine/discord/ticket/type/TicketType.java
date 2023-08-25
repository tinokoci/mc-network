package net.exemine.discord.ticket.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.util.TimeUtil;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum TicketType {

    APPLICATION("Staff Applications", "apply", TimeUtil.DAY, DatabaseCollection.TICKETS_APPLICATION),
    APPEAL("Punishment Appeals", "appeal", TimeUtil.HOUR * 12, DatabaseCollection.TICKETS_APPEAL),
    SUPPORT("Support Tickets", "ticket", TimeUtil.HOUR, DatabaseCollection.TICKETS_SUPPORT);

    private final String categoryName;
    private final String channelPrefix;
    private final long closingDuration;
    private final DatabaseCollection databaseCollection;

    public String getChannelPrefix() {
        return channelPrefix + '-';
    }

    public int getPosition() {
        return ordinal();
    }

    public static TicketType getByCategoryName(String categoryName) {
        return Arrays.stream(values())
                .filter(type -> type.getCategoryName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(null);
    }
}
