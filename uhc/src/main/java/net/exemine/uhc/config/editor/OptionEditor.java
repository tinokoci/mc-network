package net.exemine.uhc.config.editor;

import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.user.UHCUser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OptionEditor {

    public static final Map<UUID, NumberOption> EDITORS = new HashMap<>();

    public static void set(UHCUser user, NumberOption option) {
        EDITORS.put(user.getUniqueId(), option);
    }

    public static NumberOption get(UHCUser user) {
        return EDITORS.get(user.getUniqueId());
    }

    public static void remove(UHCUser user) {
        EDITORS.remove(user.getUniqueId());
    }
}
