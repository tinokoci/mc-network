package net.exemine.core.user.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.cosmetic.tag.Tag;
import net.exemine.api.data.ExeData;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.instance.Instance;
import net.exemine.api.instance.InstanceType;
import net.exemine.api.rank.Rank;
import net.exemine.api.rank.RankType;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.core.Core;
import net.exemine.core.cosmetic.color.ColorType;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.InstanceUtil;
import net.exemine.core.util.spigot.PlayerImpl;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@RequiredArgsConstructor
public abstract class ExeUser<T extends ExeData> extends PlayerImpl {

    @Getter(AccessLevel.NONE)
    private UUID uuid;

    private final AtomicReference<T> data = new AtomicReference<>();
    private final AtomicReference<BulkData> bulkData = new AtomicReference<>();

    private final Class<T> dataTypeClass;
    private final UserService<? extends ExeUser<T>, T> userService;
    private final Core core;

    public void saveData(boolean async) {
        Executor.schedule(() -> core.getDataService().update(getData())).run(async);
    }

    public void loadData(boolean async) {
        Executor.schedule(() -> {
            core.getDataService().fetch(dataTypeClass, getUniqueId()).ifPresentOrElse(
                    data::set,
                    () -> data.set(userService.instantiateData(getUniqueId()))
            );
            // Make sure not to create multiple BulkData instances for the same user
            // Also load bulk data only if load is executed on CoreUser since that's the main user object
            boolean isCoreUser = this instanceof CoreUser;
            if (getBulkData() == null) {
                bulkData.set(core.getBulkDataService().getOrCreate(getUniqueId()));
            }
            if (isCoreUser) {
                core.getBulkDataService().loadData(getBulkData());
            } else if (isOffline()) { // Just in case we need to access CoreData
                core.getUserService().fetch(uuid);
            }
        }).run(async);
    }

    public void initialization() {}

    public void onConnect(AsyncPlayerPreLoginEvent event) {
        getData().setName(event.getName());
    }

    public void onJoin() {}

    public void onQuit() {}

    public UUID getUniqueId() {
        return uuid;
    }

    void setUniqueID(UUID uuid) {
        this.uuid = uuid;
    }

    public T getData() {
        return data.get();
    }

    public BulkData getBulkData() {
        return bulkData.get();
    }

    public CoreUser getCoreUser() {
        return core.getUserService().retrieve(this);
    }

    public CoreData getCoreData() {
        return getCoreUser().getData();
    }

    public Rank getRank() {
        return getBulkData().getRank();
    }

    public boolean isEqualOrAbove(Rank rank) {
        return getRank().isEqualOrAbove(rank);
    }

    public boolean isEqualOrAbove(ExeUser<?> user) {
        return isEqualOrAbove(user.getRank());
    }

    public boolean isEqualOrAbove(RankType rankType) {
        return getRank().isEqualOrAbove(rankType);
    }

    public boolean isAbove(Rank rank) {
        return getRank().isAbove(rank);
    }

    public boolean isAbove(ExeUser<?> user) {
        return isAbove(user.getRank());
    }

    public boolean isEqual(Rank rank) {
        return getRank().equals(rank);
    }

    public boolean isEqual(RankType rankType) {
        return getRank().isEqual(rankType);
    }

    public boolean isEqual(ExeUser<?> user) {
        return isEqual(user.getRank());
    }

    public boolean hasRankBetween(Rank minInclusive, Rank maxInclusive) {
        return getBulkData().getActiveRankInfoList()
                .stream()
                .anyMatch(rankInfo -> rankInfo.getRank().isEqualOrAbove(minInclusive) && !rankInfo.getRank().isAbove(maxInclusive));
    }

    public boolean hasNoRank() {
        return isEqual(Rank.DEFAULT);
    }

    public boolean hasPermission(String permission) {
        return getBulkData().hasPermission(permission);
    }

    public boolean isDisguised() {
        return getBulkData().getDisguiseModel() != null;
    }

    public String getRealName() {
        return getData().getName();
    }

    public String getColoredRealName() {
        return getRank().getColor() + getRealName();
    }

    public String getFullRealName() {
        return getFormattedTag() + getRank().getPrefix() + getFormattedColorType() + getRealName();
    }

    public String getDisplayName() {
        if (isDisguised()) {
            return getBulkData().getDisguiseModel().getName();
        }
        return getRealName();
    }

    public String getColoredDisplayName() {
        if (isDisguised()) {
            return Rank.DEFAULT.getColor() + getDisplayName();
        }
        return getColoredRealName();
    }

    public String getFullDisplayName() {
        if (isDisguised()) {
            return Rank.DEFAULT.getPrefix() + getDisplayName();
        }
        return getFullRealName();
    }

    public String getFormattedTag() {
        Tag tag = getCoreUser().getTag();
        return tag == null ? "" : CC.WHITE + CC.translate(tag.getFormat()) + ' ';
    }

    public String getFormattedColorType() {
        ColorType color = getCoreUser().getColorType();
        return color == null ? "" : color.getFormat().toString();
    }

    public TimeZone getTimeZone() {
        return getBulkData().getTimeZone();
    }

    public boolean sendToInstance(Instance instance) {
        if (instance == null
                || instance.isOffline()
                || isOffline()
                || !InstanceUtil.canJoin(this, instance))
            return false;

        return sendToServer(instance.getName());
    }

    public boolean sendToHub(String message) {
        Instance hub = core.getInstanceService().getAllInstances(InstanceType.HUB)
                .stream()
                .filter(instance -> InstanceUtil.canJoin(this, instance)
                        && InstanceUtil.getCurrent() != instance)
                .findFirst()
                .orElse(null);

        boolean sent = sendToInstance(hub);
        if (sent && message != null) {
            sendMessage(message);
        }
        return sent;
    }

    public boolean sendToHub() {
        return sendToHub(null);
    }

    public void instantNametagRefresh() {
        NametagService<?> nametagService = NametagService.get();
        if (nametagService != null) {
            nametagService.setupNametagsOf(getUniqueId());
        }
    }
}
