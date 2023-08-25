package net.exemine.uhc.config.menu;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.uhc.config.editor.OptionEditor;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.user.UHCUser;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigEditMenu extends PaginatedMenu<UHCUser> {

    public ConfigEditMenu(UHCUser user) {
        super(user, CC.DARK_GRAY + "Option Editor", 5, 3);
        setAutoSurround(true);
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        Arrays.stream(NumberOption.values()).forEach(option -> paginate(index.getAndIncrement(), option.getItem(true))
                .onClick(() -> {
                    if (!option.canBeEdited()) return;
                    close();
                    OptionEditor.set(user, option);
                    user.sendMessage();
                    user.sendMessage(CC.PINK + "Please input a new value for the " + CC.PURPLE + option.getName() + CC.PINK + " option.");
                    user.sendMessage(CC.ITALIC_GRAY + "(You can type 'cancel' to cancel the process.)");
                    user.sendMessage();
                })
        );
        Arrays.stream(ToggleOption.values()).forEach(option -> paginate(index.getAndIncrement(), option.getItem())
                .onClick(option::toggle)
        );
    }
}