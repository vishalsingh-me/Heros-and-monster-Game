import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the player's party of heroes in the classic game.
 * Provides helpers to check defeat, list alive heroes, and revive after victories.
 */
public class Party {
    private final List<Hero> heroes = new ArrayList<Hero>();

    /**
     * Builds a party from the provided heroes.
     */
    public Party(List<Hero> members) {
        if (members != null) {
            heroes.addAll(members);
        }
    }

    /** @return read-only view of heroes */
    public List<Hero> getHeroes() {
        return Collections.unmodifiableList(heroes);
    }

    /** @return read-only list of heroes that are not fainted */
    public List<Hero> aliveHeroes() {
        return heroes.stream()
                .filter(h -> !h.isFainted())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /** @return true if every hero is fainted */
    public boolean isDefeated() {
        return heroes.stream().allMatch(Hero::isFainted);
    }

    /** Restores full HP/MP to fainted heroes after a win. */
    public void reviveAfterWin() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                hero.restoreFullHealth();
                hero.restoreFullMana();
            }
        }
    }
}
