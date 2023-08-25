package net.exemine.api.texture;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.DatabaseUtil;

@Getter
public class TextureEntry {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private final String uuid;
    private final String name;
    private final String value;
    private final String signature;
    private final long addedAt;

    public TextureEntry(String uuid, String name, String value, String signature) {
        this.uuid = uuid;
        this.name = name;
        this.value = value;
        this.signature = signature;
        this.addedAt = System.currentTimeMillis();
    }

    public boolean equals(TextureEntry entry) {
        return entry.getValue().equals(value) && entry.getSignature().equals(signature);
    }

    public boolean isTextureOutdated() {
        return System.currentTimeMillis() - addedAt > TimeUtil.HOUR * 12;
    }
}
