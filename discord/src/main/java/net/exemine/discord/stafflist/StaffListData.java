package net.exemine.discord.stafflist;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
public class StaffListData {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    @Getter(AccessLevel.NONE)
    private final UUID uuid;
    private StaffListPosition position;

    public UUID getUniqueId() {
        return uuid;
    }
}
