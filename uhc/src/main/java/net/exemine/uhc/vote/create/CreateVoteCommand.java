package net.exemine.uhc.vote.create;

import net.exemine.api.data.impl.UHCData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.vote.VoteService;

import java.util.List;

public class CreateVoteCommand extends BaseCommand<UHCUser, UHCData> {

    private final VoteService voteService;

    public CreateVoteCommand(VoteService voteService) {
        super(List.of("createvote"), Rank.TRIAL_MOD);
        this.voteService = voteService;
    }

    @Override
    public void execute(UHCUser user, UHCData data, String[] args) {
        if (voteService.isVoteRunning()) {
            user.sendMessage(CC.RED + "A vote for " + CC.BOLD + voteService.getVoteInfo().getOption().getName() + CC.RED + " is already running.");
            return;
        }
        new CreateVoteMenu(user).open();
    }
}
