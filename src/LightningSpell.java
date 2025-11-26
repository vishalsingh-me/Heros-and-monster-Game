public class LightningSpell extends Spell {
    public static final String DEBUFF_TYPE = "dodge";

    public LightningSpell(String name, int price, int requiredLevel, int manaCost, int baseDamage, double debuffAmount) {
        super(name, price, requiredLevel, manaCost, baseDamage, DEBUFF_TYPE, debuffAmount);
    }
}
