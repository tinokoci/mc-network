package net.exemine.api.twitter;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import net.exemine.api.util.LogUtil;
import net.exemine.api.util.config.ConfigFile;

import java.util.Arrays;

public class TwitterService {

    public TwitterService(ConfigFile config) {
        Arrays.stream(TwitterType.values()).forEach(type -> {
            String path = "twitter." + type.name().toLowerCase();
            if (config.getConfigurationSection(path) == null) return;
            type.setInstance(new TwitterClient(TwitterCredentials.builder()
                    .apiKey(config.getString(path + ".apiKey"))
                    .apiSecretKey(config.getString(path + ".apiSecretKey"))
                    .accessToken(config.getString(path + ".accessToken"))
                    .accessTokenSecret(config.getString(path + ".accessTokenSecret"))
                    .build()));
        });
    }

    public void tweet(TwitterType type, String message) {
        if (!type.isConnected()) {
            LogUtil.warning("Tried to tweet from " + type.name() + " instance which is not connected");
            return;
        }
        TwitterClient client = type.getInstance();
        client.postTweet(message);
    }
}
