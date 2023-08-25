package net.exemine.core.command.base;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.ExeData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.Executor;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public abstract class BaseCommand<U extends ExeUser<D>, D extends ExeData> extends BukkitCommand {

    public CommandService<U, D> commandService;
    public UserService<U, D> userService;

    private final Rank rank;
    private final boolean forUsersOnly;

    @Setter
    private boolean async = false;

    public BaseCommand(List<String> aliases, Rank rank, boolean forUsersOnly) {
        super(aliases.get(0));
        this.rank = rank;
        this.forUsersOnly = forUsersOnly;

        if (aliases.size() > 1) {
            setAliases(aliases.subList(1, aliases.size()));
        }
    }

    public BaseCommand(List<String> aliases, boolean forUsersOnly) {
        this(aliases, Rank.DEFAULT, forUsersOnly);
    }

    public BaseCommand(List<String> aliases, Rank rank) {
        this(aliases, rank, true);
    }

    public BaseCommand(List<String> aliases) {
        this(aliases, Rank.DEFAULT);
    }

    BaseCommand<U, D> setup(CommandService<U, D> commandService, UserService<U, D> userService) {
        this.commandService = commandService;
        this.userService = userService;
        return this;
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (sender instanceof Player) {
            U user = userService.get(sender);

            if (!user.isEqualOrAbove(rank)) {
                user.sendMessage(Lang.NO_PERMISSION);
                return true;
            }
        }
        if (forUsersOnly) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(CC.RED + "This command is set to player only.");
                return true;
            }
            U user = userService.get(sender);
            D data = user.getData();
            Executor.schedule(() -> execute(user, data, args)).run(async);
            return true;
        }
        Executor.schedule(() -> execute(sender, args)).run(async);
        return true;
    }

    public void execute(U user, D data, String[] args) {}

    public void execute(CommandSender sender, String[] args) {}

    public boolean isUserOffline(CommandSender sender, U target) {
        boolean isOffline = target == null || target.isOffline();
        if (isOffline) {
            sender.sendMessage(Lang.USER_NOT_FOUND);
        }
        return isOffline;
    }

    public boolean cannotAccessPerkBetween(U user, Rank minInclusive, Rank maxInclusive) {
        boolean canAccess = user.isEqualOrAbove(minInclusive) && !user.isAbove(maxInclusive);
        return cannotAccessPerk(user, canAccess);
    }

    private boolean cannotAccessPerk(U user, boolean canAccess) {
        boolean cannotAccess = !canAccess && !user.isEqualOrAbove(Rank.DEVELOPER);
        if (cannotAccess) {
            user.sendMessage(Lang.NO_PERMISSION);
        }
        return cannotAccess;
    }
}
