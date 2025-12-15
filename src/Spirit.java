/**
 * Spirit monster subclass (evasive archetype).
 */
public class Spirit extends Monster {

    /**
     * Creates a Spirit with the provided combat stats.
     */
    public Spirit(String name, int level, int maxHealth, int minDamage, int maxDamage, int defense, double dodgeChance) {
        super(name, level, maxHealth, minDamage, maxDamage, defense, dodgeChance);
    }
}
