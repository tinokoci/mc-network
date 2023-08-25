package net.exemine.api.data;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.util.string.DatabaseUtil;

import java.util.UUID;

@Getter
@Setter
public abstract class ExeData {

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    private String name;
    private String queryName;

    public void setName(String name) {
        this.name = name;
        this.queryName = name.toLowerCase();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public abstract DatabaseCollection getMongoCollection();
}