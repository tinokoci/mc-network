package net.exemine.discord.stafflist;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum StaffListPosition {

    PROJECT_MANAGER("Project Manager"),
    HEAD_OF_DEVELOPMENT("Head of Development"),
    HEAD_OF_STAFF("Head of Staff"),
    HEAD_OF_UHC_STAFF("Head of UHC Staff"),
    HEAD_OF_SUPPORT("Head of Support"),
    HEAD_OF_COMMUNITY_MANAGEMENT("Head of Community Management"),
    HEAD_OF_MARKETING("Head of Marketing"),
    HEAD_OF_CONTENT_CREATORS("Head of Content Creators");

    private final String name;

    @Nullable
    public static StaffListPosition get(@NotNull String name) {
        return Arrays.stream(values())
                .filter(position -> position.name.equalsIgnoreCase(name)
                        || position.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
