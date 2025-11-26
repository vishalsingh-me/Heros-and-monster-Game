import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WeaponLoader implements DataLoader<Weapon> {

    @Override
    public List<Weapon> load(Path path) throws IOException {
        List<Weapon> weapons = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue;
                }
                // Expected columns: name price requiredLevel damage hands
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 5) {
                    continue;
                }
                String name = parts[0];
                int price = Integer.parseInt(parts[1]);
                int requiredLevel = Integer.parseInt(parts[2]);
                int damage = Integer.parseInt(parts[3]);
                int hands = Integer.parseInt(parts[4]);
                weapons.add(new Weapon(name, price, requiredLevel, damage, hands));
            }
        }
        return weapons;
    }
}
