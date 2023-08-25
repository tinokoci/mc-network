package net.exemine.core.provider;

import net.exemine.core.provider.nametag.NametagInfo;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.user.base.ExeUser;

public interface NametagProvider<T extends ExeUser<?>> {

    NametagInfo getNametag(T toRefresh, T refreshFor, NametagService<T> nametagService);
}