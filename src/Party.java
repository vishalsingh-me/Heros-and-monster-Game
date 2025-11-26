import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Party {
    private final List<Hero> heroes = new ArrayList<>();

    public Party(List<Hero> members) {
        if (members != null) {
            heroes.addAll(members);
        }
    }

    public List<Hero> getHeroes() {
        return Collections.unmodifiableList(heroes);
    }

    public List<Hero> aliveHeroes() {
        return heroes.stream().filter(h -> !h.isFainted()).collect(Collectors.toUnmodifiableList());
    }

    public boolean isDefeated() {
        return heroes.stream().allMatch(Hero::isFainted);
    }

    public void reviveAfterWin() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                hero.restoreFullHealth();
                hero.restoreFullMana();
            }
        }
    }
}
