package net.exemine.core.logs;

import net.exemine.api.log.LogService;
import net.exemine.api.log.minecraft.MinecraftLogType;
import net.exemine.api.util.EnumUtil;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.logs.procedure.LogsProcedure;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;

import java.util.Arrays;

public class LogsMenu extends Menu<CoreUser> {

    private final LogService logService;
    private final LogsProcedure procedure;

    public LogsMenu(CoreUser user, ExeUser<?> target) {
        super(user, CC.DARK_GRAY + "Logs Lookup", 3);
        this.logService = user.getPlugin().getLogService();
        setAutoSurround(true);
        procedure = LogsProcedure.getProcedure(user);
        procedure.setTarget(target);
    }

    @Override
    public void update() {
        addExitItem();

        set(12, new ItemBuilder()
                .setMaterial(Material.LEVER)
                .setName(CC.PINK + "Start Time")
                .setLore(lore -> {
                    lore.add("");
                    lore.add(CC.GRAY + "Current:");
                    lore.add(CC.WHITE + TimeUtil.getDate(procedure.getStartTimestamp()));
                    lore.add("");
                    lore.add(CC.BOLD_RED + "Warning!");
                    lore.add(CC.RED + "Time must be in UTC.");
                    lore.add("");
                    lore.add(CC.GREEN + "Click to update.");
                })
                .build()
        ).onClick(() -> {
            close(false);
            user.sendMessage(CC.PURPLE + "[Logs] " + CC.GRAY + "Please input the time format. Example: " + CC.BOLD_GOLD + "19/02/2023 02:53");
        });
        set(13, new ItemBuilder()
                .setMaterial(Material.DROPPER)
                .setName(CC.PINK + "Log Type")
                .setLore(lore -> {
                            lore.add("");
                            Arrays.stream(MinecraftLogType.values()).forEach(type ->
                                    lore.add(Lang.LIST_PREFIX + (procedure.getType() == type ? CC.GOLD : CC.WHITE) + type.getName()));
                            lore.add("");
                            lore.add(CC.GREEN + "Click to update.");
                        }
                ).build()
        ).onClick(() -> {
            procedure.setType(EnumUtil.getNext(procedure.getType()));
            user.sendMessage(CC.PURPLE + "[Logs] " + CC.GRAY + "You have updated logs type to " + CC.GOLD + procedure.getType().getName() + CC.GRAY + '.');
            open();
        });
        set(14, new ItemBuilder()
                .setMaterial(Material.TRIPWIRE_HOOK)
                .setName(CC.PINK + "End Time")
                .setLore(lore -> {
                    lore.add("");
                    lore.add(CC.GRAY + "Current:");
                    lore.add(CC.WHITE + TimeUtil.getDate(procedure.getEndTimestamp()));
                    lore.add("");
                    lore.add(CC.BOLD_RED + "Warning!");
                    lore.add(CC.RED + "Time must be in UTC.");
                    lore.add("");
                    lore.add(CC.GREEN + "Click to update.");
                })
                .build());
        set(size - 9, new ItemBuilder()
                .setMaterial(Material.WOOL)
                .setDurability(ItemUtil.getGreen())
                .setName(CC.GREEN + "Fetch Logs")
                .build()
        ).onClick(() -> Executor.schedule(() -> {
            String url = logService.fetchMinecraftLogs(procedure.getTarget().getCoreData(), procedure.getType(), procedure.getStartTimestamp(), procedure.getEndTimestamp());
            user.sendMessage(CC.PURPLE + "[Logs] " + CC.GRAY + "Link: " + CC.GOLD + url);
            close();
        }).runAsync());
    }

    @Override
    public void onClose() {
        procedure.cancel();
    }
}