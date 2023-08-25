package net.exemine.core.menu.button;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.callable.Callback;
import net.exemine.api.util.callable.TypeCallback;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
@Setter
public class Button {

    private static final TypeCallback<ClickType> EMPTY_CALLBACK = TypeCallback.EMPTY();

    private final ItemStack item;
    private TypeCallback<ClickType> callback;

    public Button(ItemStack item, TypeCallback<ClickType> callback) {
        this.item = item;
        this.callback = callback;
        setupItem();
    }

    public Button(ItemStack item, Callback callback) {
        this(item, click -> callback.run());
    }

    public Button(ItemStack item) {
        this(item, EMPTY_CALLBACK);
    }

    public void onClick(TypeCallback<ClickType> callback) {
        this.callback = callback;
    }

    public void onClick(Callback callback) {
        this.callback = type -> callback.run();
    }

    public boolean hasEmptyCallback() {
        return callback.equals(EMPTY_CALLBACK);
    }

    public void setupItem() {
        if (item == null || item.getType() == Material.SKULL_ITEM) return;

        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            meta.setDisplayName(meta.getDisplayName() + "§b§c§d§e");
        }
        item.setItemMeta(meta);
    }
}
