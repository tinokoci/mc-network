package net.exemine.discord.claim;

import com.mongodb.client.model.Filters;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.ModalInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.exemine.api.data.DataService;
import net.exemine.api.data.bulk.BulkData;
import net.exemine.api.data.bulk.BulkDataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.match.Match;
import net.exemine.api.match.MatchService;
import net.exemine.api.match.MatchState;
import net.exemine.api.match.impl.uhc.UHCMatch;
import net.exemine.api.model.ScenarioName;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.Executor;
import net.exemine.api.util.TimeUtil;
import net.exemine.discord.user.NetworkUser;
import net.exemine.discord.util.DiscordConstants;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class UHCClaimListener extends ListenerAdapter {

    private final BulkDataService bulkDataService;
    private final DataService dataService;
    private final MatchService matchService;
    private final UHCClaimService uhcClaimService;

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        ModalInteraction interaction = event.getInteraction();
        if (!interaction.getModalId().equals(uhcClaimService.getModalId())) return;

        InteractionHook hook = event.deferReply(true).complete();
        User user = event.getUser();
        NetworkUser networkUser = NetworkUser.getOrCreate(user, NetworkUser::loadCoreData);

        ModalMapping supervisor = interaction.getValue("supervisor");
        String mode = Objects.requireNonNull(interaction.getValue("mode")).getAsString();
        String nether = Objects.requireNonNull(interaction.getValue("nether")).getAsString();
        String scenarios = Objects.requireNonNull(interaction.getValue("scenarios")).getAsString();
        String timestamp = Objects.requireNonNull(interaction.getValue("timestamp")).getAsString();

        // Supervisor Validation
        AtomicReference<Optional<CoreData>> atomicSupervisorCoreData = new AtomicReference<>(Optional.empty());
        if (supervisor != null) {
            MessageEmbed embed = EmbedUtil.error("Username of a staff member who will supervise your game is invalid.");
            atomicSupervisorCoreData.set(dataService.fetch(CoreData.class, supervisor.getAsString()));

            if (atomicSupervisorCoreData.get().isEmpty()) {
                hook.editOriginalEmbeds(embed).queue();
                return;
            }
            BulkData supervisorBulkData = bulkDataService.getOrCreate(atomicSupervisorCoreData.get().get().getUniqueId(), bulkDataService::loadRanks);

            if (!supervisorBulkData.getRank().isEqualOrAbove(Rank.MOD)) {
                hook.editOriginalEmbeds(embed).queue();
                return;
            }
        }
        // Mode Validation
        if (!uhcClaimService.isMode(mode)) {
            MessageEmbed embed = EmbedUtil.error("Mode you provided is invalid.");
            hook.editOriginalEmbeds(embed).queue();
            return;
        }
        // Nether Validation
        if (Stream.of("yes", "no").noneMatch(nether::equalsIgnoreCase)) {
            MessageEmbed embed = EmbedUtil.error("Nether state provided is invalid.");
            hook.editOriginalEmbeds(embed).queue();
            return;
        }
        // Scenarios Validation
        Set<ScenarioName> scenariosList = Arrays.stream(scenarios.replace(" ", "").split(","))
                .map(ScenarioName::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (scenariosList.isEmpty()) {
            MessageEmbed embed = EmbedUtil.error("None of the scenarios you provided are valid.");
            hook.editOriginalEmbeds(embed).queue();
            return;
        }
        // Timestamp Validation
        if (!TimeUtil.isDate(timestamp)) {
            MessageEmbed embed = EmbedUtil.error("Please use the following format: dd/MM/yyyy HH:mm"
                    + "\nMake sure the timestamp is in the UTC timezone."
                    + "\nExample: 13/01/2023 04:52");
            hook.editOriginalEmbeds(embed).queue();
            return;
        }
        long timestampInMillis = TimeUtil.getMillisFromDate(timestamp);
        if (timestampInMillis < System.currentTimeMillis()) {
            hook.editOriginalEmbeds(EmbedUtil.error("You cannot claim a match in the past.")).queue();
            return;
        }
        if (matchService.fetchMatch(UHCMatch.class, Filters.eq("start-time", String.valueOf(timestampInMillis))).isPresent()) {
            hook.editOriginalEmbeds(EmbedUtil.error("A match is already claimed at that time and date.")).queue();
            return;
        }
        // Create Match
        matchService.createMatch(UHCMatch.class, match -> {
            match.setStartTime(timestampInMillis);
            match.setState(MatchState.APPROVAL);
            match.setHostUuid(networkUser.getCoreData().getUniqueId());
            match.setMode(mode.toUpperCase().replace("TO", "To"));
            match.setNether(nether.equalsIgnoreCase("yes"));
            match.setScenarios(scenariosList);

            if (atomicSupervisorCoreData.get().isPresent()) {
                match.setSupervisorUuid(atomicSupervisorCoreData.get().get().getUniqueId());
            }
            // Send Approval Message
            EmbedBuilder embed = new EmbedBuilder();

            embed.setTitle("Match Information");
            embed.addField("Host", user.getAsMention(), true);
            embed.addField("Mode", match.getMode(), true);
            embed.addField("Nether", match.isNether() ? "Enabled" : "Disabled", true);

            if (match.hasSupervisor()) {
                CoreData supervisorData = atomicSupervisorCoreData.get().get();
                String supervisorIgn = supervisorData.isDiscordLinked()
                        ? DiscordUtil.getUser(supervisorData.getDiscordUserId()).getAsMention()
                        : supervisorData.getName();
                embed.addField("Supervisor", supervisorIgn, true);
            }
            embed.addField("Scenarios", match.getScenarios()
                    .stream()
                    .map(ScenarioName::getName)
                    .collect(Collectors.joining(", ")), false);
            embed.addField("Timestamp", TimeUtil.getDate(match.getStartTime()), true);

            String text = DiscordUtil.getOrCreateRole(Rank.MOD_PLUS).getAsMention() + ' ' + DiscordUtil.getOrCreateRole(Rank.SENIOR_MOD).getAsMention();
            Message message = DiscordConstants.getChannelUHCApproval().sendMessage(text)
                    .setEmbeds(embed.build())
                    .setActionRows(ActionRow.of(
                            Button.success(uhcClaimService.getButtonAcceptId(), "Accept"),
                            Button.danger(uhcClaimService.getButtonRejectId(), "Reject")
                    )).complete();
            match.setApprovalMessageId(message.getId());
            hook.editOriginalEmbeds(EmbedUtil.create("Match Claim", "You've sent the match request for approval."))
                    .queue();
        });

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        ButtonInteraction interaction = event.getInteraction();
        String messageId = interaction.getMessageId();
        Optional<UHCMatch> optionalMatch = matchService.fetchMatch(UHCMatch.class, Filters.eq("approval-message-id", messageId));
        if (optionalMatch.isEmpty()) return;

        Match match = optionalMatch.get();
        String buttonId = event.getComponentId();
        if (buttonId.equalsIgnoreCase(uhcClaimService.getButtonAcceptId())) {
            match.setState(MatchState.WAITING);
            event.getMessage().delete().queue();
            event.replyEmbeds(EmbedUtil.success("Match Update", "You've accepted the match request."))
                    .setEphemeral(true)
                    .queue();
        } else if (buttonId.equals(uhcClaimService.getButtonRejectId())) {
            match.setState(MatchState.INVALID);
            event.getMessage().delete().queue();
            event.replyEmbeds(EmbedUtil.error("Match Update", "You've denied the match request."))
                    .setEphemeral(true)
                    .queue();
        } else return;

        Executor.schedule(() -> matchService.updateMatch(match)).runAsync();
    }
}
