package net.exemine.uhc.vote;

import net.exemine.api.util.TimeUtil;
import net.exemine.api.util.string.CC;
import net.exemine.api.util.string.Lang;
import net.exemine.core.util.MessageUtil;
import net.exemine.core.util.spigot.Clickable;
import net.exemine.uhc.UHC;
import net.exemine.uhc.user.UHCUserService;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteTask extends BukkitRunnable {

    private final VoteService voteService;
    private final UHCUserService userService;

    private final int startValue = 60 * 2;
    private int countdown = startValue;

    public VoteTask(UHC plugin) {
        this.voteService = plugin.getVoteService();
        this.userService = plugin.getUserService();
        this.runTaskTimer(plugin, 20L, 20L);
        new Clickable()
                .add(CC.BOLD_GOLD + "[Vote] " + CC.GRAY + "A vote for " + CC.WHITE + voteService.getVoteInfo().getOption().getName() + CC.GRAY + " has started. ")
                .add(CC.BOLD_GREEN + "CLICK TO VOTE", CC.GREEN + "Click to vote.", "/vote")
                .broadcast();
    }

    @Override
    public void run() {
        VoteInfo voteInfo = voteService.getVoteInfo();

        if (--countdown == 0) {
            voteService.finishVote();
            MessageUtil.send(CC.BOLD_GOLD + "[Vote] " + CC.GRAY + "A vote for " + CC.WHITE + voteInfo.getOption().getName() + CC.GRAY + " has finished with "
                    + CC.GREEN + voteInfo.getYesCount().getValue() + CC.GRAY + ' ' + Lang.LINE + ' ' + CC.RED + voteInfo.getNoCount().getValue() + CC.GRAY + '.');
            return;
        }
        if (TimeUtil.shouldAlert(countdown, startValue)) {
            userService.getOnlineUsers().forEach(user -> {
                Clickable clickable = new Clickable(CC.BOLD_GOLD + "[Vote] " + CC.GRAY + "A vote for " + CC.WHITE + voteInfo.getOption().getName() + CC.GRAY + " is ending in " + TimeUtil.getNormalDuration(countdown, CC.PINK, CC.GRAY) + '.');

                if (!voteInfo.hasVoted(user)) {
                    clickable.add(" ");
                    clickable.add(CC.BOLD_GREEN + "VOTE", CC.GREEN + "Click to vote.", "/vote");
                }
                clickable.send(user);
            });
            MessageUtil.play(Sound.CLICK);
        }
    }
}
