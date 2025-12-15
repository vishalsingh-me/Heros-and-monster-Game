import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Potion item that boosts one or more stats by a fixed amount.
 */
public class Potion extends Item {
    private final int effectAmount;
    private final Set<String> affectedStats;

    /**
     * Builds a potion; validates positive effect and non-empty affected stats set.
     */
    public Potion(String name, int price, int requiredLevel, int effectAmount, Set<String> affectedStats) {
        super(name, price, requiredLevel);
        if (effectAmount <= 0) {
            throw new IllegalArgumentException("Effect amount must be positive");
        }
        if (affectedStats == null || affectedStats.isEmpty()) {
            throw new IllegalArgumentException("Potion must affect at least one stat");
        }
        this.effectAmount = effectAmount;
        this.affectedStats = Collections.unmodifiableSet(new HashSet<>(affectedStats));
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    /** @return immutable set of stat names affected by this potion */
    public Set<String> getAffectedStats() {
        return affectedStats;
    }
}
