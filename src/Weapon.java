/**
 * Weapon item with damage and hand requirement.
 */
public class Weapon extends Item {
    private final int damage;
    private final int handsRequired;

    /**
     * Builds a weapon, validating positive damage and handsRequired.
     */
    public Weapon(String name, int price, int requiredLevel, int damage, int handsRequired) {
        super(name, price, requiredLevel);
        if (damage <= 0 || handsRequired <= 0) {
            throw new IllegalArgumentException("Weapon damage and hands must be positive");
        }
        this.damage = damage;
        this.handsRequired = handsRequired;
    }

    /** @return weapon base damage */
    public int getDamage() {
        return damage;
    }

    /** @return number of hands needed to wield */
    public int getHandsRequired() {
        return handsRequired;
    }
}
