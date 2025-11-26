import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MonsterFactory {
    private final DragonLoader dragonLoader = new DragonLoader();
    private final ExoskeletonLoader exoskeletonLoader = new ExoskeletonLoader();
    private final SpiritLoader spiritLoader = new SpiritLoader();
    private final Random random = new Random();

    public List<Monster> loadAll(Path dragonsFile, Path exoskeletonsFile, Path spiritsFile) throws IOException {
        List<Monster> monsters = new ArrayList<>();
        monsters.addAll(dragonLoader.load(dragonsFile));
        monsters.addAll(exoskeletonLoader.load(exoskeletonsFile));
        monsters.addAll(spiritLoader.load(spiritsFile));
        return Collections.unmodifiableList(monsters);
    }

    public List<Monster> spawnForLevel(List<Monster> pool, int level, int count) {
        List<Monster> candidates = pool.stream()
                .filter(m -> m.getLevel() == level)
                .toList();
        if (candidates.isEmpty()) {
            int closestDiff = pool.stream()
                    .mapToInt(m -> Math.abs(m.getLevel() - level))
                    .min()
                    .orElse(Integer.MAX_VALUE);
            candidates = pool.stream()
                    .filter(m -> Math.abs(m.getLevel() - level) == closestDiff)
                    .toList();
        }
        List<Monster> spawned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (candidates.isEmpty()) {
                break;
            }
            Monster template = candidates.get(random.nextInt(candidates.size()));
            spawned.add(cloneMonster(template));
        }
        return spawned;
    }

    private Monster cloneMonster(Monster template) {
        if (template instanceof Dragon d) {
            return new Dragon(d.getName(), d.getLevel(), d.getMaxHealth(), d.getMinDamage(), d.getMaxDamage(),
                    d.getDefense(), d.getDodgeChance());
        }
        if (template instanceof Exoskeleton e) {
            return new Exoskeleton(e.getName(), e.getLevel(), e.getMaxHealth(), e.getMinDamage(), e.getMaxDamage(),
                    e.getDefense(), e.getDodgeChance());
        }
        if (template instanceof Spirit s) {
            return new Spirit(s.getName(), s.getLevel(), s.getMaxHealth(), s.getMinDamage(), s.getMaxDamage(),
                    s.getDefense(), s.getDodgeChance());
        }
        throw new IllegalArgumentException("Unknown monster type");
    }
}
