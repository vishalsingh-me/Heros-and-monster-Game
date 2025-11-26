import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Inventory {
    private final List<Item> items = new ArrayList<>();

    public void add(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    public boolean remove(Item item) {
        if (item == null) {
            return false;
        }
        return items.remove(item);
    }

    public List<Item> getAll() {
        return Collections.unmodifiableList(items);
    }

    public List<Item> getByType(Class<? extends Item> type) {
        return items.stream()
                .filter(i -> type.isAssignableFrom(i.getClass()))
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean contains(Item item) {
        return items.contains(item);
    }
}
