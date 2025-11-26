import java.util.Set;

public class Potion extends Item {
    private final int effectAmount;
    private final Set<String> affectedStats;

    public Potion(String name, int price, int requiredLevel, int effectAmount, Set<String> affectedStats) {
        super(name, price, requiredLevel);
        if (effectAmount <= 0) {
            throw new IllegalArgumentException("Effect amount must be positive");
        }
        if (affectedStats == null || affectedStats.isEmpty()) {
            throw new IllegalArgumentException("Potion must affect at least one stat");
        }
        this.effectAmount = effectAmount;
        this.affectedStats = Set.copyOf(affectedStats);
    }

    public int getEffectAmount() {
        return effectAmount;
    }

    public Set<String> getAffectedStats() {
        return affectedStats;
    }
}
