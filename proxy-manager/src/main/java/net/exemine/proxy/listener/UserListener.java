package net.exemine.proxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.model.Channel;
import net.exemine.api.properties.Properties;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.proxy.Proxy;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserListener {

    private final Proxy plugin;

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        Properties properties = plugin.getPropertiesService().getProperties();
        ServerPing.Builder builder = event.getPing().asBuilder();
        String description = CC.translate(properties.getMotd().getCombined());

        builder.description(Component.text(description));
        builder.onlinePlayers(plugin.getInstanceService().getOnlinePlayers());

        if (properties.isMaintenance()) {
            builder.version(new ServerPing.Version(-1, "Whitelisted"));
        }
        event.setPing(builder.build());
    }

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        RegisteredServer targetServer = event.getServer();
        Optional<RegisteredServer> previousServer = event.getPreviousServer();

        Executor.schedule(() -> {
            BulkData bulkData = plugin.getBulkDataService().getOrCreate(player.getUniqueId(), data -> plugin.getBulkDataService().loadRanks(data));
            Rank rank = bulkData.getRank();
            Channel channel = Channel.STAFF;

            if (!rank.isEqualOrAbove(channel.getRank())) {
                return;
            }
            String serverName = targetServer.getServerInfo().getName();
            String userName = rank.getColor() + player.getUsername();
            String message = previousServer
                    .map(registeredServer -> CC.GRAY + " joined " + channel.getValueColor() + serverName + CC.GRAY + " from " + channel.getValueColor() + registeredServer.getServerInfo().getName() + CC.GRAY + '.')
                    .orElseGet(() -> CC.GRAY + " has connected to " + channel.getValueColor() + serverName + CC.GRAY + '.');

            plugin.getRedisService().getPublisher().sendAlertStaffServerSwitch(uuid, channel.format(userName, message));
        }).runAsync();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Optional<ServerConnection > serverConnection = event.getPlayer().getCurrentServer();
        if (!serverConnection.isPresent()) return;

        ServerInfo lastServerInfo = serverConnection.get().getServer().getServerInfo();

        Executor.schedule(() -> {
            BulkData bulkData = plugin.getBulkDataService().getOrCreate(player.getUniqueId(), data -> plugin.getBulkDataService().loadRanks(data));
            Rank rank = bulkData.getRank();
            Channel channel = Channel.STAFF;

            if (!rank.isEqualOrAbove(channel.getRank())) {
                return;
            }
            String name = rank.getColor() + player.getUsername();
            String message = CC.GRAY + " has disconnected from " + channel.getValueColor() + lastServerInfo.getName() + CC.GRAY + '.';

            plugin.getRedisService().getPublisher().sendAlertStaffServerSwitch(uuid, channel.format(name, message));
        }).runAsync();
    }
}
