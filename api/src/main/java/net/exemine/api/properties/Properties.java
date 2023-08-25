package net.exemine.api.properties;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.model.Motd;
import net.exemine.api.rank.Rank;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Properties {

    private Rank maintenanceRank = Rank.DEFAULT;
    private Motd motd = new Motd();
    private String staffListMessageId;
    private String upcomingMatchesMessageId;
    private Map<String, String> networkTips = new HashMap<>();

    public boolean isMaintenance() {
        return maintenanceRank != Rank.DEFAULT;
    }

    public Collection<String> getListOfNetworkTips() {
        return networkTips.values();
    }
}
