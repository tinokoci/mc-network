package net.exemine.api.proxy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.client.model.Filters;
import lombok.AccessLevel;
import lombok.Getter;
import net.exemine.api.database.DatabaseCollection;
import net.exemine.api.database.DatabaseService;
import net.exemine.api.redis.RedisJsonObject;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.ProxyCheckStateUpdateModel;
import net.exemine.api.util.GsonUtil;
import net.exemine.api.util.string.DatabaseUtil;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ProxyService {

    private final DatabaseService databaseService;
    private final RedisService redisService;
    private final String apiKey;

    @Getter(AccessLevel.PACKAGE)
    private LoadingCache<String, ProxyCheck> checkCache;

    private static final String API_URL = "https://proxycheck.io/v2/";
    private static final int API_TIMEOUT = 5000;

    public ProxyService(DatabaseService databaseService, RedisService redisService, String apiKey) {
        this.databaseService = databaseService;
        this.redisService = redisService;
        this.apiKey = apiKey;
        setupCache();
        subscribeToProxyCheckStateUpdate();
    }

    private void setupCache() {
        checkCache = CacheBuilder.newBuilder()
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build(new CacheLoader<String, ProxyCheck>() {
                    @Override
                    public @NotNull ProxyCheck load(@NotNull String address) {
                        Document document = databaseService.findOne(DatabaseCollection.LOGS_VPN, Filters.eq(DatabaseUtil.PRIMARY_KEY, address)).run();
                        if (document != null) {
                            return GsonUtil.fromDocument(document, ProxyCheck.class);
                        }
                        AtomicReference<ProxyCheck> reference = new AtomicReference<>();
                        checkAddress(address).ifPresentOrElse(check -> {
                            reference.set(check);
                            databaseService.insert(DatabaseCollection.LOGS_VPN, GsonUtil.toDocument(check)).run();
                        }, () -> reference.set(ProxyCheck.DUMMY_CHECK));
                        return reference.get();
                    }
                });
    }

    public ProxyCheck getOrCheckAddress(String address) {
        try {
            return checkCache.get(address);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return ProxyCheck.DUMMY_CHECK;
        }
    }

    public void updateCheck(ProxyCheck check, ProxyCheckState state) {
        check.setState(state);
        databaseService.update(DatabaseCollection.LOGS_VPN, Filters.eq(DatabaseUtil.PRIMARY_KEY, check.getAddress()), GsonUtil.toDocument(check)).run();
        redisService.getPublisher().sendProxyCheckStateUpdate(check.getAddress(), state);
    }

    private void subscribeToProxyCheckStateUpdate() {
        redisService.subscribe(RedisMessage.PROXY_CHECK_STATE_UPDATE, ProxyCheckStateUpdateModel.class, model -> {
            ProxyCheck check = getOrCheckAddress(model.getAddress());
            check.setState(model.getNewState());
        });
    }

    /**
     * @author <a href="https://proxycheck.io">...</a>
     * Modified by valentino
     */
    private Optional<ProxyCheck> checkAddress(String address) {
        String queryUrl = API_URL + address + "?key=" + apiKey + "&vpn=1&asn=1&node=0&time=0&inf=0&port=0&seen=0&days=7";

        RedisJsonObject parentObject;
        try {
            parentObject = new RedisJsonObject(query(queryUrl));
        } catch (IOException e) {
            return Optional.empty();
        }
        if (parentObject.getString("status").equals("error")) {
            return Optional.empty();
        }
        RedisJsonObject object = new RedisJsonObject(parentObject.getJsonObject(address));

        String provider = object.getString("provider");
        String continent = object.getString("continent");
        String country = object.getString("country");
        String isocode = object.getString("isocode");
        String region = object.getString("region");
        String type = object.getString("type");
        String timeZone = object.getString("timezone");
        double latitude = object.getDouble("latitude");
        double longitude = object.getDouble("longitude");
        boolean malicious = object.getString("proxy").equalsIgnoreCase("yes");

        return Optional.of(new ProxyCheck(address, provider, continent, country, isocode, region, type, timeZone, latitude, longitude, malicious));
    }

    /**
     * @author <a href="https://proxycheck.io">...</a>
     * Modified by valentino
     */
    private String query(String url) throws IOException {
        StringBuilder response = new StringBuilder();
        URL website = new URL(url);
        URLConnection connection = website.openConnection();

        connection.setConnectTimeout(API_TIMEOUT);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent", "Defiance-AntiBot.v1.2.0");
        connection.setRequestProperty("tag", "Defiance-AntiBot.v1.2.0");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            while ((url = in.readLine()) != null) {
                response.append(url);
            }
        }
        return response.toString();
    }
}
