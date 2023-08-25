package net.exemine.core.command.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.ExeData;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.user.base.UserService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public final class CommandService<U extends ExeUser<D>, D extends ExeData> {

    private final JavaPlugin plugin;
    private final UserService<U, D> userService;

    // This is static because this is the simplest way to access all
    // commands from every module through a single list
    @Getter
    private static final List<BaseCommand<?, ?>> baseCommandList = new ArrayList<>();

    @SafeVarargs
    public final void register(BaseCommand<U, D>... commands) {
        String fallbackPrefix = plugin.getDescription().getName();
        Arrays.stream(commands).forEach(command -> {
            BaseCommand<U, D> baseCommand = command.setup(this, userService);
            Bukkit.getCommandMap().register(fallbackPrefix, baseCommand);
            baseCommandList.add(baseCommand);
        });
    }

    public boolean canExecute(ExeUser<?> user, String command) {
        BaseCommand<?, ?> baseCommand = baseCommandList
                .stream()
                .filter(c -> command.startsWith(c.getName()) || c.getAliases().stream().anyMatch(command::startsWith))
                .findFirst()
                .orElse(null);
        if (baseCommand == null) return true;
        return user.isEqualOrAbove(baseCommand.getRank());
    }
}
