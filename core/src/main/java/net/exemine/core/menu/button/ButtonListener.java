package net.exemine.core.menu.button;

import com.execets.spigot.ExeSpigot;
import com.execets.spigot.handler.MovementHandler;
import net.exemine.api.data.impl.CoreData;
import net.exemine.core.Core;
import net.exemine.core.menu.Menu;
import net.exemine.core.menu.task.MenuUpdateTask;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.Objects;

public class ButtonListener implements Listener {

    private final UserService<CoreUser, CoreData> userService;
    private static final String SCAN_GLITCH = "scan-glitch";

    public ButtonListener(UserService<CoreUser, CoreData> userService) {
        this.userService = userService;
        registerPlayerMoveFix();
        new MenuUpdateTask();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onButtonClick(InventoryClickEvent event) {
        CoreUser user = userService.get((Player) event.getWhoClicked());
        Menu<?> openMenu = Menu.OPENED_MENUS.get(user.getUniqueId());

        if (openMenu == null) return;

        ClickType clickType = event.getClick();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory instanceof PlayerInventory || clickType.isKeyboardClick() || clickType.isShiftClick() || clickType == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            user.updateInventory();
            return;
        }
        Button button = openMenu.getButtons().get(event.getSlot());

        if (openMenu.isAllowItemsMovement()) {
            if (openMenu.inMovementBound(event.getSlot()) && (button == null || button.hasEmptyCallback())) return;
        }
        if (event.getSlot() != event.getRawSlot()) {
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
            }
            return;
        }

        if (button == null) {
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        button.getCallback().run(event.getClick());

        if (Menu.OPENED_MENUS.containsKey(user.getUniqueId())) {
            Menu<?> newMenu = Menu.OPENED_MENUS.get(user.getUniqueId());

            if (newMenu == openMenu) {
                if (newMenu.isUpdateAfterClick() && !openMenu.isAllowItemsMovement()) {
                    openMenu.setClosedByMenu(true);
                    newMenu.open();
                }
            }
        }
        if (event.isCancelled()) {
            Bukkit.getScheduler().runTaskLater(Core.get(), user::updateInventory, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Menu<?> openMenu = Menu.OPENED_MENUS.get(player.getUniqueId());

        if (openMenu == null) return;
        if (!openMenu.isClosedByMenu() && openMenu.isFireOnClose()) openMenu.onClose();

        Menu.OPENED_MENUS.remove(player.getUniqueId());

        player.setMetadata(SCAN_GLITCH, new FixedMetadataValue(Core.get(), true));
    }

    private void registerPlayerMoveFix() {
        MovementHandler interceptor = (player, to, from, packet) -> {
            if (!player.hasMetadata(SCAN_GLITCH)) return;
            player.removeMetadata(SCAN_GLITCH, Core.get());

            Arrays.stream(player.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .forEach(item -> {
                        ItemMeta meta = item.getItemMeta();
                        if (meta == null || !meta.hasDisplayName()) return;

                        if (meta.getDisplayName().contains("§b§c§d§e")) {
                            player.getInventory().remove(item);
                        }
                    });
        };
        ExeSpigot.INSTANCE.addMovementHandler(interceptor);
    }
}