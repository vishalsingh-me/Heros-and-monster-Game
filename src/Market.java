import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple market that holds weapons, armors, potions, and spells and lets heroes buy/sell them.
 * I keep the lists defensive-copied so the underlying stock isn’t mutated by callers.
 */
public class Market {
    private final List<Weapon> weapons;
    private final List<Armor> armors;
    private final List<Potion> potions;
    private final List<Spell> spells;

    /**
     * Builds a market with the given item stock. Copies the lists to protect internal state.
     */
    public Market(List<Weapon> weapons, List<Armor> armors, List<Potion> potions, List<Spell> spells) {
        this.weapons = new ArrayList<Weapon>(weapons);
        this.armors = new ArrayList<Armor>(armors);
        this.potions = new ArrayList<Potion>(potions);
        this.spells = new ArrayList<Spell>(spells);
    }

    /** @return unmodifiable view of available weapons */
    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    /** @return unmodifiable view of available armors */
    public List<Armor> getArmors() {
        return Collections.unmodifiableList(armors);
    }

    /** @return unmodifiable view of available potions */
    public List<Potion> getPotions() {
        return Collections.unmodifiableList(potions);
    }

    /** @return unmodifiable view of available spells */
    public List<Spell> getSpells() {
        return Collections.unmodifiableList(spells);
    }

    /**
     * Checks if the hero meets level and gold requirements for the item.
     */
    public boolean canBuy(Hero hero, Item item) {
        return hero.getLevel() >= item.getRequiredLevel() && hero.getGold() >= item.getPrice();
    }

    /**
     * Attempts to buy an item: validates, spends gold, and adds to inventory.
     *
     * @return true if purchase succeeded
     */
    public boolean buy(Hero hero, Item item) {
        if (hero == null || item == null) {
            return false;
        }
        if (!canBuy(hero, item)) {
            return false;
        }
        if (!hero.spendGold(item.getPrice())) {
            return false;
        }
        hero.getInventory().add(item);
        return true;
    }

    /**
     * Sells an item from the hero’s inventory for half price if present.
     *
     * @return true if the sale was completed
     */
    public boolean sell(Hero hero, Item item) {
        if (hero == null || item == null) {
            return false;
        }
        Inventory inventory = hero.getInventory();
        if (!inventory.contains(item)) {
            return false;
        }
        inventory.remove(item);
        hero.addGold(item.getPrice() / 2);
        return true;
    }
}
