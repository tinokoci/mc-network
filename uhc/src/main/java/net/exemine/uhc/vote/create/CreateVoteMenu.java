package net.exemine.uhc.vote.create;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.uhc.config.option.ToggleOption;
import net.exemine.uhc.user.UHCUser;
import net.exemine.uhc.vote.VoteService;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

public class CreateVoteMenu extends Menu<UHCUser> {

    private final VoteService voteService;

    public CreateVoteMenu(UHCUser user) {
        super(user, CC.DARK_GRAY + "Select an option:", 3);
        this.voteService = user.getPlugin().getVoteService();
        setAutoSurround(true);
    }

    @Override
    public void update() {
        addExitItem();

        Queue<Integer> indexes = new LinkedList<>(List.of(10, 11, 12, 14, 15, 16));
        Stream.of(ToggleOption.NETHER, ToggleOption.BED_BOMBING, ToggleOption.SPEED_POTIONS_I, ToggleOption.SPEED_POTIONS_II, ToggleOption.STRENGTH_POTIONS_I, ToggleOption.STRENGTH_POTIONS_II)
                .forEach(toggleOption -> set(indexes.remove(), new ItemBuilder()
                        .setMaterial(toggleOption.getMaterial())
                        .setDurability(toggleOption.getItemDurability())
                        .setName(CC.PINK + toggleOption.getName())
                        .setLore(lore -> {
                            lore.add("");
                            toggleOption.getDescription().forEach(line -> lore.add(CC.GRAY + line));
                            lore.add("");
                            lore.add(CC.GREEN + "Click to create a vote.");
                        })
                        .build()
                ).onClick(() -> {
                    if (voteService.isVoteRunning()) {
                        user.sendMessage(CC.RED + "A vote for " + CC.BOLD + voteService.getVoteInfo().getOption().getName() + CC.RED + " is already running.");
                        close();
                        return;
                    }
                    voteService.createVote(user.getPlugin(), toggleOption);
                    close();
                }));
    }
}
