package net.exemine.api.rank;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.UUID;

@Getter
@Setter
@ToString
public class RankInfo {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private String id;
    private UUID uuid;
    private int index;

    private Rank rank;
    private UUID addedBy;
    private String addedReason;
    private long addedAt;
    private long duration;

    private boolean removed;
    private String removedReason;
    private UUID removedBy;
    private long removedAt;

    public RankInfo(int index, UUID uuid, Rank rank, UUID addedBy, String addedReason, long duration) {
        this.id = StringUtil.randomID();
        this.index = index;
        this.uuid = uuid;
        this.rank = rank;
        this.addedBy = addedBy;
        this.addedAt = System.currentTimeMillis();
        this.addedReason = addedReason;
        this.duration = duration;
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
