package net.exemine.uhc.vote;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;

import java.util.List;

public class VoteCommand extends BaseCommand<UHCUser, UHCData> {

    private final VoteService voteService;

    public VoteCommand(VoteService voteService) {
        super(List.of("vote"));
        this.voteService = voteService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (!voteService.isVoteRunning()) {
            user.sendMessage(CC.RED + "Voting is currently not running.");
            return;
        }
        if (voteService.getVoteInfo().hasVoted(user)) {
            user.sendMessage(CC.RED + "You've already voted.");
            return;
        }
        new VoteMenu(user).open();
    }
}
