package net.exemine.hub.provider;

import net.exemine.api.rank.Rank;
import net.exemine.core.provider.NametagProvider;
import net.exemine.core.provider.nametag.NametagInfo;
import net.exemine.core.provider.nametag.NametagService;
import net.exemine.core.util.PacketUtil;
import net.exemine.hub.user.HubUser;

public class HubNametagProvider implements NametagProvider<HubUser> {

    @Override
    public NametagInfo getNametag(HubUser toRefresh, HubUser refreshFor, NametagService<HubUser> nametagService) {
        Rank rank = toRefresh.isDisguised() ? Rank.DEFAULT : toRefresh.getRank();
        String teamName = PacketUtil.convertNumberToTeamName(rank.getPriority());
        return nametagService.getOrCreate(teamName, rank.getColor());
    }
}
