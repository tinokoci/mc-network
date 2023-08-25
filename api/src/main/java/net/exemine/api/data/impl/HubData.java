package net.exemine.api.data.impl;

import lombok.Getter;
import lombok.Setter;
import net.exemine.api.data.ExeData;
import net.exemine.api.database.DatabaseCollection;

import java.time.LocalDateTime;

@Getter
@Setter
public class HubData extends ExeData {

    private LocalDateTime firstSeen;
    
    private boolean edit;
    private boolean flight;
    private boolean playerVisibility = true;

    @Override
    public DatabaseCollection getMongoCollection() {
        return DatabaseCollection.USERS_LOBBY;
    }
}
