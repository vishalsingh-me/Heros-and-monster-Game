import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Market {
    private final List<Weapon> weapons;
    private final List<Armor> armors;
    private final List<Potion> potions;
    private final List<Spell> spells;

    public Market(List<Weapon> weapons, List<Armor> armors, List<Potion> potions, List<Spell> spells) {
        this.weapons = new ArrayList<>(weapons);
        this.armors = new ArrayList<>(armors);
        this.potions = new ArrayList<>(potions);
        this.spells = new ArrayList<>(spells);
    }

    public List<Weapon> getWeapons() {
        return Collections.unmodifiableList(weapons);
    }

    public List<Armor> getArmors() {
        return Collections.unmodifiableList(armors);
    }

    public List<Potion> getPotions() {
        return Collections.unmodifiableList(potions);
    }

    public List<Spell> getSpells() {
        return Collections.unmodifiableList(spells);
    }

    public boolean canBuy(Hero hero, Item item) {
        return hero.getLevel() >= item.getRequiredLevel() && hero.getGold() >= item.getPrice();
    }

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
