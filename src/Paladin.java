public class Paladin extends Hero {

    public Paladin(String name, int level, int maxHealth, int maxMana, int strength, int dexterity, int agility,
                   int gold, int experience) {
        super(name, level, maxHealth, maxMana, strength, dexterity, agility, gold, experience);
    }

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
