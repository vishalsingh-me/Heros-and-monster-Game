/**
 * Armor item that reduces incoming physical damage.
 */
public class Armor extends Item {
    private final int damageReduction;

    /**
     * Builds armor with a non-negative damage reduction value.
     */
    public Armor(String name, int price, int requiredLevel, int damageReduction) {
        super(name, price, requiredLevel);
        if (damageReduction < 0) {
            throw new IllegalArgumentException("Damage reduction cannot be negative");
        }
        this.damageReduction = damageReduction;
    }

    /** @return reduction amount applied to attacks */
    public int getDamageReduction() {
        return damageReduction;
    }
}
