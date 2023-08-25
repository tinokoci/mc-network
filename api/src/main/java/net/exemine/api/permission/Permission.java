package net.exemine.api.permission;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.UUID;

@Getter
@Setter
public class Permission {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private final String id;
    private final UUID uuid;
    private final String node;

    private long addedAt;
    private long duration;
    private boolean removed;

    public Permission(UUID uuid, String node, long duration) {
        this.id = StringUtil.randomID();
        this.uuid = uuid;
        this.node = node;
        this.addedAt = System.currentTimeMillis();
        this.duration = duration;
        this.removed = false;
    }

    public boolean isPermanent() {
        return duration == Long.MAX_VALUE;
    }

    public boolean isActive() {
        return !removed && !isExpired();
    }

    public boolean isExpired() {
        return !isPermanent() && addedAt + duration < System.currentTimeMillis();
    }

    public String getFormattedDuration() {
        return TimeUtil.getDurationForExpirable(isPermanent(), duration);
    }
}
