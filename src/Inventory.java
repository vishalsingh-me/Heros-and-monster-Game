import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple inventory container for items. Keeps a mutable list internally but exposes
 * unmodifiable views to callers.
 */
public class Inventory {
    private final List<Item> items = new ArrayList<>();

    /** Adds an item if non-null. */
    public void add(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    /** Removes the item if present. */
    public boolean remove(Item item) {
        if (item == null) {
            return false;
        }
        return items.remove(item);
    }

    /** @return read-only view of all items */
    public List<Item> getAll() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Filters inventory by subtype (e.g., Weapon.class) and returns an unmodifiable list.
     */
    public List<Item> getByType(Class<? extends Item> type) {
        return items.stream()
                .filter(i -> type.isAssignableFrom(i.getClass()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /** @return true if the item is contained */
    public boolean contains(Item item) {
        return items.contains(item);
    }
}
