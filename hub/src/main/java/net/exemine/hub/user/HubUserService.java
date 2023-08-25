package net.exemine.hub.user;

import net.exemine.api.data.impl.HubData;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.core.user.base.UserService;
import net.exemine.hub.Hub;

import java.util.function.Supplier;

public class HubUserService extends UserService<HubUser, HubData> {

    public HubUserService(Hub plugin, Supplier<HubUser> userSupplier, Supplier<HubData> dataSupplier, DatabaseCollection databaseCollection) {
        super(plugin, plugin.getCore().getDatabaseService(), userSupplier, dataSupplier, databaseCollection);
    }
}
