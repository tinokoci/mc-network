package net.exemine.core.settings.type;

import net.exemine.api.data.DataService;
import net.exemine.api.data.ExeData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.data.impl.FFAData;
import net.exemine.api.data.impl.HubData;
import net.exemine.api.data.impl.UHCData;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.callable.Callback;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.menu.Menu;
import net.exemine.core.settings.SettingsMenu;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.Material;

import java.util.Arrays;

public class SettingsTypeMenu extends Menu<CoreUser> {

    private final DataService dataService;
    private final SettingsType type;
    private final ExeData data;

    public SettingsTypeMenu(CoreUser user, SettingsType type, ExeData data) {
        super(user, CC.DARK_GRAY + type.getName() + " Settings", 4);
        this.dataService = user.getPlugin().getDataService();
        this.type = type;
        this.data = data;
        setAutoSurround(true);
        setUpdateAfterClick(getUpdateAfterClick(type.getInstanceType()));
    }

    @Override
    public void update() {
        if (data == null) {
            user.sendMessage(Lang.REPORT_TO_DEV);
            close();
            return;
        }
        addExitItem();
        addReturnItem(new SettingsMenu(user));

        // Core Data
        if (data instanceof CoreData) {
            CoreData coreData = (CoreData) data;
            if (type == SettingsType.GENERAL) {
                add(10, Material.PAPER, coreData.isPrivateMessages(), "Private Messages", "toggleprivatemessages", "Toggles the ability to", "receive private messages.");
                add(11, Material.NOTE_BLOCK, coreData.isMessagingSounds(), "Messaging Sounds", "togglesounds", "Toggles the ability to hear a sound", "upon receiving private messages.");
                add(12, Material.RED_ROSE, coreData.isParticles(), "Particles", "toggleparticles", "Toggles the ability to", "see particles.");
                add(13, Material.FIREBALL, coreData.isGameBroadcasts(), "Game Broadcasts", "togglegamebroadcasts", "Toggles the ability to", "see game broadcasts.");
                add(14, Material.BOOK_AND_QUILL, coreData.isServerTips(), "Server Tips", "toggleservertips", "Toggles the ability to", "see game broadcasts.");
                add(15, Material.DRAGON_EGG, coreData.isBossBar(), "Boss Bar", "togglebossbar", "Toggles the ability to", "see the boss bar.");

                if (user.isEqualOrAbove(RankType.STAFF)) {
                    CoreData.StaffData staffData = coreData.getStaffData();

                    add(16, Material.FLINT, staffData.isChatMessages(), "Staff Messages", "togglestaffmessages", "Toggles the ability to", "see staff chat messages.");
                    add(19, Material.SIGN, staffData.isServerSwitch(), "Server Switch", "togglestaffserverswitch", "Toggles the ability to", "see staff server switch", "messages.");
                    add(20, Material.REDSTONE, staffData.isReports(), "Reports", "togglestaffreports", "Toggles the ability to", "see player reports.");

                    if (user.isEqualOrAbove(Rank.SENIOR_MOD)) {
                        add(21, Material.ANVIL, staffData.isSocialSpy(), "Social Spy", "togglestaffsocialspy", "Toggles the ability to", "see private messages of players.");
                    }
                    if (user.isEqualOrAbove(Rank.DEVELOPER)) {
                        add(22, Material.NETHER_BRICK_ITEM, staffData.isInstanceAlerts(), "Instance Alerts", "toggleinstancealerts", "Toggle the ability to", "see instance alerts.");
                    }
                }
            } else if (type == SettingsType.LUNAR) {
                CoreData.LunarData lunarData = coreData.getLunarData();

                add(10, Material.SIGN, lunarData.isTitles(), "Lunar Titles", "toggletitles", "Toggles the ability to", "see Lunar Client titles.");
                add(11, Material.STAINED_GLASS_PANE, lunarData.isBorder(), "Lunar Border", "toggleborder", "Toggles the ability to", "see the Lunar Client border.");
                add(12, Material.SKULL_ITEM, 3, lunarData.isTeamView(), "Lunar Team View", "toggleteamview", "Toggles the ability to", "see the Lunar Client team mate marker.");
                add(13, Material.COMPASS, lunarData.isWaypoints(), "Lunar Waypoints", "togglewaypoints", "Toggles the ability to", "see Lunar Client waypoints.");
                add(14, Material.NAME_TAG, lunarData.isNametags(), "Lunar Nametags", "togglenametags", "Toggles the ability to", "see Lunar Client nametags.");
            }
        }
        // Hub Data
        else if (data instanceof HubData) {
            HubData hubData = (HubData) data;
            boolean onHub = InstanceUtil.isType(type.getInstanceType());

            add(10, Material.SLIME_BALL, hubData.isPlayerVisibility(), "Player Visibility", onHub, "togglevisibility", () -> {
                hubData.setPlayerVisibility(!hubData.isPlayerVisibility());
                user.sendMessage(CC.PURPLE + "[Hub] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(hubData.isPlayerVisibility()) + CC.GRAY + " see regular players.");
            }, "Toggles the ability to", "see regular players.");

            if (user.isEqualOrAbove(RankType.DONATOR)) {
                add(11, Material.FEATHER, hubData.isFlight(), "Flight", onHub, "togglefly", () -> {
                    hubData.setFlight(!hubData.isFlight());
                    user.sendMessage(CC.PURPLE + "[Perk] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(hubData.isFlight()) + CC.GRAY + " fly in the hub.");
                }, "Toggles the ability to", "fly in the hub.");
            }

            if (user.isEqualOrAbove(Rank.DEVELOPER)) {
                add(12, Material.GOLD_PICKAXE, hubData.isEdit(), "Edit Mode", onHub, "toggledit", () -> {
                    hubData.setEdit(!hubData.isEdit());
                    user.sendMessage(CC.PURPLE + "[Dev] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(hubData.isEdit()) + CC.GRAY + " edit the map in the hub.");
                }, "Toggles the ability to", "edit the map.");
            }
        }
        // UHC Data
        else if (data instanceof UHCData) {
            UHCData uhcData = (UHCData) data;
            boolean onUHC = InstanceUtil.isType(type.getInstanceType());

            add(10, Material.FLINT_AND_STEEL, uhcData.isDeathMessages(), "Death Messages", onUHC, "toggledeathmessages", () -> {
                uhcData.setDeathMessages(!uhcData.isDeathMessages());
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You can " + (uhcData.isDeathMessages() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see death messages.");
            }, "Toggles the ability to", "see death messages.");

            add(11, Material.GHAST_TEAR, uhcData.isShowSpectators(), "Spectators", onUHC, "togglespectators", () -> {
                uhcData.setShowSpectators(!uhcData.isShowSpectators());
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You can " + (uhcData.isShowSpectators() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see other spectators while spectating.");
            }, "Toggles the ability to", "see spectators.");

            if (user.isEqualOrAbove(RankType.STAFF)) {
                UHCData.StaffData staffData = uhcData.getStaffData();

                add(12, Material.DIAMOND, staffData.isXrayAlerts(), "Xray Alerts", onUHC, "togglexrayalerts", () -> {
                    staffData.setXrayAlerts(!staffData.isXrayAlerts());
                    user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isXrayAlerts() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see xray alerts while moderating.");
                }, "Toggles the ability to", "see xray alerts.");

                add(13, Material.PAPER, staffData.isHelpOpAlerts(), "Helpop Alerts", onUHC, "togglehelpopalerts", () -> {
                    staffData.setHelpOpAlerts(!staffData.isHelpOpAlerts());
                    user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isHelpOpAlerts() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see helpop alerts while moderating.");
                }, "Toggles the ability to", "see helpop alerts.");

                add(14, Material.BOOK_AND_QUILL, staffData.isHostChatMessages(), "Host Chat Messages", onUHC, "togtogglehostchatmessagesgle", () -> {
                    staffData.setHostChatMessages(!staffData.isHostChatMessages());
                    user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isHostChatMessages() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see host chat messages while moderating.");
                }, "Toggles the ability to", "see host chat messages.");

                add(15, Material.SULPHUR, staffData.isSpectatorChatMessages(), "Spectator Messages", onUHC, "togglestaffspectators", () -> {
                    staffData.setSpectatorChatMessages(!staffData.isSpectatorChatMessages());
                    user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isSpectatorChatMessages() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see spectator chat while moderating.");
                }, "Toggles the ability to", "see spectator messages.");

                add(16, Material.SADDLE, staffData.isShowGameModerators(), "Game Moderators", onUHC, "togglestaffspectators", () -> {
                    staffData.setShowGameModerators(!staffData.isShowGameModerators());
                    user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isShowGameModerators() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see other game moderators while spectating.");
                }, "Toggles the ability to", "see game moderators while", "spectating the match.");
            }
        }
        // FFA Data
        else if (data instanceof FFAData) {
            FFAData ffaData = (FFAData) data;
            FFAData.StaffData staffData = ffaData.getStaffData();
            boolean onFFA = InstanceUtil.isType(type.getInstanceType());

            add(10, Material.FLINT_AND_STEEL, ffaData.isDeathMessages(), "Death Messages", onFFA,
                "toggledeathmessages", () -> {
                ffaData.setDeathMessages(!ffaData.isDeathMessages());
                user.sendMessage(CC.PURPLE + "[FFA] " + CC.GRAY + "You can " + (ffaData.isDeathMessages() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see death messages.");
            }, "Toggles the ability to", "see death messages.");

            if (user.isEqualOrAbove(RankType.STAFF)) {
                add(11, Material.SADDLE, staffData.isShowGameModerators(), "Game Moderators", onFFA,
                    "togglestaffspectators", () -> {
                    staffData.setShowGameModerators(!staffData.isShowGameModerators());
                    user.sendMessage(CC.PURPLE + "[Staff] " + CC.GRAY + "You can " + (staffData.isShowGameModerators() ? CC.GREEN + "now" : CC.RED + "no longer") + CC.GRAY + " see other game moderators while spectating.");
                }, "Toggles the ability to see", "game moderators while spectating.");
            }
            if (user.isEqualOrAbove(Rank.DEVELOPER)) {
                add(12, Material.GOLD_PICKAXE, staffData.isEdit(), "Edit Mode", onFFA, "toggledit",
                    () -> {
                    staffData.setEdit(!staffData.isEdit());
                    user.sendMessage(CC.PURPLE + "[Dev] " + CC.GRAY + "You can " + StringUtil.formatBooleanCommand(staffData.isEdit()) + CC.GRAY + " edit the map.");
                }, "Toggles the ability to", "edit the map.");
            }
        }
    }

    private void add(int slot, Material material, int durability, boolean enabled, String name, boolean onInstance, String command, Callback callback, String... description) {
        set(slot, new ItemBuilder()
                .setMaterial(material)
                .setDurability(durability)
                .setName(CC.PINK + name)
                .setLore(lore -> {
                    Arrays.stream(description)
                            .map(line -> CC.GRAY + line)
                            .forEach(lore::add);
                    lore.add("");
                    lore.add(enabled
                            ? CC.RED + "Click to disable this option."
                            : CC.GREEN + "Click to enable this option."
                    );
                })
                .build()
        ).onClick(() -> {
            if (onInstance) {
                user.performCommand(command);
            } else {
                updateData(callback);
            }
        });
    }

    private void add(int slot, Material material, boolean enabled, String name, boolean onInstance, String command, Callback callback, String... description) {
        add(slot, material, 0, enabled, name, onInstance, command, callback, description);
    }

    private void add(int slot, Material material, int durability, boolean enabled, String name, String command, String... description) {
        add(slot, material, durability, enabled, name, true, command, Callback.EMPTY, description);
    }

    private void add(int slot, Material material, boolean enabled, String name, String command, String... description) {
        add(slot, material, enabled, name, true, command, Callback.EMPTY, description);
    }

    private void updateData(Callback callback) {
        Executor.schedule(() -> {
            callback.run();
            open();
            dataService.update(data);
        }).runAsync();
    }

    private boolean getUpdateAfterClick(InstanceType type) {
        return type == InstanceType.UNKNOWN || InstanceUtil.isType(type);
    }
}
