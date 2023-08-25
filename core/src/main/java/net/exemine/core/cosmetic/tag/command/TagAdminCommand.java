package net.exemine.core.cosmetic.tag.command;

import net.exemine.api.cosmetic.tag.Tag;
import net.exemine.api.cosmetic.tag.TagService;
import net.exemine.api.data.impl.CoreData;
import net.exemine.api.rank.Rank;
import net.exemine.api.util.StringUtil;
import net.exemine.api.util.string.CC;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.user.CoreUser;
import net.exemine.core.util.ServerUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TagAdminCommand extends BaseCommand<CoreUser, CoreData> {

    private final TagService tagService;

    public TagAdminCommand(TagService tagService) {
        super(List.of("tagadmin", "tagsadmin", "admintag", "admintags"), Rank.ADMIN, false);
        this.tagService = tagService;
        setAsync(true);
        setUsage(CC.RED + "Usage: /tagadmin <create|delete|list> [name] [format]");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getUsage());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    sender.sendMessage(getUsage());
                    return;
                }
                Tag tag = tagService.getTag(args[1]);

                if (tag != null) {
                    sender.sendMessage(CC.RED + "Tag with that name already exists.");
                    return;
                }
                String name = args[1];
                String format = StringUtil.join(args, 2);

                tagService.create(name, format);
                sender.sendMessage(CC.PURPLE + "[Tag] " + CC.GRAY + "You have created a tag named " + CC.GOLD + name + CC.GRAY
                        + " with the format " + CC.WHITE + CC.translate(format) + CC.GRAY + '.');
                break;
            case "delete":
                if (args.length != 2) {
                    sender.sendMessage(getUsage());
                    return;
                }
                tag = tagService.getTag(args[1]);

                if (tag == null) {
                    sender.sendMessage(CC.RED + "Tag with that name doesn't exist.");
                    return;
                }
                String tagName = tag.getName();
                tagService.delete(tag);
                sender.sendMessage(CC.PURPLE + "[Tag] " + CC.GRAY + "You have deleted the tag named " + CC.GOLD + tagName + CC.GRAY + '.');
                break;
            case "list":
                ServerUtil.performCommand(sender, "tags");
                break;
            default:
                sender.sendMessage(getUsage());
        }
    }
}
