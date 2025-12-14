import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
public class LegendsGame extends RpgGame {

        public LegendsGame() {
        this(new Scanner(System.in));
    }

    public LegendsGame(Scanner scanner) {
        super(scanner);
    }


    public enum GameState { EXPLORING, MAP, MARKET, INVENTORY, BATTLE }

    private final HeroFactory heroFactory = new HeroFactory();
    private final MonsterFactory monsterFactory = new MonsterFactory();
    private final MarketFactory marketFactory = new MarketFactory();
    private List<Hero> heroTemplates;
    private List<Monster> monsterPool;
    private Market market;
    private Party party;
    private GameMap map;
    private final Random random = new Random();
    private GameState state = GameState.EXPLORING;

    public static void main(String[] args) {
        LegendsGame game = new LegendsGame();
        game.run();
    }

    public void run() {
        try {
            loadData();
            printIntro();
            selectHeroes();
            setupMap();
            gameLoop();
        } catch (IOException e) {
            System.out.println("Failed to load game data: " + e.getMessage());
        }
    }

    private void printIntro() {
        System.out.println(GameText.LEGENDS_BORDER);
        System.out.println(GameText.LEGENDS_TITLE);
        System.out.println(GameText.LEGENDS_BORDER);
        System.out.println(GameText.LEGENDS_WELCOME);
        System.out.println();
        System.out.println(GameText.LEGENDS_HOW_TO_PLAY);
        System.out.println(GameText.LEGENDS_MOVE);
        System.out.println(GameText.LEGENDS_MAP);
        System.out.println(GameText.LEGENDS_INVENTORY);
        System.out.println(GameText.LEGENDS_MARKET);
        System.out.println(GameText.LEGENDS_BATTLES);
        System.out.println(GameText.LEGENDS_QUIT);
        System.out.println();
        System.out.println(GameText.LEGENDS_PRESS_ENTER);
        new java.util.Scanner(System.in).nextLine();
    }

