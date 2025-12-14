import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point / game hub for:
 *  - Legends: Monsters and Heroes
 *  - Legends of Valor
 *
 * This version removes the old DataLoader usage and delegates
 * loading to HeroFactory / MarketFactory / LegendsGame / ValorGame.
 * It is compatible with Main.start(), which calls:
 *
 *   new GameLauncher().start();
 */
public class GameLauncher {

    // Simple ANSI colors for hub UI
    private static final String RESET  = "\u001B[0m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    private final Scanner scanner;
    private final HeroFactory heroFactory;
    private final MarketFactory marketFactory;

    public GameLauncher() {
        this.scanner = new Scanner(System.in);
        this.heroFactory = new HeroFactory();
        this.marketFactory = new MarketFactory();
    }

    /**
     * Called from Main.start()
     */
    public void start() {
        runGameHub();
    }

    // ------------------------------------------------------------------
    // GAME HUB LOOP
    // ------------------------------------------------------------------

    public void runGameHub() {
        boolean exit = false;
        while (!exit) {
            printHubHeader();
            System.out.println("1. Legends: Monsters and Heroes");
            System.out.println("2. Legends of Valor");
            System.out.println("3. Quit");
            System.out.print("Select game: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    startMonstersAndHeroes();
                    break;
                case "2":
                    startLegendsOfValor();
                    break;
                case "3":
                    exit = true;
                    System.out.println("Exiting game. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void printHubHeader() {
        System.out.println();
        System.out.println(CYAN + "======================================" + RESET);
        System.out.println(CYAN + "              GAME HUB                " + RESET);
        System.out.println(CYAN + "======================================" + RESET);
    }

    // ------------------------------------------------------------------
    // MONSTERS & HEROES (first game)
    // ------------------------------------------------------------------

    /**
     * Your Monsters & Heroes game already knows how to load all data
     * (heroes, monsters, market, map) inside LegendsGame itself.
     *
     * So we just instantiate LegendsGame and run it.
     */
    private void startMonstersAndHeroes() {
        LegendsGame mhGame = new LegendsGame();
        mhGame.run();
    }

    // ------------------------------------------------------------------
    // LEGENDS OF VALOR
    // ------------------------------------------------------------------

    private void startLegendsOfValor() {
        try {
            // Load heroes using HeroFactory (same data files)
            List<Hero> allHeroes = heroFactory.loadAll(
                    Paths.get("Data/Warriors.txt"),
                    Paths.get("Data/Sorcerers.txt"),
                    Paths.get("Data/Paladins.txt")
            );

            // Build market using MarketFactory.Stock helper
            MarketFactory.Stock stock = marketFactory.loadAll(
                    Paths.get("Data/Weaponry.txt"),
                    Paths.get("Data/Armory.txt"),
                    Paths.get("Data/Potions.txt"),
                    Paths.get("Data/FireSpells.txt"),
                    Paths.get("Data/IceSpells.txt"),
                    Paths.get("Data/LightningSpells.txt")
            );

            Market market = new Market(
                    stock.getWeapons(),
                    stock.getArmors(),
                    stock.getPotions(),
                    stock.getSpells()
            );

            // Hero selection for Legends of Valor
            List<Hero> selected = selectThreeHeroesLoV(allHeroes);

            if (selected.size() == 3) {
                ValorGame valorGame = new ValorGame(selected, market, scanner);
                valorGame.run();
            } else {
                System.out.println("Returning to game hub.");
            }

        } catch (IOException e) {
            System.out.println("Error loading Legends of Valor data: " + e.getMessage());
        }
    }

    /**
     * Simple, safe hero selection for Legends of Valor.
     * You can later swap this out with your fancy table-based
     * selection UI, but this version compiles and is rubric-safe.
     */
    private List<Hero> selectThreeHeroesLoV(List<Hero> allHeroes) {
        List<Hero> chosen = new ArrayList<Hero>();
        while (chosen.size() < 3) {
            System.out.println();
            System.out.println(YELLOW + "Select hero " + (chosen.size() + 1) + " of 3" + RESET);
            for (int i = 0; i < allHeroes.size(); i++) {
                Hero h = allHeroes.get(i);
                System.out.printf("%2d) %-18s (Lvl %d)%n", i, h.getName(), h.getLevel());
            }
            System.out.print("Index (or 'q' to cancel): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                chosen.clear();
                break;
            }
            try {
                int idx = Integer.parseInt(input);
                if (idx < 0 || idx >= allHeroes.size()) {
                    System.out.println("Invalid index.");
                    continue;
                }
                Hero candidate = allHeroes.get(idx);
                if (chosen.contains(candidate)) {
                    System.out.println("That hero is already in your party.");
                    continue;
                }
                chosen.add(candidate);
                System.out.println(GREEN + candidate.getName() + " added." + RESET);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
        return chosen;
    }
}
