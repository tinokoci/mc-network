package net.exemine.api.proxy;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.exemine.api.util.string.DatabaseUtil;

@Getter
@Setter
@RequiredArgsConstructor
public class ProxyCheck {

    public static final ProxyCheck DUMMY_CHECK = new ProxyCheck("Dummy", "Dummy", "Dummy", "Dummy", "us", "Dummy", "Dummy", "UTC", 0L, 0L, false);

    @SerializedName(DatabaseUtil.PRIMARY_KEY)
    private final String address;
    private final String provider;
    private final String continent;
    private final String country;
    private final String isoCode;
    private final String region;
    private final String type;
    private final String timeZoneId;
    private final double latitude;
    private final double longitude;
    private final boolean malicious;

    private ProxyCheckState state = ProxyCheckState.NORMAL;

    public boolean isBlacklisted() {
        return state == ProxyCheckState.BLACKLISTED || (malicious && state != ProxyCheckState.WHITELISTED);
    }

    public boolean isUnknown() {
        return address == null || address.equals(DUMMY_CHECK.getAddress());
    }
}