public abstract class Item {
    private final String name;
    private final int price;
    private final int requiredLevel;

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

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
