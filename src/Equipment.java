/**
 * Tracks currently equipped weapon and armor for a hero, plus hands usage.
 */
public class Equipment {
    private Weapon weapon;
    private Armor armor;
    private int handsUsed;

    /**
     * Equips a weapon if valid, resetting any previous weapon and updating hands used.
     *
     * @return true if equipped
     */
    public boolean equipWeapon(Weapon newWeapon) {
        if (newWeapon == null) {
            return false;
        }
        int requiredHands = newWeapon.getHandsRequired();
        if (requiredHands <= 0 || requiredHands > 2) {
            return false;
        }
        // Always unequip the current weapon before equipping a new one.
        unequipWeapon();
        weapon = newWeapon;
        handsUsed = requiredHands;
        return true;
    }

    /**
     * Equips armor, replacing any previous armor.
     *
     * @return true if equipped
     */
    public boolean equipArmor(Armor newArmor) {
        if (newArmor == null) {
            return false;
        }
        armor = newArmor;
        return true;
    }

    /** Unequips the weapon and frees hands. */
    public void unequipWeapon() {
        weapon = null;
        handsUsed = 0;
    }

    /** Unequips armor. */
    public void unequipArmor() {
        armor = null;
    }

    /** @return currently equipped weapon, or null */
    public Weapon getWeapon() {
        return weapon;
    }

    /** @return currently equipped armor, or null */
    public Armor getArmor() {
        return armor;
    }

    /** @return number of hands occupied by the current weapon */
    public int getHandsUsed() {
        return handsUsed;
    }
}
