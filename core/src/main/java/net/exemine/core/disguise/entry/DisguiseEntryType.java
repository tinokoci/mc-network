package net.exemine.core.disguise.entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.database.DatabaseCollection;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum DisguiseEntryType {

    NAME(DatabaseCollection.DISGUISE_NAMES),
    SKIN(DatabaseCollection.DISGUISE_SKINS);

    private final DatabaseCollection collection;

    @Nullable
    public static DisguiseEntryType get(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
