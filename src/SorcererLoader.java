import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SorcererLoader implements DataLoader<Sorcerer> {

    @Override
    public List<Sorcerer> load(Path path) throws IOException {
        List<Sorcerer> sorcerers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//") || line.contains("/")) {
                    continue;
                }
                // Expected: name mana strength agility dexterity startingMoney startingExperience
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 7) {
                    continue;
                }
                String name = parts[0];
                int mana = Integer.parseInt(parts[1]);
                int strength = Integer.parseInt(parts[2]);
                int agility = Integer.parseInt(parts[3]);
                int dexterity = Integer.parseInt(parts[4]);
                int money = Integer.parseInt(parts[5]);
                int experience = Integer.parseInt(parts[6]);
                int level = 1;
                int maxHealth = level * 100;
                sorcerers.add(new Sorcerer(name, level, maxHealth, mana, strength, dexterity, agility, money, experience));
            }
        }
        return sorcerers;
    }
}
