package net.exemine.uhc.vote;

import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.util.item.ItemBuilder;
import net.exemine.core.util.item.ItemUtil;
import net.exemine.uhc.user.UHCUser;
import org.bukkit.Material;

import java.util.stream.IntStream;

public class VoteMenu extends Menu<UHCUser> {

    private final VoteService voteService;

    public VoteMenu(UHCUser user) {
        super(user, CC.DARK_GRAY + "Choose an option:", 5);
        this.voteService = user.getPlugin().getVoteService();
        setAutoSurround(true);
    }

    @Override
    public void update() {
        IntStream.of(10, 11, 12, 19, 21, 28, 29, 30).forEach(index -> set(index, new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName(CC.GREEN + "Click on the green wool to vote.")
                .setDurability(13)
                .build()));
        IntStream.of(14, 15, 16, 23, 25, 32, 33, 34).forEach(index -> set(index, new ItemBuilder(Material.STAINED_GLASS_PANE)
                .setName(CC.RED + "Click on the red wool to vote.")
                .setDurability(14)
                .build()));

        set(20, new ItemBuilder()
                .setMaterial(Material.WOOL)
                .setDurability(ItemUtil.getGreen())
                .setName(CC.GREEN + "Yes")
                .setLore(
                        CC.GRAY + "Click here if you want",
                        CC.GRAY + "this option enabled."
                )
                .build()
        ).onClick(() -> {
            voteService.addVote(user, true);
            close();
        });

        set(24, new ItemBuilder()
                .setMaterial(Material.WOOL)
                .setDurability(ItemUtil.getRed())
                .setName(CC.RED + "No")
                .setLore(
                        CC.GRAY + "Click here if you want",
                        CC.GRAY + "this option disabled."
                )
                .build()
        ).onClick(() -> {
            voteService.addVote(user, false);
            close();
        });
    }
}
