/**
 * Fire spell that reduces enemy defense.
 */
public class FireSpell extends Spell {
    public static final String DEBUFF_TYPE = "defense";

    /**
     * Builds a fire spell with damage, mana cost, and debuff amount.
     */
    public FireSpell(String name, int price, int requiredLevel, int manaCost, int baseDamage, double debuffAmount) {
        super(name, price, requiredLevel, manaCost, baseDamage, DEBUFF_TYPE, debuffAmount);
    }
}
