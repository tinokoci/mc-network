package net.exemine.discord.ticket.impl.appeal.level;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.exemine.api.rank.Rank;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum AppealLevel {

    STAFF(Rank.MOD, Rank.MOD_PLUS),
    SENIOR(Rank.SENIOR_MOD),
    MANAGER(Rank.MANAGER),
    ADMIN(Rank.ADMIN);

    private List<Rank> ranks = new ArrayList<>();

    AppealLevel(Rank... ranks) {
        this.ranks = List.of(ranks);
    }

    public Rank getRank() {
        return ranks.get(0);
    }
}
