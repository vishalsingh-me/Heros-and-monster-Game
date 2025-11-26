public abstract class Spell extends Item {
    private final int manaCost;
    private final int baseDamage;
    private final String debuffType;
    private final double debuffAmount;

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

    public int getManaCost() {
        return manaCost;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public String getDebuffType() {
        return debuffType;
    }

    public double getDebuffAmount() {
        return debuffAmount;
    }
}
