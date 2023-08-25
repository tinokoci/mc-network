package net.exemine.core.user;

import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.RankType;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.AlertStaffMessageModel;
import net.exemine.api.redis.pubsub.model.AlertStaffServerSwitchModel;
import net.exemine.api.redis.pubsub.model.AlertUHCAnnounceModel;
import net.exemine.api.redis.pubsub.model.generic.UUIDModel;
import net.exemine.core.user.base.UserService;
import net.exemine.core.util.spigot.Clickable;

public class UserSubscriber {

    private final RedisService redisService;
    private final UserService<CoreUser, CoreData> userService;

    public UserSubscriber(RedisService redisService, UserService<CoreUser, CoreData> userService) {
        this.redisService = redisService;
        this.userService = userService;

        subscribeToAlertStaffMessage();
        subscribeToStaffSwitchMessages();
        subscribeToAlertUHCAnnounce();
        subscribeToDataUpdate();
        subscribeToDiscordLinkRequest();
    }

    private void subscribeToAlertStaffMessage() {
        redisService.subscribe(RedisMessage.ALERT_STAFF_MESSAGE, AlertStaffMessageModel.class, model -> userService.getOnlineUsers()
                .stream()
                .filter(user -> user.isEqualOrAbove(model.getRank()) && user.getStaffData().isChatMessages())
                .forEach(staff -> staff.sendMessage(model.getMessage())));
    }

    private void subscribeToStaffSwitchMessages() {
        redisService.subscribe(RedisMessage.ALERT_STAFF_SERVER_SWITCH, AlertStaffServerSwitchModel.class, model -> userService.getOnlineUsers()
                .stream()
                .filter(user -> user.isEqualOrAbove(RankType.STAFF) && user.getStaffData().isServerSwitch())
                .forEach(staff -> staff.sendMessage(model.getMessage())));
    }

    private void subscribeToAlertUHCAnnounce() {
        redisService.subscribe(RedisMessage.ALERT_UHC_ANNOUNCE, AlertUHCAnnounceModel.class, model -> new Clickable()
                .add(model.getDescription())
                .add(model.getClickable(), model.getHover(), model.getAction())
                .broadcast());
    }

    private void subscribeToDataUpdate() {
        redisService.subscribe(RedisMessage.CORE_DATA_UPDATE, UUIDModel.class, model -> {
            CoreUser user = userService.get(model.getUniqueId());
            if (user != null) {
                user.loadData(true);
            }
        });
    }

    private void subscribeToDiscordLinkRequest() {
        redisService.subscribe(RedisMessage.DISCORD_LINK_REQUEST, UUIDModel.class, model -> {
            CoreUser user = userService.get(model.getUniqueId());
            if (user != null) {
                user.getDiscordLinkRequest(redisService);
            }
        });
    }
}
