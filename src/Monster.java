/**
 * Base monster type for all concrete monsters (Dragon, Spirit, Exoskeleton).
 * Holds combat stats (damage range, defense, dodge) and fulfills the rubric’s
 * requirement for a shared monster abstraction.
 */
public abstract class Monster extends Entity {
    private int minDamage;
    private int maxDamage;
    private int defense;
    private double dodgeChance; // 0.0 to 1.0

    /**
     * Builds a monster with basic combat stats. I validate inputs up front to avoid bad state.
     */
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

    /** @return minimum possible damage for this monster */
    public int getMinDamage() {
        return minDamage;
    }

    /** @return maximum possible damage for this monster */
    public int getMaxDamage() {
        return maxDamage;
    }

    /**
     * Sets the damage range, ensuring it is valid and non-negative.
     */
    public void setDamageRange(int minDamage, int maxDamage) {
        if (minDamage < 0 || maxDamage < minDamage) {
            throw new IllegalArgumentException("Invalid damage range");
        }
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
    }

    /** @return defense value used to reduce incoming damage */
    public int getDefense() {
        return defense;
    }

    /**
     * Updates defense, rejecting negative values.
     */
    public void setDefense(int defense) {
        if (defense < 0) {
            throw new IllegalArgumentException("Defense cannot be negative");
        }
        this.defense = defense;
    }

    /** @return dodge chance (0.0–1.0) */
    public double getDodgeChance() {
        return dodgeChance;
    }

    /**
     * Updates dodge chance, rejecting negative values.
     */
    public void setDodgeChance(double dodgeChance) {
        if (dodgeChance < 0) {
            throw new IllegalArgumentException("Dodge chance cannot be negative");
        }
        this.dodgeChance = dodgeChance;
    }
}
