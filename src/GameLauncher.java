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
            System.out.println(GameText.MENU_OPTION_1);
            System.out.println(GameText.MENU_OPTION_2);
            System.out.println(GameText.MENU_OPTION_3);
            System.out.print(GameText.MENU_PROMPT);

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
                    System.out.println(GameText.MENU_EXIT_MESSAGE);
                    break;
                default:
                    System.out.println(GameText.MENU_INVALID_CHOICE);
            }
        }
    }

    private void printHubHeader() {
        System.out.println();
        System.out.println(GameText.CYAN + GameText.HUB_BORDER + GameText.RESET);
        System.out.println(GameText.CYAN + "              " + GameText.HUB_TITLE + "                " + GameText.RESET);
        System.out.println(GameText.CYAN + GameText.HUB_BORDER + GameText.RESET);
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
                System.out.println(GameText.RETURNING_TO_HUB);
            }

        } catch (IOException e) {
            System.out.println(GameText.ERROR_LOADING_LOV + e.getMessage());
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
            System.out.println(GameText.YELLOW + GameText.HERO_SELECT_PROMPT_PREFIX + (chosen.size() + 1) + GameText.HERO_SELECT_OF + GameText.RESET);
            for (int i = 0; i < allHeroes.size(); i++) {
                Hero h = allHeroes.get(i);
                System.out.printf("%2d) %-18s (Lvl %d)%n", i, h.getName(), h.getLevel());
            }
            System.out.print(GameText.HERO_INDEX_PROMPT);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                chosen.clear();
                break;
            }
            try {
                int idx = Integer.parseInt(input);
                if (idx < 0 || idx >= allHeroes.size()) {
                    System.out.println(GameText.HERO_INVALID_INDEX);
                    continue;
                }
                Hero candidate = allHeroes.get(idx);
                if (chosen.contains(candidate)) {
                    System.out.println(GameText.HERO_ALREADY_IN_PARTY);
                    continue;
                }
                chosen.add(candidate);
                System.out.println(GameText.GREEN + candidate.getName() + GameText.HERO_ADDED_SUFFIX + GameText.RESET);
            } catch (NumberFormatException e) {
                System.out.println(GameText.HERO_INPUT_NUMBER);
            }
        }
        return chosen;
    }
}
