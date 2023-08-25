package net.exemine.core.lunar.impl;

import com.lunarclient.bukkitapi.nethandler.LCPacket;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketStaffModState;
import com.lunarclient.bukkitapi.object.StaffModule;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.exemine.core.lunar.LunarModule;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Accessors(chain = true)
public class LunarStaffModule extends LunarModule {

    private final boolean state;
    private final StaffModule[] modules;

    public LunarStaffModule(boolean state, StaffModule... modules) {
        this.state = state;
        this.modules = modules;
    }

    @Override
    protected Set<LCPacket> getPackets() {
        return Arrays.stream(modules)
                .map(module -> new LCPacketStaffModState(module.name(), state))
                .collect(Collectors.toSet());
    }
}
