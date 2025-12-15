/**
 * Sorcerer hero subclass focusing on mana and dexterity growth (good for spells).
 * Part of the required concrete hero types inheriting from {@link Hero}.
 */
public class Sorcerer extends Hero {

    /**
     * Creates a Sorcerer with the provided starting stats and currency.
     */
    public Sorcerer(String name, int level, int maxHealth, int maxMana, int strength, int dexterity, int agility,
                    int gold, int experience) {
        super(name, level, maxHealth, maxMana, strength, dexterity, agility, gold, experience);
    }

    /**
     * Applies Sorcerer-specific level-up gains: strong mana and dexterity growth,
     * moderate agility, lighter strength/health. Minimum +1 guards against stalls.
     */
    @Override
    protected void applyLevelUpGrowth() {
        int healthGain = Math.max(1, (int) (getMaxHealth() * 0.08));
        int manaGain = Math.max(1, (int) (getMaxMana() * 0.12));
        int strengthGain = Math.max(1, (int) (getStrength() * 0.05));
        int agilityGain = Math.max(1, (int) (getAgility() * 0.08));
        int dexterityGain = Math.max(1, (int) (getDexterity() * 0.12));

        setMaxHealth(getMaxHealth() + healthGain);
        setMaxMana(getMaxMana() + manaGain);
        setStrength(getStrength() + strengthGain);
        setAgility(getAgility() + agilityGain);
        setDexterity(getDexterity() + dexterityGain);
    }
}
