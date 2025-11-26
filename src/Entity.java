public abstract class Entity {
    private final String name;
    private int level;
    private int health;
    private int maxHealth;

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

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    protected void setLevel(int level) {
        if (level <= 0) {
            throw new IllegalArgumentException("Level must be positive");
        }
        this.level = level;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    protected void setMaxHealth(int maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        this.maxHealth = maxHealth;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }

    public boolean isFainted() {
        return health <= 0;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            return;
        }
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        if (amount < 0 || isFainted()) {
            return;
        }
        health = Math.min(maxHealth, health + amount);
    }

    public void restoreFullHealth() {
        health = maxHealth;
    }
}
