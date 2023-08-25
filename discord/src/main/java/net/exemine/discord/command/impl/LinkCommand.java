package net.exemine.discord.command.impl;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.exemine.api.data.DataService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.cache.RedisCache;
import net.exemine.api.redis.cache.model.DiscordLinkModel;
import net.exemine.discord.command.BaseCommand;
import net.exemine.discord.util.DiscordUtil;
import net.exemine.discord.util.EmbedUtil;
import net.exemine.discord.util.MemberUtil;

import java.util.List;
import java.util.Objects;

public class LinkCommand extends BaseCommand {

    private final DataService dataService;
    private final RedisService redisService;

    public LinkCommand(DataService dataService, RedisService redisService) {
        super("link", "Link your discord account to a minecraft account.");
        this.dataService = dataService;
        this.redisService = redisService;

        setAsync(true);
        setOptionData(List.of(
                new OptionData(OptionType.STRING, "username", "Minecraft account name to link your discord account with.", true)
        ));
    }

    @Override
    protected void execute(User user, Guild guild, SlashCommandInteraction interaction, SlashCommandInteractionEvent event) {
        String username = Objects.requireNonNull(interaction.getOption("username")).getAsString();
        InteractionHook hook = event.deferReply(true).complete();

        dataService.fetch(CoreData.class, username).ifPresentOrElse(data -> {
            Member member = event.getMember();

            if (member == null) {
                hook.editOriginalEmbeds(EmbedUtil.error("Something went wrong while trying to link you.")).queue();
                return;
            }
            if (MemberUtil.isLinked(member)) {
                hook.editOriginalEmbeds(EmbedUtil.error("You are already linked.")).queue();
                return;
            }
            if (data.isDiscordLinked()) {
                hook.editOriginalEmbeds(EmbedUtil.error(DiscordUtil.code(username) + " is already linked to an account.")).queue();
                return;
            }
            DiscordLinkModel discordLinkModel = new DiscordLinkModel(user.getId(),user.getName() + '#' + user.getDiscriminator());
            redisService.addValueToHash(RedisCache.DISCORD_LINK, data.getUniqueId(), discordLinkModel);
            redisService.getPublisher().sendDiscordLinkRequest(data.getUniqueId());
            hook.editOriginalEmbeds(EmbedUtil.create("Discord Link", "Link request has been sent to " + DiscordUtil.code(data.getName()) + " in game.")).queue();
        }, () -> hook.editOriginalEmbeds(EmbedUtil.getUserNeverPlayed(username)).queue());
    }
}
