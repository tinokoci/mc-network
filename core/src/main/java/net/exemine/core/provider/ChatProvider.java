package net.exemine.core.provider;

import net.exemine.api.data.ExeData;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.user.base.ExeUser;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public interface ChatProvider<U extends ExeUser<D>, D extends ExeData> {

    void sendMessage(U user, D data, AsyncPlayerChatEvent event);

    default void defaultSendMessage(U user, String userFormat) {
        Core.get().getUserService().getOnlineUsers()
                .stream()
                .filter(target -> !target.getData().isIgnoring(user.getUniqueId()) || user.isEqualOrAbove(RankType.STAFF))
                .forEach(target -> target.sendMessage(userFormat));
    }

    class DefaultChatProvider<U extends ExeUser<D>, D extends ExeData> implements ChatProvider<U, D> {

        @Override
        public void sendMessage(U user, D data, AsyncPlayerChatEvent event) {
            defaultSendMessage(user,  user.getFullDisplayName() + CC.GRAY + ": " + CC.WHITE + event.getMessage());
        }
    }
}
