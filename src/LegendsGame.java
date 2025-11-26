import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

public class LegendsGame {
    private final HeroFactory heroFactory = new HeroFactory();
    private final MonsterFactory monsterFactory = new MonsterFactory();
    private final MarketFactory marketFactory = new MarketFactory();
    private List<Hero> heroTemplates;
    private List<Monster> monsterPool;
    private Market market;
    private Party party;
    private GameMap map;
    private final Random random = new Random();

    public static void main(String[] args) {
        LegendsGame game = new LegendsGame();
        game.run();
    }

    public void run() {
        try {
            loadData();
            selectHeroes();
            setupMap();
            gameLoop();
        } catch (IOException e) {
            System.out.println("Failed to load game data: " + e.getMessage());
        }
    }

    private void loadData() throws IOException {
        heroTemplates = heroFactory.loadAll(
                Path.of("Data/Paladins.txt"),
                Path.of("Data/Sorcerers.txt"),
                Path.of("Data/Warriors.txt")
        );
        monsterPool = monsterFactory.loadAll(
                Path.of("Data/Dragons.txt"),
                Path.of("Data/Exoskeletons.txt"),
                Path.of("Data/Spirits.txt")
        );
        MarketFactory.Stock stock = marketFactory.loadAll(
                Path.of("Data/Weaponry.txt"),
                Path.of("Data/Armory.txt"),
                Path.of("Data/Potions.txt"),
                Path.of("Data/FireSpells.txt"),
                Path.of("Data/IceSpells.txt"),
                Path.of("Data/LightningSpells.txt")
        );
        market = new Market(stock.getWeapons(), stock.getArmors(), stock.getPotions(), stock.getSpells());
    }

