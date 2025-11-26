import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IceSpellLoader implements DataLoader<IceSpell> {

    @Override
    public List<IceSpell> load(Path path) throws IOException {
        List<IceSpell> spells = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }
                // Expected columns: name price requiredLevel damage manaCost
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 5 || parts[0].contains("/")) {
                    continue;
                }
                String name = parts[0];
                int price = Integer.parseInt(parts[1]);
                int requiredLevel = Integer.parseInt(parts[2]);
                int damage = Integer.parseInt(parts[3]);
                int manaCost = Integer.parseInt(parts[4]);
                double debuffAmount = damage * 0.1;
                spells.add(new IceSpell(name, price, requiredLevel, manaCost, damage, debuffAmount));
            }
        }
        return spells;
    }
}
