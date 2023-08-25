package net.exemine.discord.command.impl.staff;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.EmbedUtil;

public class RestartCommand extends BaseCommand {

    public RestartCommand() {
        super("restart", "Restarts the discord bot service.", true);
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        event.replyEmbeds(EmbedUtil.success("Restart", "The discord bot service has been set to restart."))
                .setEphemeral(true)
                .queue();
        System.exit(0);
    }
}
