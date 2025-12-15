import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory for loading all hero templates from their data files.
 * Keeps hero loading centralized for both game modes.
 */
public class HeroFactory {
    private final PaladinLoader paladinLoader = new PaladinLoader();
    private final SorcererLoader sorcererLoader = new SorcererLoader();
    private final WarriorLoader warriorLoader = new WarriorLoader();

    /**
     * Loads all hero types from their respective files and returns an immutable list.
     *
     * @param paladinsFile  path to paladin data
     * @param sorcerersFile path to sorcerer data
     * @param warriorsFile  path to warrior data
     * @return unmodifiable list of hero templates
     * @throws IOException if any file cannot be read
     */
    public List<Hero> loadAll(Path paladinsFile, Path sorcerersFile, Path warriorsFile) throws IOException {
        List<Hero> heroes = new ArrayList<Hero>();
        heroes.addAll(paladinLoader.load(paladinsFile));
        heroes.addAll(sorcererLoader.load(sorcerersFile));
        heroes.addAll(warriorLoader.load(warriorsFile));
        return Collections.unmodifiableList(heroes);
    }
}
