package net.exemine.discord.changelog;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ChangeLog {

    private static final Map<User, ChangeLog> PROCESSES = new HashMap<>();

    private final User user;
    private State state = State.NEW_FEATURES;

    private List<String> newFeatures = new ArrayList<>();
    private List<String> tweaks = new ArrayList<>();
    private List<String> bugFixes = new ArrayList<>();
    private List<String> knownIssues = new ArrayList<>();

    public enum State {
        NEW_FEATURES,
        TWEAKS,
        BUG_FIXES,
        KNOWN_ISSUES,
        CONFIRMATION
    }

    public ChangeLog(User user) {
        this.user = user;
        PROCESSES.put(user, this);
    }

    public String getFormattedState() {
        return state.name().toLowerCase().replace("_", " ");
    }

    public boolean isConfirmation() {
        return state == State.CONFIRMATION;
    }

    public void advanceState() {
        state = Arrays.stream(State.values())
                .filter(state -> state.ordinal() - 1 == this.state.ordinal())
                .findFirst()
                .orElse(null);
    }

    public void cancel() {
        PROCESSES.remove(user);
    }

    @Nullable
    public static ChangeLog get(User user) {
        return PROCESSES.get(user);
    }
}