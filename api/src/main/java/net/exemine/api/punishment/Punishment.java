package net.exemine.api.punishment;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.api.util.string.Lang;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class Punishment {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private String id;
    private UUID uuid;
    private int index;
    private PunishmentType type;
    private boolean active;

    private UUID addedBy;
    private long addedAt;
    private long duration;
    private String addedReason;
    private boolean addedSilently;

    private UUID removedBy;
    private long removedAt;
    private String removedReason;
    private boolean removedSilently;

    public Punishment(int index, UUID uuid, PunishmentType type, UUID addedBy, String addedReason, long duration) {
        this.id = StringUtil.randomID();
        this.index = index;
        this.uuid = uuid;
        this.type = type;
        this.addedBy = addedBy;
        this.addedAt = System.currentTimeMillis();
        this.addedReason = addedReason.replace("-s", "");
        this.duration = duration;
        this.active = true;
        this.addedSilently = addedReason.contains("-s");
    }

    public boolean isPermanent() {
        return duration == Long.MAX_VALUE;
    }

    public boolean isExpired() {
        return !isPermanent() && addedAt + duration < System.currentTimeMillis();
    }

    public String getFormattedDuration() {
        return TimeUtil.getDurationForExpirable(isPermanent(), duration);
    }

    public String getExpirationTime() {
        return TimeUtil.getNormalDuration(addedAt + duration - System.currentTimeMillis());
    }

    public String getLoginMessage() {
        StringBuilder builder = new StringBuilder();

        builder.append(CC.RED).append(type == PunishmentType.KICK ? "You have been " : "Your account is ").append(type.getFormat()).append(" from the " + Lang.SERVER_NAME + " Network!");
        builder.append(CC.RED).append("\n \nReason: ").append(addedReason);

        if (!isPermanent()) {
            builder.append(CC.RED).append("\nExpires in: ").append(getExpirationTime());
        }
        builder.append(CC.RED).append("\n \n").append("You can appeal @ " + Lang.DISCORD);

        return builder.toString();
    }
}