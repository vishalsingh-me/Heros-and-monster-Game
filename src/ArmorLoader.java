import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArmorLoader implements DataLoader<Armor> {

    @Override
    public List<Armor> load(Path path) throws IOException {
        List<Armor> armors = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }
                // Expected columns: name price requiredLevel damageReduction
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 4) {
                    continue;
                }
                String name = parts[0];
                int price = Integer.parseInt(parts[1]);
                int requiredLevel = Integer.parseInt(parts[2]);
                int reduction = Integer.parseInt(parts[3]);
                armors.add(new Armor(name, price, requiredLevel, reduction));
            }
        }
        return armors;
    }
}
