package net.exemine.discord.command.impl;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.Objects;

public class ToggleAlertsCommand extends BaseCommand {

    public ToggleAlertsCommand() {
        super("togglealerts", "Toggle UHC match alerts.");
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        Role role = DiscordConstants.getRoleUHCAlerts();
        boolean hasRole = MemberUtil.hasRole(user, role);

        if (hasRole) {
            guild.removeRoleFromMember(Objects.requireNonNull(event.getMember()), role).queue();
        } else {
            guild.addRoleToMember(Objects.requireNonNull(event.getMember()), role).queue();
        }
        event.replyEmbeds(EmbedUtil.success("UHC Alerts", "You will " + (hasRole ? "no longer" : "now") + " receive match alerts."))
                .setEphemeral(true)
                .queue();
    }
}
