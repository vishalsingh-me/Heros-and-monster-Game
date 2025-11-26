public class Sorcerer extends Hero {

    public Sorcerer(String name, int level, int maxHealth, int maxMana, int strength, int dexterity, int agility,
                    int gold, int experience) {
        super(name, level, maxHealth, maxMana, strength, dexterity, agility, gold, experience);
    }

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
