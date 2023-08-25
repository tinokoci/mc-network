package net.exemine.discord.command;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;

import java.util.Arrays;

@RequiredArgsConstructor
public class CommandService {

    private final JDA jda;

    public void register(BaseCommand... commands) {
        Arrays.stream(commands).forEach(command -> jda.addEventListener(command.setup(jda)));
    }
}
