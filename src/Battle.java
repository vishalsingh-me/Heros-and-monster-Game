import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Battle {
    private final Party party;
    private final List<Monster> monsters;
    private final Random random = new Random();

    public Battle(Party party, List<Monster> monsters) {
        this.party = party;
        this.monsters = new ArrayList<>(monsters);
    }

    public boolean isOver() {
        return party.isDefeated() || monsters.stream().allMatch(Monster::isFainted);
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    public void heroAttack(Hero hero, Monster target) {
        if (hero == null || target == null || hero.isFainted() || target.isFainted()) {
            return;
        }
        int weaponDamage = hero.getEquipment().getWeapon() != null ? hero.getEquipment().getWeapon().getDamage() : 0;
        int base = hero.getStrength() + weaponDamage;
        int damage = Math.max(1, base - target.getDefense());
        if (rollDodge(target.getDodgeChance())) {
            return;
        }
        target.takeDamage(damage);
    }

    public void monsterAttack(Monster monster, Hero target) {
        if (monster == null || target == null || monster.isFainted() || target.isFainted()) {
            return;
        }
        int damage = random.nextInt(monster.getMaxDamage() - monster.getMinDamage() + 1) + monster.getMinDamage();
        Armor armor = target.getEquipment().getArmor();
        if (armor != null) {
            damage = Math.max(0, damage - armor.getDamageReduction());
        }
        if (rollDodgeChance(target.getAgility())) {
            return;
        }
        target.takeDamage(damage);
    }

    public void castSpell(Hero hero, Spell spell, Monster target) {
        if (hero == null || spell == null || target == null || hero.isFainted() || target.isFainted()) {
            return;
        }
        if (!hero.hasManaFor(spell.getManaCost())) {
            return;
        }
        hero.spendMana(spell.getManaCost());
        int damage = spell.getBaseDamage() + (int) (hero.getDexterity() * 0.1);
        if (rollDodge(target.getDodgeChance())) {
            return;
        }
        target.takeDamage(damage);
        applyDebuff(spell, target);
    }

    private void applyDebuff(Spell spell, Monster target) {
        String type = spell.getDebuffType();
        double amount = spell.getDebuffAmount();
        switch (type) {
            case FireSpell.DEBUFF_TYPE -> target.setDefense(Math.max(0, (int) (target.getDefense() - amount)));
            case IceSpell.DEBUFF_TYPE -> {
                int newMin = Math.max(0, (int) (target.getMinDamage() - amount));
                int newMax = Math.max(newMin, (int) (target.getMaxDamage() - amount));
                target.setDamageRange(newMin, newMax);
            }
            case LightningSpell.DEBUFF_TYPE -> target.setDodgeChance(Math.max(0, target.getDodgeChance() - amount));
            default -> {
            }
        }
    }

    private boolean rollDodge(double dodgeChance) {
        return random.nextDouble() < (dodgeChance / 100.0);
    }

    private boolean rollDodgeChance(int agility) {
        double chance = Math.min(0.5, agility / 1000.0);
        return random.nextDouble() < chance;
    }
}
