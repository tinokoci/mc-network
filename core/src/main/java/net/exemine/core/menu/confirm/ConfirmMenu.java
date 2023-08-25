package net.exemine.core.menu.confirm;

import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.stream.IntStream;

public class ConfirmMenu extends Menu<ExeUser<?>> {

    private final boolean closeAfterResponse;
    private final TypeCallback<Boolean> callback;

    public ConfirmMenu(ExeUser<?> user, boolean closeAfterResponse, TypeCallback<Boolean> callback) {
        super(user, CC.DARK_GRAY + "Confirm this action:", 5);
        this.closeAfterResponse = closeAfterResponse;
        this.callback = callback;
        setAutoFill(true);
    }

    @Override
    public void update() {
        IntStream.of(10, 11, 12, 19, 21, 28, 29, 30).forEach(index -> set(index, new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName(CC.GREEN + "Click on the green wool to confirm this action.")
                .setDurability(13)
                .build()));
        IntStream.of(14, 15, 16, 23, 25, 32, 33, 34).forEach(index -> set(index, new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName(CC.RED + "Click on the red wool to cancel this action.")
                .setDurability(14)
                .build()));

        set(20, new ItemBuilder(Material.WOOL)
                .setDurability(ItemUtil.getGreen())
                .setName(CC.GREEN + "Confirm!")
                .setLore("",
                        CC.GRAY + "This action cannot be undone.",
                        CC.GRAY + "Please be careful."
                ).build()).onClick(() -> getCallback().run(true));
        set(24, new ItemBuilder(Material.WOOL)
                .setDurability(ItemUtil.getRed())
                .setName(CC.RED + "Cancel!")
                .setLore("",
                        CC.GRAY + "If you are not sure to complete",
                        CC.GRAY + "this action please cancel it instead."
                ).build()).onClick(() -> getCallback().run(false));
    }

    private TypeCallback<Boolean> getCallback() {
        return confirm -> {
            if (confirm) user.playSound(Sound.SUCCESSFUL_HIT);
            if (closeAfterResponse) user.closeInventory();

            callback.run(confirm);
        };
    }

    @Override
    public void onClose() {
        callback.run(false);
    }
}
