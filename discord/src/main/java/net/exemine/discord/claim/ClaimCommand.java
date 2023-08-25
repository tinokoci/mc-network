package net.exemine.discord.claim;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.exemine.api.rank.Rank;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.ArrayList;
import java.util.List;

public class ClaimCommand extends BaseCommand {

    private final UHCClaimService uhcClaimService;

    public ClaimCommand(UHCClaimService uhcClaimService) {
        super("claim", "Opens a UHC match claim modal", true);
        this.uhcClaimService = uhcClaimService;
        setAsync(true);
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        if (!MemberUtil.isLinked(user)) {
            event.replyEmbeds(EmbedUtil.getNotLinked()).setEphemeral(true).queue();
            return;
        }
        TextInput supervisor = TextInput.create("supervisor", "Supervisor", TextInputStyle.SHORT)
                .setPlaceholder("IGN of your supervisor")
                .setRequired(true)
                .build();
        TextInput mode = TextInput.create("mode", "Mode", TextInputStyle.SHORT)
                .setPlaceholder("Example: FFA, To2, To3 etc.")
                .setRequired(true)
                .build();
        TextInput nether = TextInput.create("nether", "Nether", TextInputStyle.SHORT)
                .setPlaceholder("Either 'Yes' or 'No'")
                .setRequired(true)
                .build();
        TextInput scenarios = TextInput.create("scenarios", "Scenarios", TextInputStyle.SHORT)
                .setPlaceholder("Example: CutClean, Timber, NoClean")
                .setRequired(true)
                .build();
        TextInput timestamp = TextInput.create("timestamp", "Timestamp (UTC)", TextInputStyle.SHORT)
                .setPlaceholder("Example: 19/02/2023 15:00")
                .setRequired(true)
                .build();
        List<ActionRow> actionRows = new ArrayList<>();

        if (MemberUtil.isEqual(user, Rank.TRIAL_MOD)) {
            actionRows.add(ActionRow.of(supervisor));
        }
        actionRows.add(ActionRow.of(mode));
        actionRows.add(ActionRow.of(nether));
        actionRows.add(ActionRow.of(scenarios));
        actionRows.add(ActionRow.of(timestamp));

        Modal modal = Modal.create(uhcClaimService.getModalId(), "UHC Claim").addActionRows(actionRows).build();
        event.replyModal(modal).queue();
    }
}
