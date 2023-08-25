package net.exemine.api.match;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.util.string.DatabaseUtil;

@Getter
@Setter
@NoArgsConstructor
public abstract class Match {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private String id;
    private MatchState state;
    private long duration;
    private String note;

    public boolean isCompleted() {
        return state == MatchState.RANKED || state == MatchState.UNRANKED;
    }

    public void init() {
        state = MatchState.WAITING;
    }

    protected abstract DatabaseCollection getMongoCollection();
}
