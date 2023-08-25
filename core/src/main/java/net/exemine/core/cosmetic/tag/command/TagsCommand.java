package net.exemine.core.cosmetic.tag.command;

import net.exemine.api.data.impl.CoreData;
import net.exemine.core.command.base.BaseCommand;
import net.exemine.core.cosmetic.tag.TagSelectMenu;
import net.exemine.core.user.CoreUser;

import java.util.List;

public class TagsCommand extends BaseCommand<CoreUser, CoreData> {

    public TagsCommand() {
        super(List.of("tags", "tag", "prefix", "prefixes"));
    }

    @Override
    public void execute(CoreUser user, CoreData data, String[] args) {
        new TagSelectMenu(user).open();
    }
}
