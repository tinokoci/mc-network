package net.exemine.core.provider;

import net.exemine.core.provider.bossbar.BossBar;
import net.exemine.core.user.base.ExeUser;

public interface BossBarProvider<T extends ExeUser<?>> {

    void setup(T user, BossBar<T> bossBar);

    void update(T user, BossBar<T> bossBar);
}
