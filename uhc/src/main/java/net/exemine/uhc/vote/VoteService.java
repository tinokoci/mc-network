package net.exemine.uhc.vote;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.MathUtil;
import net.exemine.api.util.string.CC;
import net.exemine.uhc.UHC;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.user.UHCUser;

@RequiredArgsConstructor
@Getter
@Setter
public class VoteService {

    private VoteInfo voteInfo;

    public void createVote(UHC plugin, ToggleOption option) {
        voteInfo = new VoteInfo(option);
        new VoteTask(plugin);
    }

    public void addVote(UHCUser user, boolean yes) {
        if (!isVoteRunning()) return;

        voteInfo.addVote(user, yes);
        user.sendMessage(CC.PURPLE + "[Vote] " + CC.GRAY + "You've voted with " + (yes ? CC.GREEN + "Yes" : CC.RED + "No") + CC.GRAY + " for the " + CC.GOLD + voteInfo.getOption().getName() + CC.GRAY + " option.");
    }

    public void finishVote() {
        if (!isVoteRunning()) return;

        int yesCount = voteInfo.getYesCount().getValue();
        int noCount = voteInfo.getNoCount().getValue();

        boolean enable = yesCount > noCount || (yesCount == noCount && MathUtil.tryChance(50));
        voteInfo.getOption().update(enable);
        voteInfo = null;
    }

    public boolean isVoteRunning() {
        return voteInfo != null;
    }
}
