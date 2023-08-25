package net.exemine.uhc.border;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.exemine.api.util.Executor;
import net.exemine.uhc.UHC;
import net.exemine.uhc.border.event.BorderBuildEvent;
import net.exemine.uhc.border.event.BorderShrinkEvent;
import net.exemine.uhc.border.task.BorderPlaceTask;
import net.exemine.uhc.border.task.BorderShrinkTask;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.stream.IntStream;

@Setter
@Getter
public class BorderService {

    private final UHC plugin;

    @Setter(AccessLevel.PRIVATE)
    private BorderRadius currentRadius;
    private BorderShrinkTask borderShrinkTask;
    private String formattedShrinkIn;
    private boolean firstShrinkOccurred;

    private final int netherBorder = 350;
    private final int borderHeight = 4;

    public BorderService(UHC plugin) {
        this.plugin = plugin;
        this.currentRadius = BorderRadius.RADIUS_2000;
    }

    public boolean shrinkBorder(BorderRadius radius) {
        BorderShrinkEvent borderShrinkEvent = new BorderShrinkEvent(radius);
        Bukkit.getPluginManager().callEvent(borderShrinkEvent);

        if (!borderShrinkEvent.isCancelled()) {
            currentRadius = radius;
            Bukkit.getPluginManager().callEvent(new BorderBuildEvent(radius));
            plugin.getWorldService().updateCustomWorldBorder(radius.getValue());
        }
        return !borderShrinkEvent.isCancelled();
    }

    public void buildBorder(BorderRadius radius) {
        IntStream.range(0, borderHeight).forEach(i ->
                Executor.schedule(() -> new BorderPlaceTask(plugin, radius.getValue())).runSyncLater(i * 50L)
        );
    }

    public BorderRadius getNextBorderRadius() {
        return Arrays.stream(BorderRadius.values())
                .filter(radius -> radius.ordinal() - 1 == currentRadius.ordinal())
                .findFirst()
                .orElse(null);
    }
}
