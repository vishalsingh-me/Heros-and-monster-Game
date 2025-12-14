/**
 * Strategy interface for anything that can attack another entity.
 * Implemented by Hero and Monster.
 */
public interface Attackable {

    String getName();

    int getLevel();

    int getHealth();

    boolean isFainted();

    /**
     * Perform an attack on a target. Concrete classes decide damage formula.
     */
    void attack(Attackable target);
}
