public class Equipment {
    private Weapon weapon;
    private Armor armor;
    private int handsUsed;

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

    public boolean equipArmor(Armor newArmor) {
        if (newArmor == null) {
            return false;
        }
        armor = newArmor;
        return true;
    }

    public void unequipWeapon() {
        weapon = null;
        handsUsed = 0;
    }

    public void unequipArmor() {
        armor = null;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public Armor getArmor() {
        return armor;
    }

    public int getHandsUsed() {
        return handsUsed;
    }
}
