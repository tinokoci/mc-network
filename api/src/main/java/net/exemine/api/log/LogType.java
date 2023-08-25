package net.exemine.api.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.database.DatabaseCollection;

@RequiredArgsConstructor
@Getter
public enum LogType {

    MINECRAFT(DatabaseCollection.LOGS_CHAT_MINECRAFT),
    DISCORD(DatabaseCollection.LOGS_CHAT_DISCORD);

    private final DatabaseCollection collection;
}
