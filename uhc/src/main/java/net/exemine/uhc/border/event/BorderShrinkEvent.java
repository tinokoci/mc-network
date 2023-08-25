package net.exemine.uhc.border.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.core.util.Event;
import net.exemine.uhc.border.BorderRadius;
import org.bukkit.event.Cancellable;

@RequiredArgsConstructor
@Getter
@Setter
public class BorderShrinkEvent extends Event implements Cancellable {

    private final BorderRadius borderRadius;
    private boolean cancelled;
}
