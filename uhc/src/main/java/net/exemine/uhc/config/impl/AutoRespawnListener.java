package net.exemine.uhc.config.impl;

import net.exemine.api.punishment.Punishment;
import net.exemine.api.punishment.PunishmentType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.punishment.event.PunishmentEvent;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.config.ConfigListener;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserState;
import org.bukkit.event.EventHandler;

import java.util.Collection;
import java.util.stream.Collectors;

public class AutoRespawnListener extends ConfigListener {

    @EventHandler
    public void onPunishment(PunishmentEvent event) {
        Punishment punishment = event.getPunishment();
        if (!punishment.getType().isOrGreaterThan(PunishmentType.BAN)) return;

        UHCUser user = plugin.getUserService().retrieve(event.getPunishment().getUuid());
        if (user == null) return;

        if (user.isPlaying()) {
            Collection<UHCUser> killedUsers = user.getGameInfo().getKilledUsers().values()
                    .stream()
                    .map(uuid -> plugin.getUserService().retrieve(uuid))
                    .collect(Collectors.toList());

            killedUsers.forEach(killedUser -> {
                if (killedUser.isPlaying()) return;
                if (killedUser.isOffline()) {
                    killedUser.getGameInfo().setKilledByBannedUser(true);
                    return;
                }
                Executor.schedule(() -> killedUser.setState(UHCUserState.IN_GAME)).runSync();
                killedUser.sendMessage(CC.GREEN + "You've been respawned because the person that killed you got " + punishment.getType().getFormat() + '.');
            });
            MessageUtil.send("");
            MessageUtil.send(CC.PINK + "The following player" + (killedUsers.size() == 1 ? "" : 's') + " have been respawned because " + user.getColoredDisplayName() + CC.PINK + " was " + punishment.getType().getFormat() + ':');
            killedUsers.forEach(killedUser -> MessageUtil.send(' ' + CC.GRAY + Lang.BULLET + ' ' + killedUser.getColoredDisplayName()));
            MessageUtil.send("");
        }
    }
}
