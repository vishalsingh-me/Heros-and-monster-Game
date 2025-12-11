import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameLauncher {
    private final HeroFactory heroFactory = new HeroFactory();
    private final MarketFactory marketFactory = new MarketFactory();
    private List<Hero> heroTemplates;
    private Market market;

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Loading data...");
        try {
            loadData();
            showIntroScreen(scanner);
            showMainMenu(scanner);
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

    private void showMainMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n=== GAME HUB ===");
            System.out.println("1. Legends: Monsters and Heroes");
            System.out.println("2. Legends of Valor");
            System.out.println("3. Quit");
            System.out.print("Select game: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                LegendsGame.main(new String[] {});
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

    private void showIntroScreen(Scanner scanner) {
        System.out.println();
        System.out.println("+======================================+");
        System.out.println("|    LEGENDS: MONSTERS & VALOR HUB     |");
        System.out.println("+======================================+");
        System.out.println("|  Two adventures await: the classic   |");
        System.out.println("|  Monsters & Heroes, and the new MOBA |");
        System.out.println("|  inspired Legends of Valor.          |");
        System.out.println("+======================================+");
        System.out.println();
        System.out.println("Exploration: Navigate the grid, visit markets, and push through lanes to the enemy nexus.");
        System.out.println("Combat: Choose attacks or spells, respect ranges, and manage mana to drop monsters.");
        System.out.println("Potions & Spells: Potions boost stats mid-fight; spells add elemental damage and debuffs.");
        System.out.println("Level-up: Win battles to earn gold/exp, gear up, and watch heroes grow stronger.");
        System.out.println();
        System.out.println("Controls:");
        System.out.println("  W/A/S/D - Move");
        System.out.println("  F - Basic attack (Valor)");
        System.out.println("  C - Cast spell");
        System.out.println("  T - Teleport (Valor)");
        System.out.println("  R - Recall to nexus (Valor)");
        System.out.println("  P - Use potion");
        System.out.println("  E - Equip gear");
        System.out.println("  I - Hero info / inventory");
        System.out.println("  M - Market (when on a Nexus / Market tile)");
        System.out.println("  Q - Quit / back to menu");
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
        System.out.println();
    }

    private void startValorGame(Scanner scanner) {
        System.out.println("\n--- Legends of Valor: Hero Selection ---");
        System.out.println("You must choose exactly 3 heroes.");
        List<Hero> party = new ArrayList<Hero>();

        for (int i = 0; i < heroTemplates.size(); i++) {
            Hero h = heroTemplates.get(i);
            System.out.printf("%2d) %-15s Lvl %d %-10s STR:%-3d DEX:%-3d AGI:%-3d%n",
                i, h.getName(), h.getLevel(), h.getClass().getSimpleName(),
                h.getStrength(), h.getDexterity(), h.getAgility());
        }

        while (party.size() < 3) {
            System.out.print("Enter 3 hero indices separated by spaces (or Q to cancel): ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("q")) {
                System.out.println("Returning to main menu.");
                return;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Please provide exactly 3 indices.");
                continue;
            }

            boolean valid = true;
            List<Integer> chosenIdx = new ArrayList<Integer>();
            for (String p : parts) {
                try {
                    int idx = Integer.parseInt(p);
                    if (idx < 0 || idx >= heroTemplates.size()) {
                        valid = false;
                        break;
                    }
                    if (chosenIdx.contains(idx)) {
                        System.out.println("Duplicate indices are not allowed.");
                        valid = false;
                        break;
                    }
                    chosenIdx.add(idx);
                } catch (NumberFormatException e) {
                    valid = false;
                    break;
                }
            }

            if (!valid) {
                System.out.println("Invalid selection. Try again.");
                continue;
            }

            for (int idx : chosenIdx) {
                Hero template = heroTemplates.get(idx);
                party.add(cloneHero(template));
                System.out.println(template.getName() + " added.");
            }
        }

        ValorGame game = new ValorGame(party, market, scanner);
        game.start();
    }

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
