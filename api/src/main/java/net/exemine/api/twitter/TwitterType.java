package net.exemine.api.twitter;

import io.github.redouane59.twitter.TwitterClient;
import lombok.Getter;

@Getter
public enum TwitterType {

    FEED;

    private TwitterClient instance;

    public boolean isConnected() {
        return instance != null;
    }

    public void setInstance(TwitterClient instance) {
        if (isConnected()) return;
        this.instance = instance;
    }
}
