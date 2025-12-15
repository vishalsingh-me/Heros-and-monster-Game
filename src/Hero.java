/**
 * Base hero abstraction shared by all concrete hero classes.
 * Encapsulates stats, inventory/equipment, mana handling, gold/XP, and level-up flow.
 */
public abstract class Hero extends Entity {
    private int mana;
    private int maxMana;
    private int strength;
    private int dexterity;
    private int agility;
    private int gold;
    private int experience;
    private final Inventory inventory;
    private final Equipment equipment;

    /**
     * Builds a hero with combat stats, resources, and currency. Validates core attributes.
     */
    protected Hero(String name, int level, int maxHealth, int maxMana, int strength, int dexterity, int agility,
                   int gold, int experience) {
        super(name, level, maxHealth);
        if (maxMana <= 0 || strength < 0 || dexterity < 0 || agility < 0 || gold < 0 || experience < 0) {
            throw new IllegalArgumentException("Hero attributes must be non-negative and mana positive");
        }
        this.maxMana = maxMana;
        this.mana = maxMana;
        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;
        this.gold = gold;
        this.experience = experience;
        this.inventory = new Inventory();
        this.equipment = new Equipment();
    }

    /** @return current mana */
    public int getMana() {
        return mana;
    }

    /** @return maximum mana */
    public int getMaxMana() {
        return maxMana;
    }

    /**
     * Updates max mana; clamps current mana if needed.
     */
    protected void setMaxMana(int maxMana) {
        if (maxMana <= 0) {
            throw new IllegalArgumentException("Max mana must be positive");
        }
        this.maxMana = maxMana;
        if (mana > maxMana) {
            mana = maxMana;
        }
    }

    /** Restores mana to full. */
    public void restoreFullMana() {
        mana = maxMana;
    }

    /** @return true if the hero can pay the mana cost */
    public boolean hasManaFor(int cost) {
        return cost <= mana;
    }

    /** Spends mana safely (no negative cost). */
    public void spendMana(int cost) {
        if (cost < 0) {
            return;
        }
        mana = Math.max(0, mana - cost);
    }

    /** Gains mana up to the max. */
    public void gainMana(int amount) {
        if (amount < 0) {
            return;
        }
        mana = Math.min(maxMana, mana + amount);
    }

    /** @return strength value used in physical attacks */
    public int getStrength() {
        return strength;
    }

    /** Sets strength non-negative. */
    protected void setStrength(int strength) {
        this.strength = Math.max(0, strength);
    }

    /** @return dexterity (affects spells in classic game) */
    public int getDexterity() {
        return dexterity;
    }

    /** Sets dexterity non-negative. */
    protected void setDexterity(int dexterity) {
        this.dexterity = Math.max(0, dexterity);
    }

    /** @return agility (used for dodge) */
    public int getAgility() {
        return agility;
    }

    /** Sets agility non-negative. */
    protected void setAgility(int agility) {
        this.agility = Math.max(0, agility);
    }

    /** @return current gold */
    public int getGold() {
        return gold;
    }

    /** Adds gold if positive. */
    public void addGold(int amount) {
        if (amount < 0) {
            return;
        }
        gold += amount;
    }

    /**
     * Spends gold if the hero can afford it.
     *
     * @return true if the spend succeeded
     */
    public boolean spendGold(int amount) {
        if (amount < 0 || amount > gold) {
            return false;
        }
        gold -= amount;
        return true;
    }

    /** @return current experience points */
    public int getExperience() {
        return experience;
    }

    /** Adds experience if non-negative. */
    public void addExperience(int amount) {
        if (amount < 0) {
            return;
        }
        experience += amount;
    }

    /**
     * Applies a potion effect to one or more stats.
     */
    public void applyPotionEffect(int amount, java.util.Set<String> stats) {
        if (stats == null || stats.isEmpty() || amount <= 0) {
            return;
        }
        for (String stat : stats) {
            switch (stat.toLowerCase()) {
                case "health":
                    heal(amount);
                    break;
                case "mana":
                    gainMana(amount);
                    break;
                case "strength":
                    setStrength(getStrength() + amount);
                    break;
                case "dexterity":
                    setDexterity(getDexterity() + amount);
                    break;
                case "agility":
                    setAgility(getAgility() + amount);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Checks if enough XP exists to level up; repeats if multiple levels earned.
     *
     * @return true if at least one level was gained
     */
    public boolean levelUpIfReady() {
        boolean leveled = false;
        while (experience >= experienceThreshold()) {
            experience -= experienceThreshold();
            levelUp();
            leveled = true;
        }
        return leveled;
    }

    private int experienceThreshold() {
        return getLevel() * 100;
    }

    /** @return hero inventory */
    public Inventory getInventory() {
        return inventory;
    }

    /** @return hero equipment */
    public Equipment getEquipment() {
        return equipment;
    }

    /**
     * Performs the level-up: bump level, fully heal/mana, then apply subclass growth.
     */
    public final void levelUp() {
        setLevel(getLevel() + 1);
        restoreFullHealth();
        restoreFullMana();
        applyLevelUpGrowth();
    }

    /**
     * Subclasses (Warrior/Paladin/Sorcerer) define how their stats grow on level-up.
     */
    protected abstract void applyLevelUpGrowth();
}
