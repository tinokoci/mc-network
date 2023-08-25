package net.exemine.uhc.spectator.menu;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.pagination.PaginatedMenu;
import net.exemine.core.util.LocationUtil;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.spectator.tracker.PlayerTrackerType;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.info.GameInfo;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PlayerTrackerMenu extends PaginatedMenu<UHCUser> {

    private final GameService gameService;
    private final UHCUserService userService;
    private final PlayerTrackerType type;

    public PlayerTrackerMenu(UHCUser user, GameService gameService, UHCUserService userService, PlayerTrackerType type) {
        super(user, CC.DARK_GRAY + (user.isGameModerator() ? type.getName() + " Players" : "Player Tracker"), 5, 3);
        this.gameService = gameService;
        this.userService = userService;
        this.type = type;
        setAutoSurround(true);
    }

    @Override
    public void global() {
        addExitItem();
    }

    @Override
    public void pagination() {
        AtomicInteger index = new AtomicInteger();

        List<UHCUser> users = userService.values()
                .stream()
                .filter(trackedUser -> {
                    if (trackedUser.isInPractice() && type == PlayerTrackerType.PRACTICE) return true;
                    if (!trackedUser.isPlaying()) return false;
                    switch (type) {
                        case NETHER:
                            return trackedUser.isInNether();
                        case MINING:
                            return trackedUser.getLocation().getBlockY() <= 50;
                        default:
                            return true;
                    }

                })
                .sorted(Comparator.comparing(trackedUser -> type == PlayerTrackerType.PRACTICE
                        ? trackedUser.getGameInfo().getPracticeKills().getValue()
                        : trackedUser.getGameInfo().getKills().getValue(), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        if (users.isEmpty()) {
            set(22, new ItemBuilder()
                    .setMaterial(Material.WOOL)
                    .setDurability(ItemUtil.getRed())
                    .setName(CC.RED + "No players found.")
                    .build());
            fill(new ItemBuilder()
                    .setMaterial(Material.STAINED_GLASS_PANE)
                    .setDurability(14)
                    .setName(" ")
                    .build());
        } else {
            users.forEach(trackedUser -> {
                boolean canTeleport = user.isGameModerator() || LocationUtil.isInsideRadius(trackedUser, 100);
                paginate(index.getAndIncrement(), ItemBuilder.getPlayerHead(trackedUser)
                        .setName(trackedUser.getColoredDisplayName())
                        .setLore(() -> {
                            List<String> lore = new ArrayList<>();
                            if (user.isGameModerator()) {
                                GameInfo info = trackedUser.getGameInfo();

                                if (gameService.isStateOrLower(GameState.LOBBY)) {
                                    lore.add(CC.GRAY + "Practice Kills: " + CC.RESET + info.getPracticeKills().getValue());
                                    lore.add(CC.GRAY + "Practice Deaths: " + CC.RESET + info.getPracticeDeaths().getValue());

                                    if (info.getPracticeStreak().getValue() > 0) {
                                        lore.add(CC.GRAY + "Practice Streak: " + CC.RESET + info.getPracticeStreak().getValue());
                                    }
                                } else {
                                    lore.add(CC.GRAY + "Kills: " + CC.RESET + info.getKills().getValue());
                                    lore.add(CC.GRAY + "Diamonds: " + CC.RESET + info.getMinedDiamonds().getValue());
                                    lore.add(CC.GRAY + "Gold: " + CC.RESET + info.getMinedGold().getValue());
                                    lore.add(CC.GRAY + "Health: " + CC.RESET + trackedUser.getFormattedHealth());

                                    if (trackedUser.getAbsorption() > 0f) {
                                        lore.add(CC.GRAY + "Absorption: " + CC.RESET + trackedUser.getFormattedAbsorption());
                                    }
                                    lore.add(CC.GRAY + "Exp/Level: " + CC.RESET + (int) trackedUser.getExp() + '/' + trackedUser.getLevel());
                                }
                                lore.add("");
                                lore.add(CC.GREEN + "Left click to teleport.");
                                lore.add(CC.GREEN + "Right click to view inventory.");
                            } else {
                                lore.add(canTeleport
                                        ? CC.GREEN + "Click to teleport."
                                        : CC.RED + "Player is outside of 100x100.");
                            }
                            return lore;
                        })
                        .build()
                ).onClick((clickType -> {
                    if (!canTeleport) return;
                    if (!user.isGameModerator() || clickType == ClickType.LEFT) {
                        close();
                        user.teleport(trackedUser);
                        user.sendMessage(CC.PURPLE + "[Tracker] " + CC.GRAY + "You've teleported to " + trackedUser.getColoredDisplayName() + CC.GRAY + '.');
                        return;
                    }
                    if (clickType == ClickType.RIGHT) {
                        close();
                        user.performCommand("invsee " + trackedUser.getDisplayName());
                    }
                }));
            });
        }
    }
}
