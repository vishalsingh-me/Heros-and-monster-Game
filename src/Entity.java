/**
 * Base entity for heroes and monsters. Holds name, level, and health management.
 */
public abstract class Entity {
    private final String name;
    private int level;
    private int health;
    private int maxHealth;

    /**
     * Builds an entity with basic validation and full health to start.
     */
    protected Entity(String name, int level, int maxHealth) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (level <= 0 || maxHealth <= 0) {
            throw new IllegalArgumentException("Level and health must be positive");
        }
        this.name = name;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    /** @return entity name */
    public String getName() {
        return name;
    }

    /** @return current level */
    public int getLevel() {
        return level;
    }

    /** Sets level, enforcing positivity. */
    protected void setLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("Level must be positive");
        }
        this.level = level;
    }

    /** @return current health */
    public int getHealth() {
        return health;
    }

    /** @return max health */
    public int getMaxHealth() {
        return maxHealth;
    }

    /** Sets max health, clamping current health if necessary. */
    protected void setMaxHealth(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        this.maxHealth = maxHealth;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    /** @return true if health is zero or below */
    public boolean isFainted() {
        return health <= 0;
    }

    /** Applies damage safely (no negatives), not dropping below 0. */
    public void takeDamage(int amount) {
        if (amount < 0) {
            return;
        }
        health = Math.max(0, health - amount);
    }

    /** Heals up to max health unless already fainted. */
    public void heal(int amount) {
        if (amount < 0 || isFainted()) {
            return;
        }
        health = Math.min(maxHealth, health + amount);
    }

    /** Restores to full health. */
    public void restoreFullHealth() {
        health = maxHealth;
    }
}
