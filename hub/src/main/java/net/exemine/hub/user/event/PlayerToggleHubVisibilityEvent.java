package net.exemine.hub.user.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.exemine.core.util.Event;
import net.exemine.hub.user.HubUser;

@AllArgsConstructor
@Getter
public class PlayerToggleHubVisibilityEvent extends Event {

    private final HubUser user;
    private final boolean enabled;
}
