package net.exemine.core.disguise.entry;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.util.string.DatabaseUtil;

@RequiredArgsConstructor
@Getter
public class DisguiseEntry {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private final String value;
    private final DisguiseEntryType type;
}
