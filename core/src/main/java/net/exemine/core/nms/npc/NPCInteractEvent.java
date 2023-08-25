package net.exemine.core.nms.npc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.core.util.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter
public class NPCInteractEvent extends Event implements Cancellable {

    private final Player player;
    private final NPC npc;

    @Setter
    private boolean cancelled;
}