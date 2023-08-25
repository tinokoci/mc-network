package net.exemine.core.menu.pagination;

import lombok.AccessLevel;
import lombok.Setter;
import net.exemine.api.util.string.CC;
import net.exemine.core.menu.Menu;
import net.exemine.core.menu.button.Button;
import net.exemine.core.user.base.ExeUser;
import net.exemine.core.util.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class PaginatedMenu<T extends ExeUser<?>> extends Menu<T> {

    private final Map<Integer, Button> globalButtons = new HashMap<>();
    private final Map<Integer, Button> pageButtons = new HashMap<>();

    private final int itemsPerPage;
    private final PaginationType type;
    private final PaginationItem item;

    @Setter(AccessLevel.PRIVATE)
    private int page = 1;

    public PaginatedMenu(T user, String title, int totalRows, int paginatedRows, PaginationType type, PaginationItem item) {
        super(user, title, totalRows);

        this.itemsPerPage = paginatedRows * type.getItemsPerRow();
        this.type = type;
        this.item = item;
    }

    public PaginatedMenu(T user, String title, int totalRows, int paginatedRows) {
        this(user, title, totalRows, paginatedRows, PaginationType.SEVEN, PaginationItem.CARPET);
    }

    @Override
    public final void update() {
        globalButtons.clear();
        pageButtons.clear();
        global();
        pagination();

        int minIndex = (int) ((double) (page - 1) * itemsPerPage);
        int maxIndex = (int) ((double) (page) * itemsPerPage);

        for (Map.Entry<Integer, Button> entry : pageButtons.entrySet()) {
            int index = entry.getKey();

            if (index >= minIndex && index < maxIndex) {
                index -= (int) ((double) (itemsPerPage) * (page - 1)) - 9;
                int slot = index + (type == PaginationType.SEVEN ? index > 22 ? 5 : index > 15 ? 3 : 1 : 0);
                buttons.put(slot, entry.getValue());
            }
        }
        // Previous Page Button
        if (hasPreviousPage() || item == PaginationItem.LEVER) {
            set(0, new ItemBuilder(item.getMaterial())
                    .setDurability(item.getDurability())
                    .setName(hasPreviousPage() ? CC.GREEN + "Previous Page" : CC.RED + "First Page")
                    .setLore(CC.GRAY + (hasPreviousPage()
                            ? "Go to page " + (page - 1) + '.'
                            : "This is the first page.")
                    ).build()
            ).onClick(() -> {
                if (hasPreviousPage()) changePage(page - 1);
            });
        }

        // Next Page Button
        if (hasNextPage() || item == PaginationItem.LEVER) {
            set(8, new ItemBuilder(item.getMaterial())
                    .setDurability(item.getDurability())
                    .setName(hasNextPage() ? CC.GREEN + "Next Page" : CC.RED + "Last Page")
                    .setLore(CC.GRAY + (hasNextPage()
                            ? "Go to page " + (page + 1) + '.'
                            : "This is the last page.")
                    ).build()
            ).onClick(() -> {
                if (hasNextPage()) changePage(page + 1);
            });
        }
        buttons.putAll(globalButtons);
    }

    public void changePage(int page) {
        setPage(page);
        getButtons().clear();
        open();
    }

    public Button set(int slot, ItemStack item) {
        Button button = new Button(item);
        globalButtons.put(slot, button);
        return button;
    }

    public Button paginate(int slot, ItemStack item) {
        Button button = new Button(item);
        pageButtons.put(slot, button);
        return button;
    }

    public int getPages() {
        int buttonAmount = pageButtons.size();
        if (buttonAmount == 0) return 1;

        return (int) Math.ceil(buttonAmount / (double) itemsPerPage);
    }

    private boolean hasNextPage() {
        return getPages() > page;
    }

    private boolean hasPreviousPage() {
        return page > 1;
    }

    public void global() {
    }

    public abstract void pagination();
}