    private void selectHeroes() {
        Scanner scanner = new Scanner(System.in);
        List<Hero> chosen = new ArrayList<>();
        System.out.println("Choose 1-3 heroes by index:");
        for (int i = 0; i < heroTemplates.size(); i++) {
            Hero h = heroTemplates.get(i);
            System.out.printf("%d) %s (Lvl %d) STR:%d DEX:%d AGI:%d%n", i, h.getName(), h.getLevel(),
                    h.getStrength(), h.getDexterity(), h.getAgility());
        }
        while (chosen.size() < 3) {
            System.out.print("Enter index (or blank to finish): ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                break;
            }
            try {
                int idx = Integer.parseInt(line);
                if (idx >= 0 && idx < heroTemplates.size()) {
                    Hero template = heroTemplates.get(idx);
                    chosen.add(cloneHero(template));
                    System.out.println(template.getName() + " added.");
                } else {
                    System.out.println("Invalid index.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
        if (chosen.isEmpty()) {
            chosen.add(cloneHero(heroTemplates.get(0)));
        }
        party = new Party(chosen);
    }

    private Hero cloneHero(Hero template) {
        if (template instanceof Warrior w) {
            return new Warrior(w.getName(), w.getLevel(), w.getMaxHealth(), w.getMaxMana(),
                    w.getStrength(), w.getDexterity(), w.getAgility(), w.getGold(), w.getExperience());
        }
        if (template instanceof Sorcerer s) {
            return new Sorcerer(s.getName(), s.getLevel(), s.getMaxHealth(), s.getMaxMana(),
                    s.getStrength(), s.getDexterity(), s.getAgility(), s.getGold(), s.getExperience());
        }
        if (template instanceof Paladin p) {
            return new Paladin(p.getName(), p.getLevel(), p.getMaxHealth(), p.getMaxMana(),
                    p.getStrength(), p.getDexterity(), p.getAgility(), p.getGold(), p.getExperience());
        }
        throw new IllegalArgumentException("Unknown hero type");
    }

    private void setupMap() {
        map = GameMap.generateDefault(8, market);
    }

    private void gameLoop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Game start! Use W/A/S/D to move, Q to quit, M to view map.");
        boolean running = true;
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "w" -> attemptMove(-1, 0);
                case "s" -> attemptMove(1, 0);
                case "a" -> attemptMove(0, -1);
                case "d" -> attemptMove(0, 1);
                case "m" -> map.render();
                case "q" -> running = false;
                default -> System.out.println("Commands: W/A/S/D move, M map, Q quit");
            }
        }
        System.out.println("Goodbye!");
    }

    private void attemptMove(int dRow, int dCol) {
        if (!map.move(dRow, dCol)) {
            System.out.println("Cannot move there.");
            return;
        }
        Tile tile = map.getCurrentTile();
        if (tile instanceof MarketTile marketTile) {
            enterMarket(marketTile.getMarket());
        } else if (tile instanceof CommonTile) {
            maybeBattle();
        } else {
            // inaccessible would have been blocked
        }
    }

    private void enterMarket(Market market) {
        Scanner scanner = new Scanner(System.in);
        boolean shopping = true;
        System.out.println("Entered Market. Commands: list, buy, sell, exit");
        while (shopping) {
            System.out.print("Market> ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            switch (cmd) {
                case "list" -> listMarket(market);
                case "buy" -> doBuy(market, scanner);
                case "sell" -> doSell(market, scanner);
                case "exit" -> shopping = false;
                default -> System.out.println("Commands: list, buy, sell, exit");
            }
        }
    }

    private void listMarket(Market market) {
        System.out.println("Weapons:");
        for (int i = 0; i < market.getWeapons().size(); i++) {
            Weapon w = market.getWeapons().get(i);
            System.out.printf("%d) %s lvl%d dmg:%d hands:%d price:%d%n", i, w.getName(), w.getRequiredLevel(),
                    w.getDamage(), w.getHandsRequired(), w.getPrice());
        }
        System.out.println("Armors:");
        for (int i = 0; i < market.getArmors().size(); i++) {
            Armor a = market.getArmors().get(i);
            System.out.printf("%d) %s lvl%d red:%d price:%d%n", i, a.getName(), a.getRequiredLevel(),
                    a.getDamageReduction(), a.getPrice());
        }
        System.out.println("Potions:");
        for (int i = 0; i < market.getPotions().size(); i++) {
            Potion p = market.getPotions().get(i);
            System.out.printf("%d) %s lvl%d effect:%d stats:%s price:%d%n", i, p.getName(), p.getRequiredLevel(),
                    p.getEffectAmount(), p.getAffectedStats(), p.getPrice());
        }
        System.out.println("Spells:");
        for (int i = 0; i < market.getSpells().size(); i++) {
            Spell s = market.getSpells().get(i);
            System.out.printf("%d) %s lvl%d dmg:%d mana:%d price:%d type:%s%n", i, s.getName(),
                    s.getRequiredLevel(), s.getBaseDamage(), s.getManaCost(), s.getPrice(), s.getDebuffType());
        }
    }

    private void doBuy(Market market, Scanner scanner) {
        Hero hero = chooseHero(scanner);
        if (hero == null) {
            return;
        }
        System.out.println("Buy which category? weapon/armor/potion/spell");
        String cat = scanner.nextLine().trim().toLowerCase();
        switch (cat) {
            case "weapon" -> buyItem(market.getWeapons(), hero, market, scanner);
            case "armor" -> buyItem(market.getArmors(), hero, market, scanner);
            case "potion" -> buyItem(market.getPotions(), hero, market, scanner);
            case "spell" -> buyItem(market.getSpells(), hero, market, scanner);
            default -> System.out.println("Unknown category.");
        }
    }

    private <T extends Item> void buyItem(List<T> items, Hero hero, Market market, Scanner scanner) {
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            System.out.printf("%d) %s lvl%d price:%d%n", i, it.getName(), it.getRequiredLevel(), it.getPrice());
        }
        System.out.print("Index to buy: ");
        String line = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < items.size()) {
                Item item = items.get(idx);
                if (market.buy(hero, item)) {
                    System.out.println("Purchased " + item.getName());
                } else {
                    System.out.println("Cannot buy (level/gold).");
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private void doSell(Market market, Scanner scanner) {
        Hero hero = chooseHero(scanner);
        if (hero == null) {
            return;
        }
        List<Item> items = hero.getInventory().getAll();
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            System.out.printf("%d) %s price:%d%n", i, it.getName(), it.getPrice());
        }
        System.out.print("Index to sell: ");
        String line = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < items.size()) {
                Item item = items.get(idx);
                if (market.sell(hero, item)) {
                    System.out.println("Sold " + item.getName());
                } else {
                    System.out.println("Cannot sell.");
                }
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private Hero chooseHero(Scanner scanner) {
        List<Hero> heroes = party.getHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            System.out.printf("%d) %s HP:%d/%d Mana:%d/%d Gold:%d%n", i, h.getName(), h.getHealth(), h.getMaxHealth(),
                    h.getMana(), h.getMaxMana(), h.getGold());
        }
        System.out.print("Choose hero index: ");
        String line = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < heroes.size()) {
                return heroes.get(idx);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private void maybeBattle() {
        if (random.nextDouble() > 0.3) {
            return;
        }
        System.out.println("A battle begins!");
        int highestLevel = party.getHeroes().stream().mapToInt(Hero::getLevel).max().orElse(1);
        List<Monster> foes = monsterFactory.spawnForLevel(monsterPool, highestLevel, party.getHeroes().size());
        Battle battle = new Battle(party, foes);
        battleLoop(battle);
        if (party.isDefeated()) {
            System.out.println("Party defeated. Game over.");
            System.exit(0);
        } else {
            party.reviveAfterWin();
            int rewardGold = 100 * foes.size();
            int rewardExp = 50 * foes.size();
            for (Hero h : party.getHeroes()) {
                if (!h.isFainted()) {
                    h.addGold(rewardGold);
                    h.addExperience(rewardExp);
                }
            }
            System.out.println("Victory! Earned gold and experience.");
        }
    }

    private void battleLoop(Battle battle) {
        Scanner scanner = new Scanner(System.in);
        while (!battle.isOver()) {
            for (Hero hero : party.aliveHeroes()) {
                System.out.printf("Hero %s turn. Actions: attack, spell, skip%n", hero.getName());
                String action = scanner.nextLine().trim().toLowerCase();
                switch (action) {
                    case "attack" -> {
                        Optional<Monster> target = battleTarget(battle);
                        target.ifPresent(t -> battle.heroAttack(hero, t));
                    }
                    case "spell" -> {
                        Spell spell = chooseSpell(hero, scanner);
                        Optional<Monster> target = battleTarget(battle);
                        if (spell != null && target.isPresent()) {
                            battle.castSpell(hero, spell, target.get());
                        }
                    }
                    default -> System.out.println("Turn skipped.");
                }
            }
            for (Monster m : new ArrayList<>(battle.getMonsters())) {
                if (m.isFainted()) continue;
                Hero target = randomAliveHero();
                if (target != null) {
                    battle.monsterAttack(m, target);
                }
            }
        }
    }

    private Optional<Monster> battleTarget(Battle battle) {
        List<Monster> alive = battle.getMonsters().stream().filter(m -> !m.isFainted()).toList();
        if (alive.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(alive.get(0));
    }

    private Spell chooseSpell(Hero hero, Scanner scanner) {
        List<Spell> spells = hero.getInventory().getByType(Spell.class).stream().map(s -> (Spell) s).toList();
        for (int i = 0; i < spells.size(); i++) {
            Spell s = spells.get(i);
            System.out.printf("%d) %s dmg:%d mana:%d type:%s%n", i, s.getName(), s.getBaseDamage(), s.getManaCost(),
                    s.getDebuffType());
        }
        if (spells.isEmpty()) {
            System.out.println("No spells.");
            return null;
        }
        System.out.print("Choose spell index: ");
        String line = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < spells.size()) {
                return spells.get(idx);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private Hero randomAliveHero() {
        List<Hero> alive = party.aliveHeroes();
        if (alive.isEmpty()) {
            return null;
        }
        return alive.get(random.nextInt(alive.size()));
    }
}
