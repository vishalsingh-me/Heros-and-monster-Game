/**
 * Ice spell that reduces enemy damage output.
 */
public class IceSpell extends Spell {
    public static final String DEBUFF_TYPE = "damage";

    /**
     * Builds an ice spell with damage, mana cost, and debuff amount.
     */
    public IceSpell(String name, int price, int requiredLevel, int manaCost, int baseDamage, double debuffAmount) {
        super(name, price, requiredLevel, manaCost, baseDamage, DEBUFF_TYPE, debuffAmount);
    }
}
