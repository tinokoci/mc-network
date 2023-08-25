package net.exemine.api.data.stat.string;

import net.exemine.api.data.stat.TimedStat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StringTimedStat extends TimedStat<String> {

    public List<String> getAll(StringOrder order) {
        Comparator<Map.Entry<Long, String>> comparator = order == StringOrder.ASCENDING
                ? Map.Entry.comparingByKey()
                : Collections.reverseOrder(Map.Entry.comparingByKey());

        return map.entrySet()
                .stream()
                .sorted(comparator)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public String getByTimestamp(long timestamp) {
        return map.get(timestamp);
    }

    public String getByIndex(int index) {
        return new ArrayList<>(map.values()).get(index);
    }

    public int size() {
        return map.size();
    }
}
