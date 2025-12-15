/**
 * Lightning spell that reduces enemy dodge chance.
 */
public class LightningSpell extends Spell {
    public static final String DEBUFF_TYPE = "dodge";

    /**
     * Builds a lightning spell with damage, mana cost, and debuff amount.
     */
    public LightningSpell(String name, int price, int requiredLevel, int manaCost, int baseDamage, double debuffAmount) {
        super(name, price, requiredLevel, manaCost, baseDamage, DEBUFF_TYPE, debuffAmount);
    }
}
