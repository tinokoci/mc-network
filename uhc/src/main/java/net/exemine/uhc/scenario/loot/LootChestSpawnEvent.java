package net.exemine.uhc.scenario.loot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.core.util.Event;
import net.exemine.uhc.user.UHCUser;

@RequiredArgsConstructor
@Getter
@Setter
public class LootChestSpawnEvent extends Event {

    private final UHCUser user;
    private final LootChest lootChest;
}
