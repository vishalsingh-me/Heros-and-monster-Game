public class Armor extends Item {
    private final int damageReduction;

    public Armor(String name, int price, int requiredLevel, int damageReduction) {
        super(name, price, requiredLevel);
        if (damageReduction < 0) {
            throw new IllegalArgumentException("Damage reduction cannot be negative");
        }
        this.damageReduction = damageReduction;
    }

    public int getDamageReduction() {
        return damageReduction;
    }
}
