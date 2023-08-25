package net.exemine.core.lunar;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketNametagsOverride;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketNametagsUpdate;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTeammates;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketTitle;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketUpdateWorld;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketWorldBorder;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointAdd;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointRemove;
import lombok.Setter;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.Executor;
import net.exemine.core.Core;
import net.exemine.core.user.CoreUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.entity.Player;

import java.util.Set;

public abstract class LunarModule {

    private final UserService<CoreUser, CoreData> userService = Core.get().getUserService();
    protected final LunarClientAPI api = LunarClientAPI.getInstance();

    @Setter
    private boolean force;

    public void send(Player player) {
        Executor.schedule(() -> {
            if (player == null || !player.isOnline() || !api.isRunningLunarClient(player)) return;
            CoreUser user = userService.get(player);
            CoreData.LunarData data = user.getData().getLunarData();

            getPackets().forEach(packet -> {
                if (!force) {
                    if (packet instanceof LCPacketTitle && !data.isTitles()) return;
                    if ((packet instanceof LCPacketWorldBorder || packet instanceof LCPacketUpdateWorld) && !data.isBorder()) return;
                    if (packet instanceof LCPacketTeammates && !data.isTeamView()) return;
                    if ((packet instanceof LCPacketWaypointAdd || packet instanceof LCPacketWaypointRemove) && !data.isWaypoints()) return;
                    if ((packet instanceof LCPacketNametagsOverride || packet instanceof LCPacketNametagsUpdate) && !data.isNametags()) return;
                }
                api.sendPacket(player, packet);
                if (packet instanceof LCPacketWorldBorder) {
                    Executor.schedule(() -> api.sendPacket(player, new LCPacketUpdateWorld(((LCPacketWorldBorder) packet).getWorld())))
                            .runSyncLater(50);
                }
            });
        }).runSyncLater(250L);
    }

    protected abstract Set<LCPacket> getPackets();
}
