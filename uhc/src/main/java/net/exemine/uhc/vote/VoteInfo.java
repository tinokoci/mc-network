package net.exemine.uhc.vote;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.stat.number.impl.SimpleIntStat;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.user.UHCUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class VoteInfo {

    private final ToggleOption option;

    private final SimpleIntStat yesCount = new SimpleIntStat();
    private final SimpleIntStat noCount = new SimpleIntStat();

    private final List<UUID> voted = new ArrayList<>();

    public void addVote(UHCUser user, boolean yes) {
        if (yes) {
            yesCount.increment();
        } else {
            noCount.increment();
        }
        voted.add(user.getUniqueId());
    }

    public boolean hasVoted(UHCUser user) {
        return voted.contains(user.getUniqueId());
    }
}
