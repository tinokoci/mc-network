package net.exemine.core.settings;

import net.exemine.api.data.ExeData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.settings.type.SettingsType;
import net.exemine.core.settings.type.SettingsTypeMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.item.ItemBuilder;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsMenu extends Menu<CoreUser> {

    public SettingsMenu(CoreUser user) {
        super(user, CC.DARK_GRAY + "Settings", 3);
        setAutoFill(true);
    }

    @Override
    public void update() {
        addExitItem();

        AtomicInteger index = new AtomicInteger(11);
        Arrays.stream(SettingsType.values()).forEach(type -> {
            set(index.getAndIncrement(), new ItemBuilder()
                    .setMaterial(type.getMaterial())
                    .setName(CC.BOLD_PINK + type.getName())
                    .setLore(CC.GRAY + "All of the " + type.getName().toLowerCase() + " settings.")
                    .build()
            ).onClick(() -> Executor.schedule(() -> {
                ExeData data = getData(type);
                if (data == null) {
                    user.sendMessage(CC.RED + "You've never played on that gamemode.");
                    return;
                }
                new SettingsTypeMenu(user, type, data).open();
            }).runAsync());
        });
    }

    private ExeData getData(SettingsType type) {
        if (type.getInstanceType() == InstanceType.UNKNOWN) {
            return user.getData();
        }
        if (InstanceUtil.isType(type.getInstanceType())) {
            return SettingsProvider.getModuleUserService().get(user).getData();
        }
        Optional<? extends ExeData> data = user.getPlugin().getDataService().fetch(type.getDataClazz(), user.getUniqueId());
        return data.orElse(null);
    }
}
