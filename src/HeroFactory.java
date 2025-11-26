import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeroFactory {
    private final PaladinLoader paladinLoader = new PaladinLoader();
    private final SorcererLoader sorcererLoader = new SorcererLoader();
    private final WarriorLoader warriorLoader = new WarriorLoader();

    public List<Hero> loadAll(Path paladinsFile, Path sorcerersFile, Path warriorsFile) throws IOException {
        List<Hero> heroes = new ArrayList<>();
        heroes.addAll(paladinLoader.load(paladinsFile));
        heroes.addAll(sorcererLoader.load(sorcerersFile));
        heroes.addAll(warriorLoader.load(warriorsFile));
        return Collections.unmodifiableList(heroes);
    }
}
