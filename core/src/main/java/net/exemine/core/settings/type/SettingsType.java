package net.exemine.core.settings.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.data.ExeData;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.data.impl.FFAData;
import net.exemine.api.data.impl.HubData;
import net.exemine.api.data.impl.UHCData;
import net.exemine.api.instance.InstanceType;
import org.bukkit.Material;

@RequiredArgsConstructor
@Getter
public enum SettingsType {

    GENERAL("General", CoreData.class, InstanceType.UNKNOWN, Material.BOOK),
    LUNAR("Lunar", CoreData.class, InstanceType.UNKNOWN, Material.ENDER_PEARL),
    HUB("Hub", HubData.class, InstanceType.HUB, Material.WATCH),
    UHC("UHC", UHCData.class, InstanceType.UHC, Material.GOLDEN_APPLE),
    FFA("FFA", FFAData.class, InstanceType.FFA, Material.DIAMOND_SWORD);

    private final String name;
    private final Class<? extends ExeData> dataClazz;
    private final InstanceType instanceType;
    private final Material material;
}
