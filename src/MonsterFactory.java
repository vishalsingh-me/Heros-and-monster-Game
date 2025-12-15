import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Factory for loading and spawning monsters from data files.
 * I wrote it this way so there is a single place to read monster stats and to create fresh
 * monster instances for battles without mutating the templates.
 */
public class MonsterFactory {
    private final DragonLoader dragonLoader = new DragonLoader();
    private final ExoskeletonLoader exoskeletonLoader = new ExoskeletonLoader();
    private final SpiritLoader spiritLoader = new SpiritLoader();
    private final Random random = new Random();

    /**
     * Loads all monster templates from the three data files.
     *
     * @param dragonsFile       path to dragon data
     * @param exoskeletonsFile  path to exoskeleton data
     * @param spiritsFile       path to spirit data
     * @return unmodifiable list of monster templates
     * @throws IOException if any file cannot be read
     */
    public List<Monster> loadAll(Path dragonsFile, Path exoskeletonsFile, Path spiritsFile) throws IOException {
        List<Monster> monsters = new ArrayList<Monster>();
        monsters.addAll(dragonLoader.load(dragonsFile));
        monsters.addAll(exoskeletonLoader.load(exoskeletonsFile));
        monsters.addAll(spiritLoader.load(spiritsFile));
        return Collections.unmodifiableList(monsters);
    }

    /**
     * Spawns a number of monsters near the requested level. If no exact match exists,
     * it picks the closest level difference. Each spawned monster is a clone so the
     * templates stay untouched.
     *
     * @param pool   all loaded monster templates
     * @param level  desired level to match
     * @param count  how many to spawn
     * @return fresh monster instances ready for placement
     */
    public List<Monster> spawnForLevel(List<Monster> pool, int level, int count) {
        List<Monster> candidates = pool.stream()
                .filter(m -> m.getLevel() == level)
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            int closestDiff = pool.stream()
                    .mapToInt(m -> Math.abs(m.getLevel() - level))
                    .min()
                    .orElse(Integer.MAX_VALUE);
            candidates = pool.stream()
                    .filter(m -> Math.abs(m.getLevel() - level) == closestDiff)
                    .collect(Collectors.toList());
        }
        List<Monster> spawned = new ArrayList<Monster>();
        for (int i = 0; i < count; i++) {
            if (candidates.isEmpty()) {
                break;
            }
            Monster template = candidates.get(random.nextInt(candidates.size()));
            spawned.add(cloneMonster(template));
        }
        return spawned;
    }

    /**
     * Clones a monster template into a new instance, preserving stats but not sharing state.
     *
     * @param template monster template to copy
     * @return new monster instance
     */
    private Monster cloneMonster(Monster template) {
        if (template instanceof Dragon) {
            Dragon d = (Dragon) template;
            return new Dragon(d.getName(), d.getLevel(), d.getMaxHealth(), d.getMinDamage(), d.getMaxDamage(),
                    d.getDefense(), d.getDodgeChance());
        }
        if (template instanceof Exoskeleton) {
            Exoskeleton e = (Exoskeleton) template;
            return new Exoskeleton(e.getName(), e.getLevel(), e.getMaxHealth(), e.getMinDamage(), e.getMaxDamage(),
                    e.getDefense(), e.getDodgeChance());
        }
        if (template instanceof Spirit) {
            Spirit s = (Spirit) template;
            return new Spirit(s.getName(), s.getLevel(), s.getMaxHealth(), s.getMinDamage(), s.getMaxDamage(),
                    s.getDefense(), s.getDodgeChance());
        }
        throw new IllegalArgumentException("Unknown monster type");
    }
}
