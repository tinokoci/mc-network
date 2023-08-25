package net.exemine.discord.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.util.callable.TypeCallback;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.discord.Discord;
import net.exemine.discord.util.DiscordUtil;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Getter
@Setter
public class NetworkUser {

    private static final Map<User, NetworkUser> USERS = new ConcurrentHashMap<>();

    private final User user;

    private final AtomicReference<CoreData> coreData = new AtomicReference<>(new CoreData());
    private final AtomicReference<BulkData> bulkData = new AtomicReference<>();

    public static NetworkUser getOrCreate(User user, TypeCallback<NetworkUser> success) {
        NetworkUser networkUser = USERS.get(user);

        if (networkUser == null) {
            networkUser = new NetworkUser(user);
            USERS.put(user, networkUser);
        }
        success.run(networkUser);
        return networkUser;
    }

    public static NetworkUser getOrCreate(User user) {
        return getOrCreate(user, TypeCallback.EMPTY());
    }

    public void loadBulkData() {
        loadCoreData().ifPresent(coreData -> {
            BulkData bulkData = Discord.get().getBulkDataService().getOrCreate(coreData.getUniqueId());
            Discord.get().getBulkDataService().loadData(bulkData);
            this.bulkData.set(bulkData);
        });
    }

    public Optional<CoreData> loadCoreData() {
        Optional<CoreData> optionalCoreData = Discord.get().getDataService().fetch(CoreData.class, DatabaseUtil.DISCORD_KEY, user.getId());
        optionalCoreData.ifPresent(coreData::set);
        return optionalCoreData;
    }

    public CoreData getCoreData() {
        return coreData.get();
    }

    public BulkData getBulkData() {
        return bulkData.get();
    }

    public boolean isAbove(BulkData bulkData) {
        return getBulkData().getRank().isAbove(bulkData.getRank());
    }

    public Member getMember() {
        return DiscordUtil.getGuild().getMember(user);
    }
}