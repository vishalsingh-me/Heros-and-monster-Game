import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpiritLoader implements DataLoader<Spirit> {

    @Override
    public List<Spirit> load(Path path) throws IOException {
        List<Spirit> spirits = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//") || line.contains("/")) {
                    continue;
                }
                // Expected: name level damage defense dodgeChance
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 5) {
                    continue;
                }
                String name = parts[0];
                int level = Integer.parseInt(parts[1]);
                int damage = Integer.parseInt(parts[2]);
                int defense = Integer.parseInt(parts[3]);
                double dodge = Double.parseDouble(parts[4]);
                int maxHealth = level * 100;
                int minDamage = Math.max(1, damage / 2);
                int maxDamage = damage;
                spirits.add(new Spirit(name, level, maxHealth, minDamage, maxDamage, defense, dodge));
            }
        }
        return spirits;
    }
}
