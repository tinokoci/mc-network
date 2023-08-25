package net.exemine.uhc.user.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.core.util.Event;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter
@Setter
public class HeadPostSpawnEvent extends Event implements Cancellable {

    private final UHCUser user;
    private boolean cancelled;
}
