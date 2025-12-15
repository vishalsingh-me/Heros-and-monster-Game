/**
 * Paladin hero subclass specializing in balanced strength and dexterity gains.
 * Fits the rubric requirement for second-level inheritance where each concrete hero
 * extends the base {@link Hero} class with class-specific level-up growth rules.
 */
public class Paladin extends Hero {

    /**
     * Creates a Paladin with the provided base stats and currency values.
     *
     * @param name        hero name
     * @param level       starting level
     * @param maxHealth   maximum health points
     * @param maxMana     maximum mana points
     * @param strength    base strength
     * @param dexterity   base dexterity
     * @param agility     base agility
     * @param gold        starting gold
     * @param experience  starting experience
     */
    public Paladin(String name, int level, int maxHealth, int maxMana, int strength, int dexterity, int agility,
                   int gold, int experience) {
        super(name, level, maxHealth, maxMana, strength, dexterity, agility, gold, experience);
    }

    /**
     * Applies Paladin-specific level-up growth: strong gains to strength and dexterity,
     * moderate health and mana increases, modest agility gain. Ensures at least 1 point
     * is added per stat to avoid stagnant progression at low values.
     */
    @Override
    protected void applyLevelUpGrowth() {
        int healthGain = Math.max(1, (int) (getMaxHealth() * 0.1));
        int manaGain = Math.max(1, (int) (getMaxMana() * 0.08));
        int strengthGain = Math.max(1, (int) (getStrength() * 0.1));
        int agilityGain = Math.max(1, (int) (getAgility() * 0.06));
        int dexterityGain = Math.max(1, (int) (getDexterity() * 0.1));

        setMaxHealth(getMaxHealth() + healthGain);
        setMaxMana(getMaxMana() + manaGain);
        setStrength(getStrength() + strengthGain);
        setAgility(getAgility() + agilityGain);
        setDexterity(getDexterity() + dexterityGain);
    }
}
