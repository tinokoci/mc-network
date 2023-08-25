package net.exemine.api.data.template;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.exemine.api.database.DatabaseCollection;
import org.bson.Document;

@RequiredArgsConstructor
@Getter
public class DataTemplate {

    private final Document document;
    private final DatabaseCollection collection;
}
