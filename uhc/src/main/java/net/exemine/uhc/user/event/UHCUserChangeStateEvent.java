package net.exemine.uhc.user.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.core.util.Event;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;

@RequiredArgsConstructor
@Getter
public class UHCUserChangeStateEvent extends Event {

    private final UHCUser user;
    private final UHCUserState oldState;
}
