/**
 * Exoskeleton monster subclass (defense-heavy archetype).
 */
public class Exoskeleton extends Monster {

    /**
     * Creates an Exoskeleton with the provided combat stats.
     */
    public Exoskeleton(String name, int level, int maxHealth, int minDamage, int maxDamage, int defense, double dodgeChance) {
        super(name, level, maxHealth, minDamage, maxDamage, defense, dodgeChance);
    }
}
