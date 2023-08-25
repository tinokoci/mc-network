package net.exemine.uhc.command.staff;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.Executor;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.util.MessageUtil;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.BorderRadius;
import net.exemine.uhc.border.BorderService;
import net.exemine.uhc.border.task.BorderShrinkTask;
import net.exemine.uhc.config.menu.ConfigEditMenu;
import net.exemine.uhc.config.option.NumberOption;
import net.exemine.uhc.game.GameService;
import net.exemine.uhc.game.GameState;
import net.exemine.uhc.logger.CombatLogger;
import net.exemine.uhc.logger.CombatLoggerService;
import net.exemine.uhc.scatter.ScatterService;
import net.exemine.uhc.scenario.menu.ScenarioEditMenu;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.user.UHCUserService;
import net.exemine.uhc.user.UHCUserState;
import net.exemine.uhc.world.WorldService;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UHCCommand extends BaseCommand<UHCUser, UHCData> {

    private final UHC plugin;
    private final BorderService borderService;
    private final GameService gameService;
    private final CombatLoggerService combatLoggerService;
    private final ScatterService scatterService;
    private final UHCUserService userService;
    private final WorldService worldService;

    public UHCCommand(UHC plugin) {
        super(List.of("uhc", "game"), Rank.TRIAL_MOD);
        this.plugin = plugin;
        this.borderService = plugin.getBorderService();
        this.gameService = plugin.getGameService();
        this.combatLoggerService = plugin.getCombatLoggerService();
        this.scatterService = plugin.getScatterService();
        this.userService = plugin.getUserService();
        this.worldService = plugin.getWorldService();
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (args.length < 1) {
            sendUsage(user);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "start":
                if (gameService.isNotState(GameState.LOBBY) || !user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                user.sendMessage(CC.BOLD_GREEN + "You've started the game.");
                int countdown = args.length > 1 && StringUtil.isInteger(args[1])
                        ? Math.min(Integer.parseInt(args[1]), 60)
                        : 60;
                scatterService.startScatter(countdown);
                break;
            case "host":
                if (user.isPlaying() || user.isInPractice()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                boolean force = args.length == 2 && args[1].equalsIgnoreCase("force");

                if (!force && args.length != 1) {
                    sendUsage(user);
                    return;
                }
                UHCUserState disabledState = gameService.isStateOrHigher(GameState.SCATTERING) ? UHCUserState.SPECTATOR : UHCUserState.LOBBY;
                UHCUser currentHost = gameService.getHost();
                if (currentHost != null && currentHost != user) {
                    if (!force) {
                        user.sendMessage(currentHost.getColoredRealName() + CC.RED + " is already set as the game host.");
                        return;
                    }
                    currentHost.setState(disabledState);
                    if (currentHost.isOnline()) {
                        currentHost.sendMessage(user.getColoredRealName() + CC.RED + " has forcefully replaced you as the game host.");
                    }
                }
                user.setState(user.inState(UHCUserState.HOST) ? disabledState : UHCUserState.HOST);
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You're " + StringUtil.formatBooleanCommand(user.inState(UHCUserState.HOST)) + CC.GRAY + " the game " + CC.GOLD + "host" + CC.GRAY + '.');
                break;
            case "supervisor":
                if (user.isPlaying() || user.isInPractice()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (user.isEqual(Rank.TRIAL_MOD)) {
                    user.sendMessage(CC.RED + "You're not allowed to supervise games.");
                    return;
                }
                force = args.length == 2 && args[1].equalsIgnoreCase("force");

                if (!force && args.length != 1) {
                    sendUsage(user);
                    return;
                }
                disabledState = gameService.isStateOrHigher(GameState.SCATTERING) ? UHCUserState.SPECTATOR : UHCUserState.LOBBY;
                UHCUser currentSupervisor = gameService.getSupervisor();
                if (currentSupervisor != null && currentSupervisor != user) {
                    if (!force) {
                        user.sendMessage(currentSupervisor.getColoredRealName() + CC.RED + " is already set as the game supervisor.");
                        return;
                    }
                    currentSupervisor.setState(disabledState);
                    if (currentSupervisor.isOnline()) {
                        currentSupervisor.sendMessage(user.getColoredRealName() + CC.RED + " has forcefully replaced you as the game supervisor.");
                    }
                }
                user.setState(user.inState(UHCUserState.SUPERVISOR) ? disabledState : UHCUserState.SUPERVISOR);
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You're " + StringUtil.formatBooleanCommand(user.inState(UHCUserState.SUPERVISOR)) + CC.GRAY + " the game " + CC.GOLD + "supervisor" + CC.GRAY + '.');
                break;
            case "mod":
                if (user.isPlaying() || user.isInPractice()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 1) {
                    sendUsage(user);
                    return;
                }
                disabledState = gameService.isStateOrHigher(GameState.SCATTERING) ? UHCUserState.SPECTATOR : UHCUserState.LOBBY;
                user.setState(user.inState(UHCUserState.MODERATOR) ? disabledState : UHCUserState.MODERATOR);
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You're " + StringUtil.formatBooleanCommand(user.inState(UHCUserState.MODERATOR)) + CC.GRAY + " the game " + CC.GOLD + "moderator" + CC.GRAY + '.');
                break;
            case "heal":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                UHCUser target = userService.get(args[1]);
                if (isUserOffline(user, target)) return;

                if (!target.isPlaying()) {
                    user.sendMessage(CC.RED + "That player isn't playing this game.");
                    return;
                }
                target.setHealth(20.0D);
                target.setFoodLevel(20);
                target.setSaturation(20.0f);
                target.sendMessage(CC.GREEN + "You've been healed.");
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've healed " + target.getColoredDisplayName() + CC.GRAY + '.');
                break;
            case "latescatter":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                target = userService.get(args[1]);
                if (isUserOffline(user, target)) return;

                if (target.isPlaying()) {
                    user.sendMessage(CC.RED + "That player is already playing this game.");
                    return;
                }
                if (target.getGameInfo().isDied()) {
                    user.sendMessage(CC.RED + "That player has played and died in this game, respawn them instead.");
                    return;
                }
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've late scattered " + target.getColoredDisplayName() + CC.GRAY + '.');
                gameService.setInitialPlayers(gameService.getInitialPlayers() + 1);
                target.setState(UHCUserState.SCATTER);
                Executor.schedule(() -> {
                    target.setState(UHCUserState.IN_GAME);
                    target.sendMessage(CC.GREEN + "You've been late scattered.");
                }).runSyncLater(3000L);
                break;
            case "respawn":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                target = userService.get(args[1]);
                if (isUserOffline(user, target)) return;

                if (target.isPlaying()) {
                    user.sendMessage(CC.RED + "That player is already playing this game.");
                    return;
                }
                if (!target.getGameInfo().isDied()) {
                    user.sendMessage(CC.RED + "That player hasn't played in this game.");
                    return;
                }
                if (user != target) {
                    user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've respawned " + target.getColoredDisplayName() + CC.GRAY + '.');
                }
                target.setState(UHCUserState.IN_GAME);
                target.sendMessage(CC.GREEN + "You've been respawned.");
                break;
            case "remove":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                target = userService.get(args[1]);
                if (isUserOffline(user, target)) return;

                if (!target.isPlaying()) {
                    user.sendMessage(CC.RED + "That player is not playing this game.");
                    return;
                }
                target.setCustomDeathMessage("<victim> " + CC.GRAY + "has been eliminated from the game.");
                target.setHealth(0); // fires death event
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've eliminated " + target.getColoredDisplayName() + CC.GRAY + '.');
                target.sendMessage(CC.RED + "You've been eliminated from the game.");
                break;
            case "forceshrink":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (args.length != 2) {
                    sendUsage(user);
                    return;
                }
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                BorderShrinkTask borderShrinkTask = borderService.getBorderShrinkTask();

                if (borderShrinkTask == null) {
                    user.sendMessage(CC.RED + "The border shrink process has not yet started. If you need to speed up the process, change the value in game options.");
                    return;
                }
                if (!StringUtil.isInteger(args[1])) {
                    sendUsage(user);
                    return;
                }
                int inputRadius = Integer.parseInt(args[1]);
                BorderRadius currentRadius = borderService.getCurrentRadius();
                BorderRadius newRadius = Arrays.stream(BorderRadius.values())
                        .filter(borderRadius -> borderRadius.getValue() == inputRadius)
                        .findFirst()
                        .orElse(null);

                if (newRadius == null) {
                    user.sendMessage(CC.RED + "Available radius sizes: " + Arrays.stream(BorderRadius.values()).map(borderRadius -> String.valueOf(borderRadius.getValue())).collect(Collectors.joining(", ")));
                    return;
                }
                if (inputRadius >= currentRadius.getValue()) {
                    user.sendMessage(CC.RED + "You cannot shrink the border to the same or higher value than it's current.");
                    return;
                }
                borderShrinkTask.cancel();
                borderService.setFormattedShrinkIn(null);
                borderService.setBorderShrinkTask(new BorderShrinkTask(plugin, newRadius, 60));

                MessageUtil.send(CC.BOLD_GOLD + "[Border] " + CC.GRAY + "The border has been manually set to shrink to " + CC.WHITE + inputRadius + CC.GRAY + " radius.");
                break;
            case "offline":
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                if (combatLoggerService.getLoggers().isEmpty()) {
                    user.sendMessage(CC.RED + "There are no alive combat loggers.");
                    return;
                }
                user.sendMessage(CC.STRIKETHROUGH_GRAY + "------------------------------");
                user.sendMessage(CC.GOLD + "Lists of living combat loggers:");
                user.sendMessage();
                combatLoggerService.getLoggers().values()
                        .stream()
                        .sorted(Comparator.comparingLong(CombatLogger::getTimestamp))
                        .forEach(logger -> {
                            String formattedDiesIn = TimeUtil.getNormalDuration(System.currentTimeMillis() + NumberOption.RELOG_TIME.getMinutesInMillis() - logger.getTimestamp());
                            user.sendMessage(' ' + CC.GRAY + Lang.BULLET + ' ' + logger.getEntity().getCustomName() + ' ' + CC.GRAY + "(dies in " + formattedDiesIn + ')');
                        });
                user.sendMessage(CC.STRIKETHROUGH_GRAY + "------------------------------");
                break;
            case "checkwinners":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                if (gameService.isStateOrLower(GameState.SCATTERING)) {
                    user.sendMessage(CC.RED + "You cannot do that in the current game state.");
                    return;
                }
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've manually checked for if there's only one alive teams left to trigger end game state.");
                gameService.checkIfGameShouldEnd();
                break;
            case "options":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                new ConfigEditMenu(user).open();
                break;
            case "scenarios":
                if (!user.canExecuteModCommand()) {
                    user.sendMessage(CC.RED + "You cannot do that in your current state.");
                    return;
                }
                new ScenarioEditMenu(user).open();
                break;
            case "sync":
                gameService.setUseMatchSynchronization(!gameService.isUseMatchSynchronization());
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You've " + (gameService.isUseMatchSynchronization() ? CC.GREEN + "enabled" : CC.RED + "disabled")
                        + CC.GRAY + " upcoming games synchronization.");
                break;
            case "fetchworlds":
                worldService.setWorldsUsed(!worldService.isWorldsUsed());
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "New worlds will " + StringUtil.formatBooleanCommand(worldService.isWorldsUsed()) + CC.GRAY + " be used once the server restarts.");
                break;
            case "spawnrate":
                if (true) {
                    user.sendMessage(CC.RED + "Nope");
                    return;
                }
                if (args.length < 2 || !StringUtil.isInteger(args[1])) {
                    user.sendMessage(CC.RED + "Usage: /uhc spawnrate <number>");
                    return;
                }
                worldService.setAnimalSpawnRate(Integer.parseInt(args[1]));
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + "You have set the animal spawn rate to " + CC.PINK + args[1] + CC.GRAY + ".");
                break;
            case "fixlag":
                if (args.length < 2) {
                    user.performCommand("uhc fixlag medium");
                    return;
                }
                WorldService.ClearType clearType = WorldService.ClearType.getByName(args[1]);
                if (clearType == null) {
                    user.sendMessage(CC.RED + "Usage: /uhc fixlag <hard|medium|soft>");
                    return;
                }
                int removed = worldService.clearEntities(clearType);
                user.sendMessage(CC.PURPLE + "[UHC] " + CC.GRAY + StringUtils.capitalize(clearType.name().toLowerCase()) +
                        " cleared a total amount of " + CC.PINK + removed + CC.GRAY + " entities.");
                break;
            default:
                sendUsage(user);
        }
    }

    private void sendUsage(UHCUser user) {
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
        user.sendMessage(CC.BOLD_PURPLE + " UHC Commands " + CC.GRAY + '-' + CC.WHITE + " Manage the game with these commands");
        user.sendMessage();
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc start " + CC.GRAY + " - Starts the game");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc host [force]" + CC.GRAY + " - Toggle the host mode, force replaces the current host");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc supervisor [force]" + CC.GRAY + " - Toggle the supervisor mode, force replaces the current supervisor");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc mod" + CC.GRAY + " - Toggle the staff spectator mode");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc heal <player> " + CC.GRAY + " - Heals the player");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc latescatter <player> " + CC.GRAY + " - Late scatter the player");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc respawn <player> " + CC.GRAY + " - Respawn the player");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc remove <player> " + CC.GRAY + " - Eliminate the player from the game");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc forceshrink <radius> " + CC.GRAY + " - Force shrink the border");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc offline " + CC.GRAY + " - Lists of all alive combat loggers");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc checkwinners " + CC.GRAY + " - Manually check if only one team is left");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc options" + CC.GRAY + " - Edit game's options");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc scenarios" + CC.GRAY + " - Edit game's scenarios");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc spawnrate <number>" + CC.GRAY + " - Set the animal spawn rate.");
        user.sendMessage(CC.GRAY + ' ' + Lang.BULLET + CC.PINK + " /uhc fixlag <hard|medium|soft>" + CC.GRAY + " - Clear entities from the map.");
        user.sendMessage(CC.STRIKETHROUGH_GRAY + "--------------------------------------------------");
    }
}
