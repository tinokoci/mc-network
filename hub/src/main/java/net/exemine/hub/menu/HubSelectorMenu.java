package net.exemine.hub.menu;

import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceService;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.hub.user.HubUser;
import org.bukkit.Material;

import java.util.concurrent.atomic.AtomicInteger;

public class HubSelectorMenu extends Menu<HubUser> {

    private final InstanceService instanceService;

    public HubSelectorMenu(HubUser user) {
        super(user, CC.DARK_GRAY + "Hub Selector", 3);
        this.instanceService = user.getPlugin().getCore().getInstanceService();
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();
        AtomicInteger index = new AtomicInteger(10);
        instanceService.getAllInstances(InstanceType.HUB).forEach(instance -> getHub(index.getAndIncrement(), instance));
    }

    private void getHub(int slot, Instance instance) {
        boolean connectedToInstance = instance.equals(InstanceUtil.getCurrent());

        set(slot, new ItemBuilder(instance.isOffline() ? Material.WOOL : Material.WATCH)
                .setDurability(instance.isOffline() ? ItemUtil.getRed() : -1)
                .setName((instance.isOffline() ? CC.BOLD_RED : connectedToInstance ? CC.BOLD_GREEN : CC.BOLD_PINK) + instance.getName())
                .setLore(lore -> {
                    lore.add(CC.DARK_GRAY + "Lobby");
                    lore.add("");
                    lore.add(CC.GRAY + "This is a hub server.");
                    lore.add("");
                    lore.add(CC.GRAY + "State: " + instance.getStatus(user.isEqualOrAbove(RankType.STAFF)));
                    lore.add(CC.GRAY + "Players: " + CC.RESET + instance.getOnlinePlayers() + "/" + instance.getMaxPlayers());
                    lore.add("");
                    lore.add(instance.isOffline() ? CC.RED + "Server is offline."
                            : connectedToInstance ? CC.GREEN + "You are on here."
                            : CC.GREEN + "Click to connect!");
                }).build()
        ).onClick(() -> user.performCommand("join " + instance.getName()));
    }
}

