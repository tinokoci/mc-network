package net.exemine.core.user.base;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.exemine.api.data.ExeData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.util.string.DatabaseUtil;
import net.exemine.core.util.UserUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/***
 * @param <U> - User
 * @param <D> - Data
 */
@Getter
public class UserService<U extends ExeUser<D>, D extends ExeData> {

    private final Map<UUID, U> users = new ConcurrentHashMap<>();

    private final DatabaseService databaseService;
    private final Supplier<U> userSupplier;
    private final Supplier<D> dataSupplier;
    private final DatabaseCollection databaseCollection;

    public UserService(JavaPlugin plugin, DatabaseService databaseService, Supplier<U> userSupplier, Supplier<D> dataSupplier, DatabaseCollection databaseCollection) {
        this.databaseService = databaseService;
        this.userSupplier = userSupplier;
        this.dataSupplier = dataSupplier;
        this.databaseCollection = databaseCollection;

        if (plugin != null) {
            Bukkit.getPluginManager().registerEvents(new ExeListener<>(this), plugin);
        }
    }

    U instantiateUser(UUID uuid) {
        U user = userSupplier.get();
        user.setUniqueID(uuid);
        user.initialization();
        users.put(uuid, user);
        return user;
    }

    D instantiateData(UUID uuid) {
        D data = dataSupplier.get();
        data.setUniqueId(uuid);
        return data;
    }

    U create(UUID uuid) {
        U user = users.get(uuid);

        if (user == null) {
            user = instantiateUser(uuid);
        }
        return user;
    }

    /***
     * @return User if they're stored in memory or null otherwise
     */
    public U retrieve(UUID uuid) {
        return users.get(uuid);
    }

    public U retrieve(String name) {
        return values()
                .stream()
                .filter(user -> user.getDisplayName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public U retrieve(Player player) {
        return retrieve(player.getUniqueId());
    }

    /***
     * @return User if they're online on the server or null otherwise
     */
    public U get(UUID uuid) {
        if (UserUtil.isPlayerOffline(uuid)) {
            return null;
        }
        return retrieve(uuid);
    }

    public U get(String name) {
        if (UserUtil.isPlayerOffline(name)) {
            return null;
        }
        return get(Bukkit.getPlayer(name));
    }

    public U get(ExeUser<?> user) {
        return get(user.getUniqueId());
    }

    public U get(Player player) {
        return get(player.getUniqueId());
    }

    public U get(CommandSender sender) {
        return sender instanceof ConsoleCommandSender ? null : get((Player) sender);
    }

    /***
     * @return Optional of User if they're online, or if they exist in the database
     */
    public Optional<U> fetch(String name) {
        U user = retrieve(name);

        if (user == null) {
            Document document = databaseService.findOne(databaseCollection, Filters.eq(DatabaseUtil.QUERY_NAME_KEY, name.toLowerCase())).run();
            if (document == null) return Optional.empty();

            UUID uuid = UUID.fromString(document.getString(DatabaseUtil.PRIMARY_KEY));
            user = create(uuid);
        }
        user.loadData(false);
        return Optional.of(user);
    }

    /***
     * @return Optional of User if they're online, or if they exist in the database
     */
    public Optional<U> fetch(UUID uuid) {
        U user = get(uuid);

        if (user == null) {
            Document document = databaseService.findOne(databaseCollection, Filters.eq(DatabaseUtil.PRIMARY_KEY, uuid.toString())).run();
            if (document == null) return Optional.empty();

            user = create(uuid);
        }
        user.loadData(false);
        return Optional.of(user);
    }

    public Collection<U> values() {
        return map().values();
    }

    public Map<UUID, U> map() {
        return users;
    }

    public Collection<U> getOnlineUsers() {
        return values()
                .stream()
                .filter(ExeUser::isOnline)
                .collect(Collectors.toList());
    }
}