public abstract class Monster extends Entity {
    private int minDamage;
    private int maxDamage;
    private int defense;
    private double dodgeChance; // 0.0 to 1.0

    protected Monster(String name, int level, int maxHealth, int minDamage, int maxDamage, int defense, double dodgeChance) {
        super(name, level, maxHealth);
        if (minDamage < 0 || maxDamage < minDamage || defense < 0 || dodgeChance < 0) {
            throw new IllegalArgumentException("Invalid monster attributes");
        }
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.defense = defense;
        this.dodgeChance = dodgeChance;
    }

    public int getMinDamage() {
        return minDamage;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public void setDamageRange(int minDamage, int maxDamage) {
        if (minDamage < 0 || maxDamage < minDamage) {
            throw new IllegalArgumentException("Invalid damage range");
        }
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        if (defense < 0) {
            throw new IllegalArgumentException("Defense cannot be negative");
        }
        this.defense = defense;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public void setDodgeChance(double dodgeChance) {
        if (dodgeChance < 0) {
            throw new IllegalArgumentException("Dodge chance cannot be negative");
        }
        this.dodgeChance = dodgeChance;
    }
}
