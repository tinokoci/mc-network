package net.exemine.hub.provider;

import net.exemine.api.data.impl.HubData;
import net.exemine.api.util.string.CC;
import net.exemine.core.provider.ChatProvider;
import net.exemine.hub.user.HubUser;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class HubChatProvider implements ChatProvider<HubUser, HubData> {

    @Override
    public void sendMessage(HubUser user, HubData data, AsyncPlayerChatEvent event) {
        if (user.getCoreUser().isPunishedAllowedConnectForLink()) {
            user.sendLinkRequiredMessage();
            return;
        }
        defaultSendMessage(user, user.getFullDisplayName() + CC.GRAY + ": " + CC.WHITE + event.getMessage());
    }
}
