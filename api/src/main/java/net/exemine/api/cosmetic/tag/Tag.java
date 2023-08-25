package net.exemine.api.cosmetic.tag;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.cosmetic.Cosmetic;
import net.exemine.api.cosmetic.CosmeticType;
import net.exemine.api.util.string.DatabaseUtil;

@RequiredArgsConstructor
@Getter
public class Tag implements Cosmetic {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private final String name;
    private final String format;

    @Override
    public CosmeticType getType() {
        return CosmeticType.TAG;
    }

    @Override
    public String name() {
        return name;
    }
}
