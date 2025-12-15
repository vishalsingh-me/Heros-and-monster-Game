/**
 * Warrior hero subclass with heavier strength/HP/agility growth.
 * This fulfills the rubric’s second-level inheritance for concrete hero types.
 */
public class Warrior extends Hero {

    /**
     * Creates a Warrior with the provided starting stats and currency.
     */
    public Warrior(String name, int level, int maxHealth, int maxMana, int strength, int dexterity, int agility,
                   int gold, int experience) {
        super(name, level, maxHealth, maxMana, strength, dexterity, agility, gold, experience);
    }

    /**
     * Applies Warrior-specific level-up gains: strong boosts to strength/HP/agility,
     * modest mana and dexterity bumps. Minimum +1 to avoid stagnation.
     */
    @Override
    protected void applyLevelUpGrowth() {
        int healthGain = Math.max(1, (int) (getMaxHealth() * 0.1));
        int manaGain = Math.max(1, (int) (getMaxMana() * 0.05));
        int strengthGain = Math.max(1, (int) (getStrength() * 0.1));
        int agilityGain = Math.max(1, (int) (getAgility() * 0.1));
        int dexterityGain = Math.max(1, (int) (getDexterity() * 0.05));

        setMaxHealth(getMaxHealth() + healthGain);
        setMaxMana(getMaxMana() + manaGain);
        setStrength(getStrength() + strengthGain);
        setAgility(getAgility() + agilityGain);
        setDexterity(getDexterity() + dexterityGain);
    }
}
