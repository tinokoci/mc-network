package net.exemine.uhc.user;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.core.user.base.UserService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UHCUserService extends UserService<UHCUser, UHCData> {

    public UHCUserService(JavaPlugin plugin, DatabaseService databaseService, Supplier<UHCUser> userSupplier, Supplier<UHCData> dataSupplier, DatabaseCollection databaseCollection) {
        super(plugin, databaseService, userSupplier, dataSupplier, databaseCollection);
    }

    public List<UHCUser> getWaitingUsers() {
        return getOnlineUsers(UHCUserState.LOBBY, UHCUserState.PRACTICE);
    }

    public List<UHCUser> getPracticeUsers() {
        return getOnlineUsers(UHCUserState.PRACTICE);
    }

    public List<UHCUser> getAllScatterUsers() {
        return getAllUsers(UHCUserState.SCATTER);
    }

    public List<UHCUser> getInGameUsers() {
        return getOnlineUsers(UHCUserState.IN_GAME);
    }

    public List<UHCUser> getSpectatorUsers() {
        return getOnlineUsers(UHCUserState.SPECTATOR, UHCUserState.SUPERVISOR, UHCUserState.MODERATOR, UHCUserState.HOST);
    }

    public List<UHCUser> getModAndHostUsers() {
        return getOnlineUsers(UHCUserState.MODERATOR, UHCUserState.SUPERVISOR, UHCUserState.HOST);
    }

    public List<UHCUser> getHostUsers() {
        return values()
                .stream()
                .filter(user -> user.inState(UHCUserState.HOST))
                .collect(Collectors.toList());
    }

    public List<UHCUser> getSupervisorUsers() {
        return values()
                .stream()
                .filter(user -> user.inState(UHCUserState.SUPERVISOR))
                .collect(Collectors.toList());
    }

    public List<UHCUser> getModUsers() {
        return getOnlineUsers(UHCUserState.MODERATOR);
    }

    private List<UHCUser> getOnlineUsers(UHCUserState... states) {
        return getOnlineUsers()
                .stream()
                .filter(user -> user.inState(states))
                .collect(Collectors.toList());
    }

    private List<UHCUser> getAllUsers(UHCUserState... states) {
        return values()
                .stream()
                .filter(user -> user.inState(states))
                .collect(Collectors.toList());
    }
}