    private void loadData() throws IOException {
        heroTemplates = heroFactory.loadAll(
                Paths.get("Data/Paladins.txt"),
                Paths.get("Data/Sorcerers.txt"),
                Paths.get("Data/Warriors.txt")
        );
        monsterPool = monsterFactory.loadAll(
                Paths.get("Data/Dragons.txt"),
                Paths.get("Data/Exoskeletons.txt"),
                Paths.get("Data/Spirits.txt")
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

    private void selectHeroes() {
        Scanner scanner = new Scanner(System.in);
        List<Hero> chosen = new ArrayList<>();
        System.out.println(GameText.LEGENDS_CHOOSE_HEROES);
        for (int i = 0; i < heroTemplates.size(); i++) {
            Hero h = heroTemplates.get(i);
            System.out.printf("%d) %s (Lvl %d) STR:%d DEX:%d AGI:%d%n", i, h.getName(), h.getLevel(),
                    h.getStrength(), h.getDexterity(), h.getAgility());
        }
        while (chosen.size() < 3) {
            System.out.print(GameText.LEGENDS_ENTER_INDEX);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                break;
            }
            try {
                int idx = Integer.parseInt(line);
                if (idx >= 0 && idx < heroTemplates.size()) {
                    Hero template = heroTemplates.get(idx);
                    chosen.add(cloneHero(template));
                    System.out.println(template.getName() + GameText.HERO_ADDED_SUFFIX);
                } else {
                    System.out.println(GameText.LEGENDS_INVALID_INDEX);
                }
            } catch (NumberFormatException e) {
                System.out.println(GameText.LEGENDS_ENTER_NUMBER);
            }
        }
        if (chosen.isEmpty()) {
            chosen.add(cloneHero(heroTemplates.get(0)));
        }
        party = new Party(chosen);
    }

    private Hero cloneHero(Hero template) {
        if (template instanceof Warrior) {
            Warrior w = (Warrior) template;
            return new Warrior(w.getName(), w.getLevel(), w.getMaxHealth(), w.getMaxMana(),
                    w.getStrength(), w.getDexterity(), w.getAgility(), w.getGold(), w.getExperience());
        }
        if (template instanceof Sorcerer) {
            Sorcerer s = (Sorcerer) template;
            return new Sorcerer(s.getName(), s.getLevel(), s.getMaxHealth(), s.getMaxMana(),
                    s.getStrength(), s.getDexterity(), s.getAgility(), s.getGold(), s.getExperience());
        }
        if (template instanceof Paladin) {
            Paladin p = (Paladin) template;
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
        System.out.println(GameText.LEGENDS_GAME_START);
        System.out.println(GameText.LEGENDS_MOVE_AGAIN);
        map.render();
        boolean running = true;
        while (running) {
            System.out.print(GameText.LEGENDS_PROMPT);
            String input = scanner.nextLine().trim().toLowerCase();
            switch (state) {
                case EXPLORING:
                    switch (input) {
                        case "w":
                            attemptMove(-1, 0);
                            break;
                        case "s":
                            attemptMove(1, 0);
                            break;
                        case "a":
                            attemptMove(0, -1);
                            break;
                        case "d":
                            attemptMove(0, 1);
                            break;
                        case "m":
                            state = GameState.MAP;
                            map.render();
                            break;
                        case "i":
                            state = GameState.INVENTORY;
                            showInventory();
                            System.out.println(GameText.LEGENDS_INVENTORY_OPENED);
                            break;
                        case "q":
                            System.out.print("Are you sure? (y/n): ");
                            String ans = scanner.nextLine().trim().toLowerCase();
                            if (ans.equals("y")) {
                                running = false;
                            }
                            break;
                        default:
                            System.out.println(GameText.LEGENDS_COMMANDS);
                            break;
                    }
                    break;
                case MAP:
                    if (input.equals("b") || input.equals("q")) {
                        state = GameState.EXPLORING;
                    } else {
                        System.out.println(GameText.LEGENDS_MAP_VIEW);
                    }
                    break;
                case MARKET:
                    // market loop handles input; fallback here
                    if (input.equals("b")) {
                        state = GameState.EXPLORING;
                    }
                    break;
                case INVENTORY:
                    if (input.equals("b")) {
                        state = GameState.EXPLORING;
                    } else if (input.equals("q")) {
                        state = GameState.EXPLORING;
                    } else {
                        showInventory();
                        System.out.println(GameText.LEGENDS_MAP_VIEW);
                    }
                    break;
                case BATTLE:
                    // battle managed inside battleLoop
                    break;
            }
        }
        System.out.println(GameText.LEGENDS_GOODBYE);
    }

    private void attemptMove(int dRow, int dCol) {
        if (!map.move(dRow, dCol)) {
            System.out.println(GameText.LEGENDS_CANNOT_MOVE);
            return;
        }
        map.render();
        System.out.printf(GameText.LEGENDS_MOVED_TO, map.getHeroRow(), map.getHeroCol());
        Tile tile = map.getCurrentTile();
        if (tile instanceof MarketTile) {
            MarketTile marketTile = (MarketTile) tile;
            System.out.println(GameText.LEGENDS_ENTERED_MARKET);
            state = GameState.MARKET;
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
        System.out.println(GameText.LEGENDS_MARKET_COMMANDS);
        while (shopping) {
            System.out.print(GameText.LEGENDS_MARKET_PROMPT);
            String cmd = scanner.nextLine().trim().toLowerCase();
            switch (cmd) {
                case "list":
                    listMarket(market);
                    break;
                case "buy":
                    doBuy(market, scanner);
                    break;
                case "sell":
                    doSell(market, scanner);
                    break;
                case "exit":
                case "b":
                    shopping = false;
                    state = GameState.EXPLORING;
                    break;
                default:
                    System.out.println(GameText.LEGENDS_MARKET_COMMANDS_SHORT);
                    break;
            }
        }
    }

    private void listMarket(Market market) {
        System.out.println(GameText.LEGENDS_MARKET_WEAPONS);
        for (int i = 0; i < market.getWeapons().size(); i++) {
            Weapon w = market.getWeapons().get(i);
            System.out.printf("%d) %s lvl%d dmg:%d hands:%d price:%d%n", i, w.getName(), w.getRequiredLevel(),
                    w.getDamage(), w.getHandsRequired(), w.getPrice());
        }
        System.out.println(GameText.LEGENDS_MARKET_ARMORS);
        for (int i = 0; i < market.getArmors().size(); i++) {
            Armor a = market.getArmors().get(i);
            System.out.printf("%d) %s lvl%d red:%d price:%d%n", i, a.getName(), a.getRequiredLevel(),
                    a.getDamageReduction(), a.getPrice());
        }
        System.out.println(GameText.LEGENDS_MARKET_POTIONS);
        for (int i = 0; i < market.getPotions().size(); i++) {
            Potion p = market.getPotions().get(i);
            System.out.printf("%d) %s lvl%d effect:%d stats:%s price:%d%n", i, p.getName(), p.getRequiredLevel(),
                    p.getEffectAmount(), p.getAffectedStats(), p.getPrice());
        }
        System.out.println(GameText.LEGENDS_MARKET_SPELLS);
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
        System.out.println(GameText.LEGENDS_MARKET_BUY_WHICH);
        String cat = scanner.nextLine().trim().toLowerCase();
        switch (cat) {
            case "weapon":
                buyItem(market.getWeapons(), hero, market, scanner);
                break;
            case "armor":
                buyItem(market.getArmors(), hero, market, scanner);
                break;
            case "potion":
                buyItem(market.getPotions(), hero, market, scanner);
                break;
            case "spell":
                buyItem(market.getSpells(), hero, market, scanner);
                break;
            default:
                System.out.println(GameText.LEGENDS_MARKET_UNKNOWN_CAT);
                break;
        }
    }

    private <T extends Item> void buyItem(List<T> items, Hero hero, Market market, Scanner scanner) {
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            System.out.printf("%d) %s lvl%d price:%d%n", i, it.getName(), it.getRequiredLevel(), it.getPrice());
        }
        System.out.print(GameText.LEGENDS_MARKET_INDEX_BUY);
        String line = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < items.size()) {
                Item item = items.get(idx);
                if (market.buy(hero, item)) {
                    System.out.println(String.format(GameText.LEGENDS_PURCHASED, item.getName()));
                } else {
                    System.out.println(GameText.LEGENDS_CANNOT_BUY);
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
        System.out.print(GameText.LEGENDS_MARKET_INDEX_SELL);
        String line = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(line);
            if (idx >= 0 && idx < items.size()) {
                Item item = items.get(idx);
                if (market.sell(hero, item)) {
                    System.out.println(String.format(GameText.LEGENDS_SOLD, item.getName()));
                } else {
                    System.out.println(GameText.LEGENDS_CANNOT_SELL);
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
        System.out.println(GameText.LEGENDS_BATTLE_BEGINS);
        int highestLevel = party.getHeroes().stream().mapToInt(Hero::getLevel).max().orElse(1);
        List<Monster> foes = monsterFactory.spawnForLevel(monsterPool, highestLevel, party.getHeroes().size());
        if (foes.isEmpty()) {
            System.out.println(GameText.LEGENDS_NO_MONSTERS);
            return;
        }
        Battle battle = new Battle(party, foes);
        state = GameState.BATTLE;
        battleLoop(battle);
        if (party.isDefeated()) {
            System.out.println(GameText.LEGENDS_PARTY_DEFEATED);
            System.exit(0);
        } else {
            party.reviveAfterWin();
            int rewardGold = 100 * foes.size();
            int rewardExp = 50 * foes.size();
            for (Hero h : party.getHeroes()) {
                if (!h.isFainted()) {
                    h.addGold(rewardGold);
                    h.addExperience(rewardExp);
                    if (h.levelUpIfReady()) {
                        System.out.println(h.getName() + " leveled up to " + h.getLevel());
                    }
                }
            }
            System.out.println(GameText.LEGENDS_VICTORY);
        }
        state = GameState.EXPLORING;
    }

    private void showInventory() {
        System.out.println(GameText.LEGENDS_INVENTORY_TITLE);
        List<Hero> heroes = party.getHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            System.out.printf("[%d] %s (HP:%d/%d Mana:%d/%d Gold:%d)%n", i, h.getName(), h.getHealth(),
                    h.getMaxHealth(), h.getMana(), h.getMaxMana(), h.getGold());
            System.out.println("  Weapons:");
            printItems(h.getInventory().getByType(Weapon.class));
            System.out.println("  Armors:");
            printItems(h.getInventory().getByType(Armor.class));
            System.out.println("  Potions:");
            printItems(h.getInventory().getByType(Potion.class));
            System.out.println("  Spells:");
            printItems(h.getInventory().getByType(Spell.class));
        }
    }

    private void printItems(List<? extends Item> items) {
        if (items.isEmpty()) {
            System.out.println(GameText.LEGENDS_NONE);
            return;
        }
        for (int j = 0; j < items.size(); j++) {
            Item item = items.get(j);
            System.out.printf("    %d) %s (lvl %d, price %d)%n", j, item.getName(), item.getRequiredLevel(),
                    item.getPrice());
        }
    }

    private void battleLoop(Battle battle) {
        Scanner scanner = new Scanner(System.in);
        while (!battle.isOver()) {
            for (Hero hero : party.aliveHeroes()) {
                int choice = promptBattleChoice(scanner, hero);
                switch (choice) {
                    case 1: {
                        Optional<Monster> target = battleTarget(battle, scanner);
                        target.ifPresent(t -> battle.heroAttack(hero, t));
                        break;
                    }
                    case 2: {
                        Spell spell = chooseSpell(hero, scanner);
                        Optional<Monster> target = battleTarget(battle, scanner);
                        if (spell != null && target.isPresent()) {
                            battle.castSpell(hero, spell, target.get());
                        }
                        break;
                    }
                    case 3:
                        usePotion(hero, scanner);
                        break;
                    case 4:
                        equip(hero, scanner);
                        break;
                    case 5:
                        System.out.println("Turn skipped.");
                        break;
                    default:
                        System.out.println("Turn skipped.");
                        break;
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

    private int promptBattleChoice(Scanner scanner, Hero hero) {
        while (true) {
            System.out.printf("Hero %s turn:%n", hero.getName());
            System.out.println("1) Attack");
            System.out.println("2) Cast Spell");
            System.out.println("3) Use Potion");
            System.out.println("4) Equip");
            System.out.println("5) Skip Turn");
            System.out.print("Enter choice: ");
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= 1 && val <= 5) {
                    return val;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid choice. Please enter a number 1-5.");
        }
    }

    private Optional<Monster> battleTarget(Battle battle, Scanner scanner) {
        List<Monster> alive = battle.getMonsters().stream().filter(m -> !m.isFainted())
                .collect(Collectors.toList());
        if (alive.isEmpty()) {
            return Optional.empty();
        }
        while (true) {
                System.out.println(GameText.LEGENDS_CHOOSE_TARGET);
            for (int i = 0; i < alive.size(); i++) {
                Monster m = alive.get(i);
                System.out.printf("%d) %s HP:%d/%d DEF:%d%n", i, m.getName(), m.getHealth(), m.getMaxHealth(), m.getDefense());
            }
            System.out.print("Enter index: ");
            String line = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(line);
                if (idx >= 0 && idx < alive.size()) {
                    return Optional.of(alive.get(idx));
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid target. Try again.");
        }
    }

    private Spell chooseSpell(Hero hero, Scanner scanner) {
        List<Spell> spells = hero.getInventory().getByType(Spell.class).stream().map(s -> (Spell) s)
                .collect(Collectors.toList());
        if (spells.isEmpty()) {
            System.out.println("No spells.");
            return null;
        }
        while (true) {
            System.out.println(GameText.LEGENDS_CHOOSE_SPELL);
            for (int i = 0; i < spells.size(); i++) {
                Spell s = spells.get(i);
                System.out.printf("%d) %s dmg:%d mana:%d type:%s%n", i, s.getName(), s.getBaseDamage(), s.getManaCost(),
                        s.getDebuffType());
            }
            System.out.print("Enter index: ");
            String line = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(line);
                if (idx >= 0 && idx < spells.size()) {
                    return spells.get(idx);
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid spell. Try again.");
        }
    }

    private void usePotion(Hero hero, Scanner scanner) {
        List<Potion> potions = hero.getInventory().getByType(Potion.class).stream().map(p -> (Potion) p)
                .collect(Collectors.toList());
        if (potions.isEmpty()) {
            System.out.println("No potions.");
            return;
        }
        while (true) {
            System.out.println("Choose potion:");
            for (int i = 0; i < potions.size(); i++) {
                Potion p = potions.get(i);
                System.out.printf("%d) %s +%d %s%n", i, p.getName(), p.getEffectAmount(), p.getAffectedStats());
            }
            System.out.print("Enter index: ");
            String line = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(line);
                if (idx >= 0 && idx < potions.size()) {
                    Potion p = potions.get(idx);
                    hero.applyPotionEffect(p.getEffectAmount(), p.getAffectedStats());
                    hero.getInventory().remove(p);
                    System.out.println("Used " + p.getName());
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Invalid potion. Try again.");
        }
    }

    private void equip(Hero hero, Scanner scanner) {
        List<Weapon> weapons = hero.getInventory().getByType(Weapon.class).stream().map(w -> (Weapon) w)
                .collect(Collectors.toList());
        List<Armor> armors = hero.getInventory().getByType(Armor.class).stream().map(a -> (Armor) a)
                .collect(Collectors.toList());
        System.out.println(GameText.LEGENDS_EQUIP_MENU);
        if (!weapons.isEmpty()) {
            System.out.println(GameText.LEGENDS_MARKET_WEAPONS);
            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                System.out.printf("%d) %s dmg:%d hands:%d%n", i, w.getName(), w.getDamage(), w.getHandsRequired());
            }
            System.out.print(GameText.LEGENDS_WEAPON_INDEX);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                try {
                    int idx = Integer.parseInt(line);
                    if (idx >= 0 && idx < weapons.size()) {
                        hero.getEquipment().equipWeapon(weapons.get(idx));
                        System.out.println("Equipped weapon: " + weapons.get(idx).getName());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (!armors.isEmpty()) {
            System.out.println(GameText.LEGENDS_MARKET_ARMORS);
            for (int i = 0; i < armors.size(); i++) {
                Armor a = armors.get(i);
                System.out.printf("%d) %s red:%d%n", i, a.getName(), a.getDamageReduction());
            }
            System.out.print(GameText.LEGENDS_ARMOR_INDEX);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                try {
                    int idx = Integer.parseInt(line);
                    if (idx >= 0 && idx < armors.size()) {
                        hero.getEquipment().equipArmor(armors.get(idx));
                        System.out.println("Equipped armor: " + armors.get(idx).getName());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (weapons.isEmpty() && armors.isEmpty()) {
            System.out.println("No equipment available.");
        }
    }

    private Hero randomAliveHero() {
        List<Hero> alive = party.aliveHeroes();
        if (alive.isEmpty()) {
            return null;
        }
        return alive.get(random.nextInt(alive.size()));
    }
}
