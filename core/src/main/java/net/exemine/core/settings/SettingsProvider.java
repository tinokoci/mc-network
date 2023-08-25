package net.exemine.core.settings;

import lombok.Getter;
import lombok.Setter;
import net.exemine.core.user.base.UserService;

public class SettingsProvider {

    @Setter
    @Getter
    private static UserService<?, ?> moduleUserService;
}
