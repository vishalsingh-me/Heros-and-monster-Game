import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory for loading all market inventory (weapons, armors, potions, spells) from data files.
 * I’m keeping it here so every game mode can share the same stocked market without duplicating I/O.
 */
public class MarketFactory {
    private final WeaponLoader weaponLoader = new WeaponLoader();
    private final ArmorLoader armorLoader = new ArmorLoader();
    private final PotionLoader potionLoader = new PotionLoader();
    private final FireSpellLoader fireSpellLoader = new FireSpellLoader();
    private final IceSpellLoader iceSpellLoader = new IceSpellLoader();
    private final LightningSpellLoader lightningSpellLoader = new LightningSpellLoader();

    /**
     * Loads all item categories from the given file paths and wraps them in a Stock container.
     *
     * @param weaponsFile       path to weapons data
     * @param armorsFile        path to armors data
     * @param potionsFile       path to potions data
     * @param fireSpellsFile    path to fire spells data
     * @param iceSpellsFile     path to ice spells data
     * @param lightningSpellsFile path to lightning spells data
     * @return stocked market data as an immutable bundle
     * @throws IOException if any file cannot be read
     */
    public Stock loadAll(Path weaponsFile, Path armorsFile, Path potionsFile,
                         Path fireSpellsFile, Path iceSpellsFile, Path lightningSpellsFile) throws IOException {
        List<Weapon> weapons = weaponLoader.load(weaponsFile);
        List<Armor> armors = armorLoader.load(armorsFile);
        List<Potion> potions = potionLoader.load(potionsFile);
        List<Spell> spells = new ArrayList<Spell>();
        spells.addAll(fireSpellLoader.load(fireSpellsFile));
        spells.addAll(iceSpellLoader.load(iceSpellsFile));
        spells.addAll(lightningSpellLoader.load(lightningSpellsFile));

        return new Stock(weapons, armors, potions, spells);
    }

    /**
     * Immutable bundle of loaded market items so callers can stock their markets safely.
     */
    public static class Stock {
        private final List<Weapon> weapons;
        private final List<Armor> armors;
        private final List<Potion> potions;
        private final List<Spell> spells;

        /**
         * Wraps provided item lists as unmodifiable lists to prevent accidental mutation.
         *
         * @param weapons list of weapons
         * @param armors  list of armors
         * @param potions list of potions
         * @param spells  list of spells
         */
        public Stock(List<Weapon> weapons, List<Armor> armors, List<Potion> potions, List<Spell> spells) {
            this.weapons = Collections.unmodifiableList(new ArrayList<Weapon>(weapons));
            this.armors = Collections.unmodifiableList(new ArrayList<Armor>(armors));
            this.potions = Collections.unmodifiableList(new ArrayList<Potion>(potions));
            this.spells = Collections.unmodifiableList(new ArrayList<Spell>(spells));
        }

        public List<Weapon> getWeapons() {
            return weapons;
        }

        public List<Armor> getArmors() {
            return armors;
        }

        public List<Potion> getPotions() {
            return potions;
        }

        public List<Spell> getSpells() {
            return spells;
        }
    }
}
