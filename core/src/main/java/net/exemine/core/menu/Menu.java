package net.exemine.core.menu;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.Executor;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.button.Button;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Getter
@Setter
public abstract class Menu<T extends ExeUser<?>> {

    public static final Map<UUID, Menu<?>> OPENED_MENUS = new HashMap<>();
    public final Map<Integer, Button> buttons = new HashMap<>();

    public final T user;
    private final String title;
    public final int size;

    private boolean updateAfterClick = true;
    private boolean fireOnClose = true;
    private boolean closedByMenu;
    private boolean allowItemsMovement;
    private int[] itemMovementBound = new int[2];

    private Inventory inventory;

    // Optional values
    public T target;
    private boolean autoUpdate;
    private boolean async;
    private boolean autoFill;
    private boolean autoSurround;

    private ItemStack placeholderItem = new ItemBuilder()
            .setMaterial(Material.STAINED_GLASS_PANE)
            .setName(" ")
            .setDurability(15)
            .build();

    private Button surroundItem = new Button(placeholderItem);
    private Button fillItem = new Button(placeholderItem);

    public Menu(T user, String title, int rows) {
        this.user = user;
        this.title = StringUtil.limitLength(title, 32);
        this.size = rows * 9;
    }

    public void open() {
        Executor.schedule(() -> {
            buttons.clear();
            update();
            Menu<?> previousMenu = OPENED_MENUS.get(user.getUniqueId());
            InventoryView openInventory = user.getOpenInventory();
            boolean update = false;

            if (openInventory != null) {
                if (previousMenu == null) {
                    user.closeInventory();
                } else {
                    int previousSize = openInventory.getTopInventory().getSize();

                    if (previousSize == size && user.getOpenInventory().getTopInventory().getTitle().equals(title)) {
                        inventory = user.getOpenInventory().getTopInventory();
                        update = true;
                    } else {
                        previousMenu.setClosedByMenu(true);
                        user.closeInventory();
                    }
                }
            }
            if (inventory == null) {
                inventory = Bukkit.createInventory(user, size, title);
            }
            inventory.setContents(new ItemStack[inventory.getSize()]);

            OPENED_MENUS.put(user.getUniqueId(), this);

            if (isAutoSurround()) {
                IntStream.range(0, size)
                        .filter(slot -> buttons.get(slot) == null)
                        .filter(slot -> slot < 9 || slot > size - 10 || slot % 9 == 0 || (slot + 1) % 9 == 0)
                        .forEach(index -> buttons.put(index, surroundItem));
            }
            if (isAutoFill()) {
                IntStream.range(0, size)
                        .filter(index -> buttons.get(index) == null)
                        .forEach(index -> buttons.put(index, fillItem));
            }
            for (Map.Entry<Integer, Button> buttonEntry : buttons.entrySet()) {
                inventory.setItem(buttonEntry.getKey(), buttonEntry.getValue().getItem());
            }
            for (int i = 0; i < size; i++) {
                if (!buttons.containsKey(i)) {
                    inventory.setItem(i, null);
                }
            }

            if (update) {
                user.updateInventory();
            } else {
                user.openInventory(inventory);
            }
            setClosedByMenu(false);
        }).run(async);
    }

    public void close(boolean fireOnClose) {
        this.fireOnClose = fireOnClose;
        user.closeInventory();
    }

    public void close() {
        close(true);
    }

    public void addReturnItem(Menu<T> menu) {
        set(size - 9, new ItemBuilder(Material.ARROW)
                .setName(CC.RED + "Go Back")
                .build()
        ).onClick(menu::open);
    }

    public void addExitItem() {
        set(size - 1, new ItemBuilder(Material.REDSTONE)
                .setName(CC.RED + "Close")
                .build()
        ).onClick(() -> close(true));
    }

    public void fill(ItemStack item) {
        setFillItem(new Button(item));
        setAutoFill(true);
    }

    public Button set(int slot, ItemStack item) {
        Button button = new Button(item);
        buttons.put(slot, button);
        return button;
    }

    public void setAutoFill(boolean autoFill) {
        this.autoFill = autoFill;
    }

    public void setAutoFill(ItemStack item) {
        autoFill = true;
        fillItem = new Button(new ItemStack(item));
    }

    public boolean inMovementBound(int slot) {
        return slot >= itemMovementBound[0] && slot < itemMovementBound[1];
    }



    public abstract void update();

    public void onClose() {}
}