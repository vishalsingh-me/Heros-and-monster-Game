import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PotionLoader implements DataLoader<Potion> {

    @Override
    public List<Potion> load(Path path) throws IOException {
        List<Potion> potions = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }
                // Expected columns: name price requiredLevel effectAmount affectedStat(s...)
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 5) {
                    continue;
                }
                String name = parts[0];
                int price = Integer.parseInt(parts[1]);
                int requiredLevel = Integer.parseInt(parts[2]);
                int effectAmount = Integer.parseInt(parts[3]);
                Set<String> stats = Stream.of(parts).skip(4).collect(Collectors.toUnmodifiableSet());
                potions.add(new Potion(name, price, requiredLevel, effectAmount, stats));
            }
        }
        return potions;
    }
}
