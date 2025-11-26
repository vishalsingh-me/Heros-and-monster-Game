import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarketFactory {
    private final WeaponLoader weaponLoader = new WeaponLoader();
    private final ArmorLoader armorLoader = new ArmorLoader();
    private final PotionLoader potionLoader = new PotionLoader();
    private final FireSpellLoader fireSpellLoader = new FireSpellLoader();
    private final IceSpellLoader iceSpellLoader = new IceSpellLoader();
    private final LightningSpellLoader lightningSpellLoader = new LightningSpellLoader();

    public Stock loadAll(Path weaponsFile, Path armorsFile, Path potionsFile,
                         Path fireSpellsFile, Path iceSpellsFile, Path lightningSpellsFile) throws IOException {
        List<Weapon> weapons = weaponLoader.load(weaponsFile);
        List<Armor> armors = armorLoader.load(armorsFile);
        List<Potion> potions = potionLoader.load(potionsFile);
        List<Spell> spells = new ArrayList<>();
        spells.addAll(fireSpellLoader.load(fireSpellsFile));
        spells.addAll(iceSpellLoader.load(iceSpellsFile));
        spells.addAll(lightningSpellLoader.load(lightningSpellsFile));

        return new Stock(weapons, armors, potions, spells);
    }

    public static class Stock {
        private final List<Weapon> weapons;
        private final List<Armor> armors;
        private final List<Potion> potions;
        private final List<Spell> spells;

        public Stock(List<Weapon> weapons, List<Armor> armors, List<Potion> potions, List<Spell> spells) {
            this.weapons = Collections.unmodifiableList(new ArrayList<>(weapons));
            this.armors = Collections.unmodifiableList(new ArrayList<>(armors));
            this.potions = Collections.unmodifiableList(new ArrayList<>(potions));
            this.spells = Collections.unmodifiableList(new ArrayList<>(spells));
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
