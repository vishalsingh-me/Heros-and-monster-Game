/**
 * Dragon monster subclass (high damage archetype).
 * Part of the required concrete monsters extending {@link Monster}.
 */
public class Dragon extends Monster {

    /**
     * Creates a Dragon with the given combat stats.
     */
    public Dragon(String name, int level, int maxHealth, int minDamage, int maxDamage, int defense, double dodgeChance) {
        super(name, level, maxHealth, minDamage, maxDamage, defense, dodgeChance);
    }
}
