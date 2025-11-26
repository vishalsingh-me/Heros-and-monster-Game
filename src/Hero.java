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

    public int getMana() {
        return mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    protected void setMaxMana(int maxMana) {
        if (maxMana <= 0) {
            throw new IllegalArgumentException("Max mana must be positive");
        }
        this.maxMana = maxMana;
        if (mana > maxMana) {
            mana = maxMana;
        }
    }

    public void restoreFullMana() {
        mana = maxMana;
    }

    public boolean hasManaFor(int cost) {
        return cost <= mana;
    }

    public void spendMana(int cost) {
        if (cost < 0) {
            return;
        }
        mana = Math.max(0, mana - cost);
    }

    public void gainMana(int amount) {
        if (amount < 0) {
            return;
        }
        mana = Math.min(maxMana, mana + amount);
    }

    public int getStrength() {
        return strength;
    }

    protected void setStrength(int strength) {
        this.strength = Math.max(0, strength);
    }

    public int getDexterity() {
        return dexterity;
    }

    protected void setDexterity(int dexterity) {
        this.dexterity = Math.max(0, dexterity);
    }

    public int getAgility() {
        return agility;
    }

    protected void setAgility(int agility) {
        this.agility = Math.max(0, agility);
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        if (amount < 0) {
            return;
        }
        gold += amount;
    }

    public boolean spendGold(int amount) {
        if (amount < 0 || amount > gold) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public int getExperience() {
        return experience;
    }

    public void addExperience(int amount) {
        if (amount < 0) {
            return;
        }
        experience += amount;
    }

    public void applyPotionEffect(int amount, java.util.Set<String> stats) {
        if (stats == null || stats.isEmpty() || amount <= 0) {
            return;
        }
        for (String stat : stats) {
            switch (stat.toLowerCase()) {
                case "health" -> heal(amount);
                case "mana" -> gainMana(amount);
                case "strength" -> setStrength(getStrength() + amount);
                case "dexterity" -> setDexterity(getDexterity() + amount);
                case "agility" -> setAgility(getAgility() + amount);
                default -> {
                }
            }
        }
    }

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

    public Inventory getInventory() {
        return inventory;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public final void levelUp() {
        setLevel(getLevel() + 1);
        restoreFullHealth();
        restoreFullMana();
        applyLevelUpGrowth();
    }

    protected abstract void applyLevelUpGrowth();
}
