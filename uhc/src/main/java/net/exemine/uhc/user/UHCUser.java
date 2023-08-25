package net.exemine.uhc.user;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.scatter.late.LateScatterTime;
import net.exemine.uhc.team.Team;
import net.exemine.uhc.user.event.UHCUserChangeStateEvent;
import net.exemine.uhc.user.info.AntiChugInfo;
import net.exemine.uhc.user.info.GameInfo;
import net.exemine.uhc.user.info.NoCleanInfo;
import net.exemine.uhc.user.info.RelogInfo;
import net.exemine.uhc.user.info.RespawnInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class UHCUser extends ExeUser<UHCData> {

    private final UHC plugin;
    private final GameService gameService;

    private final GameInfo gameInfo = new GameInfo();
    private RespawnInfo respawnInfo;
    private RelogInfo relogInfo;

    private final AntiChugInfo antiChugInfo = new AntiChugInfo();
    private NoCleanInfo noCleanInfo;

    private final List<ItemStack> burnedItems = new ArrayList<>();

    private Vector lastCheckedPosition;

    private UHCUserState state = UHCUserState.LOBBY;

    private boolean hostChat;
    private boolean spectatorChat;
    private boolean hostThanks;

    private boolean spectatorTeleportDelay;
    private String deathMessage;

    public UHCUser(UHC plugin, Class<UHCData> dataTypeClass, UserService<? extends ExeUser<UHCData>, UHCData> userService) {
        super(dataTypeClass, userService, plugin.getCore());
        this.plugin = plugin;
        this.gameService = plugin.getGameService();
    }

    @Override
    public void initialization() {
        noCleanInfo = new NoCleanInfo(getUniqueId());
    }

    public boolean inState(UHCUserState... states) {
        return Arrays.stream(states).anyMatch(state -> this.state == state);
    }

    public boolean notInState(UHCUserState... states) {
        return Arrays.stream(states).noneMatch(state -> this.state == state);
    }

    public void setState(UHCUserState state) {
        UHCUserChangeStateEvent event = new UHCUserChangeStateEvent(this, this.state);
        this.state = state;
        Bukkit.getPluginManager().callEvent(event);
    }

    public Team getTeam() {
        return plugin.getTeamService().getTeam(this);
    }

    public boolean inTeamWithTeammates() {
        return getTeam() != null && getTeam().getSize() > 1;
    }

    public boolean isSpectatorChat() {
        return isEqualOrAbove(RankType.STAFF) && spectatorChat;
    }

    public boolean isHostChat() {
        return isEqualOrAbove(RankType.STAFF) && hostChat;
    }

    public boolean isGameModerator() {
        return inState(UHCUserState.MODERATOR, UHCUserState.SUPERVISOR, UHCUserState.HOST);
    }

    public boolean isRegularSpectator() {
        return inState(UHCUserState.SPECTATOR);
    }

    public boolean isSpectating() {
        return isGameModerator() || isRegularSpectator();
    }

    public boolean isPlaying() {
        return inState(UHCUserState.IN_GAME);
    }

    public boolean isScattering() {
        return inState(UHCUserState.SCATTER);
    }

    public boolean isInPractice() {
        return inState(UHCUserState.PRACTICE);
    }

    public boolean isInLobby() {
        return inState(UHCUserState.LOBBY);
    }

    public boolean isWaiting() {
        return isInLobby() || isInPractice();
    }

    public boolean canLateScatter() {
        return System.currentTimeMillis() - gameService.getStartTime() < LateScatterTime.get(getRank());
    }

    public void sendHostMessage(String chatMessage) {
        String message = CC.GOLD + "[Host Chat] " + state.getStaffLongPrefix() + CC.GRAY + getRealName() + ": " + CC.YELLOW + chatMessage;
        plugin.getUserService().getModAndHostUsers()
                .stream()
                .filter(modOrHost -> modOrHost.getStaffData().isHostChatMessages())
                .forEach(modOrHost -> modOrHost.sendMessage(message));
    }

    public UHCData.StaffData getStaffData() {
        return getData().getStaffData();
    }

    public void setCustomDeathMessage(String deathMessage) {
        setLastDamageCause(new EntityDamageEvent(null, EntityDamageEvent.DamageCause.CUSTOM, 0));
        this.deathMessage = deathMessage;
    }

    public boolean hasRespawnInfo() {
        return respawnInfo != null;
    }

    public void updateRespawnInfo() {
        setRespawnInfo(new RespawnInfo(
                getLocation(),
                getInventory().getContents(),
                getInventory().getArmorContents(),
                getExp(),
                getLevel()
        ));
    }

    public void applyRelogInfo() {
        if (relogInfo == null) {
            sendMessage(CC.RED + "Couldn't apply your relog info, contact a developer.");
            return;
        }
        PlayerInventory inventory = getInventory();

        inventory.setContents(relogInfo.getInventory());
        inventory.setArmorContents(relogInfo.getArmor());
        teleport(relogInfo.getLocation());
        setFallDistance(relogInfo.getFallDistance());
        setHealth(relogInfo.getHealth());
        setFoodLevel(relogInfo.getFoodLevel());
        setSaturation(relogInfo.getSaturation());
        setLevel(relogInfo.getLevel());
        setExp(relogInfo.getExp());
        setFireTicks(relogInfo.getFireTicks());
        relogInfo.getActivePotionEffects().forEach(this::addPotionEffect);
        setNoDamageTicks(0);
        setInvulnerableTicks(0);
        setGameMode(GameMode.SURVIVAL);
        setFlying(false);
        setAllowFlight(false);
    }

    public void saveRelogInfo() {
        PlayerInventory inventory = getInventory();
        setRelogInfo(new RelogInfo(
                getLocation(),
                inventory.getContents(),
                inventory.getArmorContents(),
                getFallDistance(),
                getHealth(),
                getFoodLevel(),
                getSaturation(),
                getExp(),
                getLevel(),
                getFireTicks(),
                getActivePotionEffects()
        ));
    }

    public void updateMatchInventory() {
        UHCData data = getData();
        PlayerInventory inventory = getInventory();
        long matchStartTime = gameService.getMatch().getStartTime();

        data.getItemContents().put(matchStartTime, ItemUtil.serializeItemArray(inventory.getContents()));
        data.getArmorContents().put(matchStartTime, ItemUtil.serializeItemArray(inventory.getArmorContents()));
    }

    public UHCUser getGameKiller(boolean nullifyLogger) {
        Player bukkitKiller = super.getKiller();
        if (bukkitKiller != null) {
            return plugin.getUserService().get(bukkitKiller);
        }
        GameInfo gameInfo = getGameInfo();
        if (gameInfo.getLoggerKillerUuid() != null) {
            UHCUser killer = plugin.getUserService().get(gameInfo.getLoggerKillerUuid());
            if (nullifyLogger) gameInfo.setLoggerKillerUuid(null);
            return killer;
        }
        return null;
    }

    public void spawnHead(Location location) {
        Block fenceBlock = location.getBlock();
        Block skullBlock = fenceBlock.getRelative(BlockFace.UP);

        fenceBlock.setType(Material.NETHER_FENCE);
        skullBlock.setType(Material.SKULL);

        Skull skull = (Skull) skullBlock.getState();
        skull.setOwner(getDisplayName());
        skull.update();
        skullBlock.setData((byte) 1);
    }

    public boolean isInNether() {
        return inWorld(plugin.getWorldService().getNetherWorld());
    }

    public boolean canExecuteModCommand() {
        return isGameModerator() || isEqualOrAbove(Rank.ADMIN);
    }
}
