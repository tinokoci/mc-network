package net.exemine.uhc.user.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.Cooldown;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@Setter
public class NoCleanInfo {

    public static final Cooldown<UUID> COOLDOWN = new Cooldown<>();

    private final UUID uuid;

    public boolean isActive() {
        return COOLDOWN.isActive(uuid);
    }

    public String getShortDuration() {
        return COOLDOWN.getShortDuration(uuid);
    }
}
