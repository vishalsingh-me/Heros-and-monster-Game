import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FireSpellLoader implements DataLoader<FireSpell> {

    @Override
    public List<FireSpell> load(Path path) throws IOException {
        List<FireSpell> spells = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }
                // Expected columns: name price requiredLevel damage manaCost
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 5 || parts[0].contains("/")) {
                    continue; // skip header or malformed
                }
                String name = parts[0];
                int price = Integer.parseInt(parts[1]);
                int requiredLevel = Integer.parseInt(parts[2]);
                int damage = Integer.parseInt(parts[3]);
                int manaCost = Integer.parseInt(parts[4]);
                double debuffAmount = damage * 0.1; // simple proportion as debuff magnitude
                spells.add(new FireSpell(name, price, requiredLevel, manaCost, damage, debuffAmount));
            }
        }
        return spells;
    }
}
