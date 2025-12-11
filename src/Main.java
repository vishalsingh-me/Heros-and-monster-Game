import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final HeroFactory heroFactory = new HeroFactory();
    private final MarketFactory marketFactory = new MarketFactory();
    private List<Hero> heroTemplates;
    private Market market;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        System.out.println("Loading data...");
        try {
            loadData();
            showMainMenu();
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private void loadData() throws IOException {
        heroTemplates = heroFactory.loadAll(
            Paths.get("Data/Paladins.txt"),
            Paths.get("Data/Sorcerers.txt"),
            Paths.get("Data/Warriors.txt")
        );
        MarketFactory.Stock stock = marketFactory.loadAll(
            Paths.get("Data/Weaponry.txt"),
            Paths.get("Data/Armory.txt"),
            Paths.get("Data/Potions.txt"),
            Paths.get("Data/FireSpells.txt"),
            Paths.get("Data/IceSpells.txt"),
            Paths.get("Data/LightningSpells.txt")
        );
        market = new Market(stock.getWeapons(), stock.getArmors(), stock.getPotions(), stock.getSpells());
    }

    private void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== GAME HUB ===");
            System.out.println("1. Legends: Monsters and Heroes");
            System.out.println("2. Legends of Valor");
            System.out.println("3. Quit");
            System.out.print("Select game: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                // Launch original game
                LegendsGame.main(new String[]{}); 
            } else if (choice.equals("2")) {
                startValorGame(scanner);
            } else if (choice.equals("3")) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("Invalid selection.");
            }
        }
    }

    private void startValorGame(Scanner scanner) {
        // Hero Selection specific to LoV
        System.out.println("\n--- Legends of Valor: Hero Selection ---");
        System.out.println("You must choose exactly 3 heroes.");
        List<Hero> party = new ArrayList<Hero>();
        
        while (party.size() < 3) {
            System.out.println("Choose hero " + (party.size() + 1) + ":");
            for (int i = 0; i < heroTemplates.size(); i++) {
                Hero h = heroTemplates.get(i);
                System.out.printf("%d) %s (Lvl %d) %s%n", i, h.getName(), h.getLevel(), h.getClass().getSimpleName());
            }
            System.out.print("Index: ");
            try {
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < heroTemplates.size()) {
                    Hero template = heroTemplates.get(idx);
                    party.add(cloneHero(template));
                    System.out.println(template.getName() + " added.");
                } else {
                    System.out.println("Invalid index.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }

        ValorGame game = new ValorGame(party, market, scanner);
        game.start();
    }

    // Helper to clone hero from template so we don't modify the "factory" copy
    private Hero cloneHero(Hero template) {
        if (template instanceof Warrior) {
            Warrior w = (Warrior) template;
            return new Warrior(
                w.getName(),
                w.getLevel(),
                w.getMaxHealth(),
                w.getMaxMana(),
                w.getStrength(),
                w.getDexterity(),
                w.getAgility(),
                w.getGold(),
                w.getExperience()
            );
        }
        if (template instanceof Sorcerer) {
            Sorcerer s = (Sorcerer) template;
            return new Sorcerer(
                s.getName(),
                s.getLevel(),
                s.getMaxHealth(),
                s.getMaxMana(),
                s.getStrength(),
                s.getDexterity(),
                s.getAgility(),
                s.getGold(),
                s.getExperience()
            );
        }
        if (template instanceof Paladin) {
            Paladin p = (Paladin) template;
            return new Paladin(
                p.getName(),
                p.getLevel(),
                p.getMaxHealth(),
                p.getMaxMana(),
                p.getStrength(),
                p.getDexterity(),
                p.getAgility(),
                p.getGold(),
                p.getExperience()
            );
        }
        throw new IllegalArgumentException("Unknown hero type");
    }
}
