/**
 * Base spell item with mana cost, base damage, and a debuff effect.
 * Concrete spells (fire/ice/lightning) extend this to set their debuff type.
 */
public abstract class Spell extends Item {
    private final int manaCost;
    private final int baseDamage;
    private final String debuffType;
    private final double debuffAmount;

    /**
     * Builds a spell, validating positive cost/damage and a non-empty debuff type.
     */
    protected Spell(String name, int price, int requiredLevel, int manaCost, int baseDamage,
                    String debuffType, double debuffAmount) {
        super(name, price, requiredLevel);
        if (manaCost <= 0 || baseDamage <= 0) {
            throw new IllegalArgumentException("Spell mana cost and damage must be positive");
        }
        if (debuffType == null || debuffType.isEmpty()) {
            throw new IllegalArgumentException("Debuff type cannot be empty");
        }
        if (debuffAmount < 0) {
            throw new IllegalArgumentException("Debuff amount cannot be negative");
        }
        this.manaCost = manaCost;
        this.baseDamage = baseDamage;
        this.debuffType = debuffType;
        this.debuffAmount = debuffAmount;
    }

    /** @return mana cost to cast the spell */
    public int getManaCost() {
        return manaCost;
    }

    /** @return base damage before hero modifiers */
    public int getBaseDamage() {
        return baseDamage;
    }

    /** @return debuff type keyword (defense/damage/dodge) */
    public String getDebuffType() {
        return debuffType;
    }

    /** @return magnitude of the debuff */
    public double getDebuffAmount() {
        return debuffAmount;
    }
}
