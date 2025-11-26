public class Weapon extends Item {
    private final int damage;
    private final int handsRequired;

    public Weapon(String name, int price, int requiredLevel, int damage, int handsRequired) {
        super(name, price, requiredLevel);
        if (damage <= 0 || handsRequired <= 0) {
            throw new IllegalArgumentException("Weapon damage and hands must be positive");
        }
        this.damage = damage;
        this.handsRequired = handsRequired;
    }

    public int getDamage() {
        return damage;
    }

    public int getHandsRequired() {
        return handsRequired;
    }
}
