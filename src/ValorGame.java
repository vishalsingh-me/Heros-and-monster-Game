import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * Legends of Valor game.
 *
 * NOTE: This class only has UI/print improvements added (colors, formatting,
 * help, event log, end-game summary). Game rules/mechanics are the same
 * as your previous version.
 */
public class ValorGame extends RpgGame {

    // ===== ANSI color codes for colorful terminal UI =====
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";

    private static final String BLACK   = "\u001B[30m";
    private static final String RED     = "\u001B[31m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String BLUE    = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN    = "\u001B[36m";
    private static final String WHITE   = "\u001B[37m";

    // bright variants
    private static final String BRIGHT_RED     = "\u001B[91m";
    private static final String BRIGHT_GREEN   = "\u001B[92m";
    private static final String BRIGHT_YELLOW  = "\u001B[93m";
    private static final String BRIGHT_BLUE    = "\u001B[94m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";
    private static final String BRIGHT_CYAN    = "\u001B[96m";
    private static final String BRIGHT_WHITE   = "\u001B[97m";

    private static final String BG_RED    = "\u001B[41m";
    private static final String BG_GREEN  = "\u001B[42m";
    private static final String BG_YELLOW = "\u001B[43m";
    private static final String BG_BLUE   = "\u001B[44m";

    // ===== game flags =====
    private boolean quitRequested = false;
    private boolean gameOver      = false;

    // ===== Tracking for end-of-game stats and event log =====
    private static final int EVENT_LOG_SIZE = 5;
    private final List<String> eventLog = new ArrayList<String>();
    private final Map<Hero, Integer> heroKills  = new HashMap<Hero, Integer>();
    private final Map<Hero, Integer> heroFaints = new HashMap<Hero, Integer>();
    private final Map<Hero, Integer> heroDamage = new HashMap<Hero, Integer>();

    private ValorMap map;
    private final List<Hero> heroes;
    private final List<Monster> monsters;
    private final Map<Hero, Integer> heroSpawnCols; // which column a hero spawns in
    private final MonsterFactory monsterFactory;
    private final Market market;
    private List<Monster> globalMonsterPool; // cached monsters

    private int round;
    private final Random random = new Random();
    private final Set<Hero> recallUsedThisRound = new HashSet<Hero>();

    private static final int MONSTER_SPAWN_RATE = 8; // spawn every 8 rounds

    private String endReason;   // used for end-game summary

    // ----------------------------------------------------
    // Constructor
    // ----------------------------------------------------
    public ValorGame(List<Hero> selectedHeroes, Market market, Scanner scanner) {
        super(scanner);

        if (selectedHeroes.size() != 3) {
            throw new IllegalArgumentException("Legends of Valor requires exactly 3 heroes.");
        }

        this.heroes        = new ArrayList<Hero>(selectedHeroes);
        this.monsters      = new ArrayList<Monster>();
        this.heroSpawnCols = new HashMap<Hero, Integer>();
        this.monsterFactory = new MonsterFactory();
        this.market        = market;
        this.round         = 0;
        this.gameOver      = false;
        this.endReason     = null;

        // Initialize Map
        this.map = new ValorMap();

        // Pre-load monsters to avoid disk I/O every spawn
        try {
            this.globalMonsterPool = monsterFactory.loadAll(
                    Paths.get("Data/Dragons.txt"),
                    Paths.get("Data/Exoskeletons.txt"),
                    Paths.get("Data/Spirits.txt")
            );
        } catch (IOException e) {
            System.out.println("Warning: Could not load monster data: " + e.getMessage());
            this.globalMonsterPool = new ArrayList<Monster>();
        }
    }

    // ----------------------------------------------------
    // Core loop
    // ----------------------------------------------------
    public void start() {
        System.out.println(BRIGHT_CYAN + BOLD + "Welcome to Legends of Valor!" + RESET);
        showIntro();
        initializeGame();

        while (!gameOver) {
            round++;
            recallUsedThisRound.clear();

            System.out.println();
            System.out.println(BRIGHT_CYAN + BOLD
                    + "========== ROUND " + round + " =========="
                    + RESET);

            // 1. Hero Turn
            processHeroTurn();
            if (checkWinCondition()) {
                break;
            }

            // 2. Monster Turn
            processMonsterTurn();
            if (checkWinCondition()) {
                break;
            }

            // 3. End of Round Updates (Respawn, Regen, Spawn)
            endOfRound();
        }

        System.out.println();
        if (quitRequested) {
            System.out.println("Exited to main menu.");
        } else {
            System.out.println(BRIGHT_CYAN + BOLD + "Game Over!" + RESET);
        }

        // show summary for rubric
        if (endReason == null) {
            endReason = quitRequested ? "You quit the battle." : "Battle finished.";
        }
        printEndGameSummary(endReason);
    }

    @Override
    public void run() {
        start();
    }

    // ----------------------------------------------------
    // Intro / setup
    // ----------------------------------------------------
    private void showIntro() {
        System.out.println();
        System.out.println("Three lanes connect the Hero Nexus (bottom) to the Monster Nexus (top).");
        System.out.println("Heroes push north, Monsters push south.");
        System.out.println("Reach the enemy Nexus to win. Don't let monsters reach yours!");
        System.out.println("Use W/A/S/D to move, F for physical attack, C to cast spells.");
        System.out.println("Teleport (T) only from your Nexus to an ally in another lane.");
        System.out.println("Recall (R) to return to your spawn Nexus tile.");
        System.out.println();
    }

    private void initializeGame() {
        // Assign heroes to lanes (Cols 0, 3, 6)
        int[] startCols = {0, 3, 6};

        for (int i = 0; i < 3; i++) {
            Hero h = heroes.get(i);
            int col = startCols[i];
            int row = 7; // Hero Nexus (bottom)

            heroSpawnCols.put(h, Integer.valueOf(col));

            ValorTile tile = map.getTile(row, col);
            tile.setHero(h);
        }

        spawnMonsters();
    }

    // ----------------------------------------------------
    // Monster spawn
    // ----------------------------------------------------
    private void spawnMonsters() {
        int maxHeroLevel = 1;
        for (Hero h : heroes) {
            if (h.getLevel() > maxHeroLevel) {
                maxHeroLevel = h.getLevel();
            }
        }

        int[] spawnCols = {1, 4, 7}; // Monster Nexus columns

        for (int i = 0; i < spawnCols.length; i++) {
            int col = spawnCols[i];
            ValorTile tile = map.getTile(0, col);
            if (!tile.hasMonster()) {
                List<Monster> candidates =
                        monsterFactory.spawnForLevel(globalMonsterPool, maxHeroLevel, 1);
                if (!candidates.isEmpty()) {
                    Monster m = candidates.get(0);
                    tile.setMonster(m);
                    monsters.add(m);
                    Lane lane = Lane.getLaneForCol(col);
                    System.out.println("A wild " + BRIGHT_RED + m.getName() + RESET
                            + " appeared in " + BOLD + lane + RESET + " lane!");
                    addEvent("A wild " + m.getName() + " appeared in " + lane + " lane.");
                }
            }
        }
    }

    // ----------------------------------------------------
    // HERO TURN LOGIC
    // ----------------------------------------------------
    private void processHeroTurn() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                continue;
            }

            boolean turnDone = false;
            while (!turnDone && !gameOver && !quitRequested) {
                // always show map
                map.printMap();

                printRoundHeroBanner(hero);
                printActionMenu();

                System.out.print("> ");
                String input = scanner.nextLine().trim().toUpperCase();

                if (input.length() == 0) {
                    System.out.println(RED + "Please enter a command. Type '?' for help." + RESET);
                    continue;
                }

                if ("Q".equals(input)) {
                    quitRequested = confirmQuit();
                    if (quitRequested) {
                        gameOver = true;
                        endReason = "You chose to quit the game.";
                        turnDone = true;
                    }
                    continue;
                }

                switch (input) {
                    case "W":
                        turnDone = attemptMove(hero, -1, 0);
                        break;
                    case "S":
                        turnDone = attemptMove(hero, 1, 0);
                        break;
                    case "A":
                        turnDone = attemptMove(hero, 0, -1);
                        break;
                    case "D":
                        turnDone = attemptMove(hero, 0, 1);
                        break;
                    case "F":
                        turnDone = attemptAttack(hero);
                        break;
                    case "C":
                        turnDone = attemptCastSpell(hero);
                        break;
                    case "P":
                        turnDone = usePotion(hero);
                        break;
                    case "E":
                        turnDone = equipItem(hero);
                        break;
                    case "T":
                        turnDone = attemptTeleport(hero);
                        break;
                    case "R":
                        turnDone = attemptRecall(hero);
                        break;
                    case "M": {
                        ValorTile heroTile = findHeroTile(hero);
                        if (heroTile != null &&
                                map.isNexus(heroTile.getRow(), heroTile.getCol())) {
                            visitMarket(hero);
                        } else {
                            System.out.println(YELLOW
                                    + "You must be on a Nexus tile to visit a market."
                                    + RESET);
                        }
                        break;
                    }
                    case "I":
                        printHeroSheet(hero);
                        break;
                    case "H":
                        printPartyOverview();
                        break;
                    case "V":
                        map.printMap();
                        break;
                    case "?":
                    case "HELP":
                        printHelp();
                        break;
                    default:
                        System.out.println(RED + "Invalid input. Type '?' for help." + RESET);
                        break;
                }
            }

            if (gameOver || quitRequested) {
                break;
            }
        }
    }

    private boolean confirmQuit() {
        System.out.print(RED + "Quit to game hub? (y/N): " + RESET);
        String line = scanner.nextLine().trim();
        return line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes");
    }

    private boolean attemptMove(Hero hero, int dRow, int dCol) {
        ValorTile currentTile = findHeroTile(hero);
        if (currentTile == null) {
            return false;
        }

        int newRow = currentTile.getRow() + dRow;
        int newCol = currentTile.getCol() + dCol;

        if (!map.isValidCoordinate(newRow, newCol)) {
            System.out.println(RED + "Cannot move out of bounds." + RESET);
            return false;
        }

        ValorTile targetTile = map.getTile(newRow, newCol);

        // obstacles
        if (targetTile.getType() == TileType.OBSTACLE) {
            System.out.print("Path blocked by Obstacle. Destroy it? (y/n): ");
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) {
                targetTile.setType(TileType.PLAIN);
                System.out.println(YELLOW + "Obstacle removed! (Turn consumed)" + RESET);
                addEvent(hero.getName() + " removed an obstacle at (" + newRow + "," + newCol + ")");
                return true;
            } else {
                return false;
            }
        }

        if (!targetTile.isAccessible()) {
            System.out.println(RED + "Path blocked." + RESET);
            return false;
        }

        if (targetTile.hasHero()) {
            System.out.println(RED + "Tile occupied by another hero." + RESET);
            return false;
        }

        if (isBlockedByMonster(currentTile, targetTile)) {
            System.out.println(RED + "Cannot move past a monster in this lane without killing it!"
                    + RESET);
            return false;
        }

        currentTile.setHero(null);
        targetTile.setHero(hero);
        System.out.println(hero.getName() + " moved to (" + newRow + ", " + newCol + ")");
        return true;
    }

    private boolean isBlockedByMonster(ValorTile current, ValorTile target) {
        Lane lane = Lane.getLaneForCol(current.getCol());
        if (lane == null) {
            return false;
        }

        int currentRow = current.getRow();
        int targetRow = target.getRow();

        for (Monster m : monsters) {
            ValorTile mTile = findMonsterTile(m);
            if (mTile == null) {
                continue;
            }

            Lane mLane = Lane.getLaneForCol(mTile.getCol());
            if (mLane != lane) {
                continue;
            }

            int mRow = mTile.getRow();

            if (mRow <= currentRow && targetRow < mRow) {
                return true;
            }
        }
        return false;
    }

    private boolean attemptAttack(Hero hero) {
        ValorTile currentTile = findHeroTile(hero);
        if (currentTile == null) {
            return false;
        }

        List<Monster> targets = getTargetsInRange(currentTile);
        if (targets.isEmpty()) {
            System.out.println("No monsters in range.");
            return false;
        }

        Monster target = targets.get(0);
        if (targets.size() > 1) {
            System.out.println("Choose target:");
            for (int i = 0; i < targets.size(); i++) {
                Monster m = targets.get(i);
                System.out.println(i + ") " + m.getName() + " (HP: " + m.getHealth() + ")");
            }
            try {
                System.out.print("> ");
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < targets.size()) {
                    target = targets.get(idx);
                }
            } catch (Exception ignored) {
            }
        }

        System.out.println(BRIGHT_GREEN + hero.getName() + RESET
                + " attacks "
                + BRIGHT_RED + target.getName() + RESET + "!");

        double terrainBonus = 1.0;
        if (currentTile.getType() == TileType.KOULOU) {
            terrainBonus = 1.1; // +10% STR
        }

        int weaponDmg = (hero.getEquipment().getWeapon() != null)
                ? hero.getEquipment().getWeapon().getDamage()
                : 0;
        int totalStr = (int) ((hero.getStrength() + weaponDmg) * terrainBonus);
        int damage = Math.max(0, totalStr - target.getDefense());

        target.takeDamage(damage);
        System.out.println("Dealt " + RED + damage + " damage" + RESET + ".");

        addHeroDamage(hero, damage);
        addEvent(hero.getName() + " dealt " + damage + " dmg to " + target.getName()
                + " (HP " + target.getHealth() + "/" + target.getMaxHealth() + ")");

        if (target.isFainted()) {
            handleMonsterDeath(hero, target);
        }
        return true;
    }

    private boolean attemptCastSpell(Hero hero) {
        List<Item> spellItems = hero.getInventory().getByType(Spell.class);
        if (spellItems.isEmpty()) {
            System.out.println("No spells in inventory.");
            return false;
        }

        System.out.println("Choose Spell:");
        for (int i = 0; i < spellItems.size(); i++) {
            Spell s = (Spell) spellItems.get(i);
            System.out.println(i + ") " + s.getName() + " (Mana: " + s.getManaCost()
                    + ", Dmg: " + s.getBaseDamage() + ")");
        }

        Spell spell;
        try {
            System.out.print("Choose spell index: ");
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx >= 0 && idx < spellItems.size()) {
                spell = (Spell) spellItems.get(idx);
            } else {
                System.out.println("Invalid selection.");
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return false;
        }

        if (hero.getMana() < spell.getManaCost()) {
            System.out.println("Not enough mana.");
            return false;
        }

        ValorTile heroTile = findHeroTile(hero);
        if (heroTile == null) {
            return false;
        }
        List<Monster> targets = getTargetsInRange(heroTile);
        if (targets.isEmpty()) {
            System.out.println("No targets in range.");
            return false;
        }

        Monster target = targets.get(0);
        if (targets.size() > 1) {
            System.out.println("Choose target:");
            for (int i = 0; i < targets.size(); i++) {
                Monster m = targets.get(i);
                System.out.println(i + ") " + m.getName()
                        + " (HP: " + m.getHealth() + ")");
            }
            try {
                System.out.print("> ");
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < targets.size()) {
                    target = targets.get(idx);
                }
            } catch (Exception ignored) {
            }
        }

        hero.spendMana(spell.getManaCost());

        int damage = spell.getBaseDamage() + (int) (hero.getDexterity() * 0.1);
        target.takeDamage(damage);

        System.out.println(BRIGHT_GREEN + hero.getName() + RESET
                + " casts " + MAGENTA + spell.getName() + RESET
                + " on " + BRIGHT_RED + target.getName() + RESET
                + " for " + RED + damage + " damage" + RESET + "!");

        addHeroDamage(hero, damage);
        addEvent(hero.getName() + " cast " + spell.getName()
                + " on " + target.getName()
                + " for " + damage + " dmg (HP " + target.getHealth()
                + "/" + target.getMaxHealth() + ")");

        // debuff
        double amount = spell.getDebuffAmount();
        String type = spell.getDebuffType().toLowerCase();

        if ("defense".equals(type)) {
            target.setDefense(Math.max(0, target.getDefense() - (int) amount));
            System.out.println(target.getName() + "'s defense reduced by " + (int) amount);
        } else if ("damage".equals(type)) {
            int newMin = Math.max(0, target.getMinDamage() - (int) amount);
            int newMax = Math.max(newMin, target.getMaxDamage() - (int) amount);
            target.setDamageRange(newMin, newMax);
            System.out.println(target.getName() + "'s damage reduced by " + (int) amount);
        } else if ("dodge".equals(type)) {
            target.setDodgeChance(Math.max(0, target.getDodgeChance() - amount));
            System.out.println(target.getName() + "'s dodge chance reduced by " + amount);
        }

        if (target.isFainted()) {
            handleMonsterDeath(hero, target);
        }

        return true;
    }

    private void handleMonsterDeath(Hero hero, Monster target) {
        System.out.println(BRIGHT_RED + target.getName() + " died!" + RESET);
        ValorTile mTile = findMonsterTile(target);
        if (mTile != null) {
            mTile.setMonster(null);
        }
        monsters.remove(target);

        hero.addGold(500 * target.getLevel());
        hero.addExperience(2 * target.getLevel());
        hero.levelUpIfReady();

        addHeroKill(hero);
        addEvent(hero.getName() + " killed " + target.getName() + "!");
    }

    private boolean attemptTeleport(Hero hero) {
        ValorTile heroTile = findHeroTile(hero);
        if (heroTile == null) {
            return false;
        }

        System.out.println("Choose target hero to teleport to:");
        List<Hero> validTargets = new ArrayList<Hero>();
        Lane currentLane = Lane.getLaneForCol(heroTile.getCol());

        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            if (h == hero) {
                continue;
            }
            ValorTile t = findHeroTile(h);
            if (t == null) {
                continue;
            }
            Lane l = Lane.getLaneForCol(t.getCol());
            if (l != currentLane) {
                validTargets.add(h);
            }
        }

        if (validTargets.isEmpty()) {
            System.out.println("No valid heroes in other lanes.");
            return false;
        }

        for (int i = 0; i < validTargets.size(); i++) {
            System.out.println(i + ") " + validTargets.get(i).getName());
        }

        int choice;
        try {
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return false;
        }

        if (choice < 0 || choice >= validTargets.size()) {
            System.out.println("Invalid choice.");
            return false;
        }

        Hero targetHero = validTargets.get(choice);
        ValorTile targetTile = findHeroTile(targetHero);

        int r = targetTile.getRow();
        int c = targetTile.getCol();

        int[][] offsets = {{0, 1}, {0, -1}, {1, 0}}; // right, left, behind
        for (int i = 0; i < offsets.length; i++) {
            int nr = r + offsets[i][0];
            int nc = c + offsets[i][1];
            if (map.isValidCoordinate(nr, nc)) {
                ValorTile dest = map.getTile(nr, nc);
                if (dest.isAccessible() && !dest.hasHero() && !dest.hasMonster()) {
                    heroTile.setHero(null);
                    dest.setHero(hero);
                    System.out.println("Teleported!");
                    addEvent(hero.getName() + " teleported near " + targetHero.getName());
                    return true;
                }
            }
        }

        System.out.println("No safe space around target hero.");
        return false;
    }

    private boolean attemptRecall(Hero hero) {
        Integer colObj = heroSpawnCols.get(hero);
        int spawnCol = (colObj == null) ? 0 : colObj.intValue();
        int spawnRow = 7;

        ValorTile spawnTile = map.getTile(spawnRow, spawnCol);
        if (spawnTile.hasHero() && spawnTile.getHero() != hero) {
            System.out.println("Spawn point blocked!");
            return false;
        }

        ValorTile current = findHeroTile(hero);
        if (current != null) {
            current.setHero(null);
        }
        spawnTile.setHero(hero);
        System.out.println(hero.getName() + " recalled to Nexus.");
        addEvent(hero.getName() + " recalled to Nexus.");
        return true;
    }

    private void visitMarket(Hero hero) {
        System.out.println(BOLD + "--- MARKET ---" + RESET);
        System.out.println("Hero: " + hero.getName() + "  Level: " + hero.getLevel()
                + "  HP: " + hero.getHealth() + "/" + hero.getMaxHealth()
                + "  MP: " + hero.getMana() + "/" + hero.getMaxMana()
                + "  Gold: " + hero.getGold());
        System.out.println("Type 'I' at any time to view hero stats.");
        boolean shopping = true;
        while (shopping) {
            System.out.println("1) Buy Potion");
            System.out.println("2) Buy Spell");
            System.out.println("3) Buy Armor/Weapon");
            System.out.println("4) Sell Item");
            System.out.println("5) View Hero Info");
            System.out.println("6) Exit");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
            if ("I".equalsIgnoreCase(choice)) {
                printHeroSheet(hero);
                continue;
            }
            switch (choice) {
                case "1":
                    buyItem(hero, market.getPotions());
                    break;
                case "2":
                    buyItem(hero, market.getSpells());
                    break;
                case "3":
                    System.out.println("Weapons or Armors? (W/A)");
                    String sub = scanner.nextLine().trim().toUpperCase();
                    if (sub.equals("W")) {
                        buyItem(hero, market.getWeapons());
                    } else if (sub.equals("A")) {
                        buyItem(hero, market.getArmors());
                    }
                    break;
                case "4":
                    if (hero.getInventory().getAll().isEmpty()) {
                        System.out.println("Inventory empty.");
                    } else {
                        Item i = hero.getInventory().getAll().get(0);
                        market.sell(hero, i);
                        System.out.println("Sold " + i.getName());
                    }
                    break;
                case "5":
                    printHeroSheet(hero);
                    break;
                case "6":
                    shopping = false;
                    break;
                default:
                    System.out.println("Invalid. Enter 1-6 or I for hero info.");
            }
        }
    }

    private <T extends Item> void buyItem(Hero hero, List<T> items) {
        if (items.isEmpty()) {
            System.out.println("No items available.");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            T it = items.get(i);
            System.out.println(i + ") " + it.getName()
                    + " cost:" + it.getPrice());
        }
        try {
            System.out.print("Buy index (or -1 to cancel): ");
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx == -1) {
                return;
            }
            if (idx >= 0 && idx < items.size()) {
                if (market.buy(hero, items.get(idx))) {
                    System.out.println(GREEN + "Bought!" + RESET);
                } else {
                    System.out.println("Cannot afford or level too low.");
                }
            } else {
                System.out.println("Invalid index.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
    }

    // wrapper names to match your original code
    private boolean usePotion(Hero h) {
        return attemptPotion(h);
    }

    private boolean equipItem(Hero h) {
        return attemptEquip(h);
    }

    private boolean attemptPotion(Hero h) {
        List<Item> potions = h.getInventory().getByType(Potion.class);
        if (potions.isEmpty()) {
            System.out.println("No potions.");
            return false;
        }
        Potion p = (Potion) potions.get(0);
        h.applyPotionEffect(p.getEffectAmount(), p.getAffectedStats());
        h.getInventory().remove(p);
        System.out.println(GREEN + "Used " + p.getName() + RESET);
        addEvent(h.getName() + " used potion " + p.getName());
        return true;
    }

    private boolean attemptEquip(Hero hero) {
        List<Item> weaponItems = hero.getInventory().getByType(Weapon.class);
        List<Weapon> weapons = new ArrayList<Weapon>();
        for (int i = 0; i < weaponItems.size(); i++) {
            weapons.add((Weapon) weaponItems.get(i));
        }

        List<Item> armorItems = hero.getInventory().getByType(Armor.class);
        List<Armor> armors = new ArrayList<Armor>();
        for (int i = 0; i < armorItems.size(); i++) {
            armors.add((Armor) armorItems.get(i));
        }

        System.out.println(BOLD + "--- EQUIP MENU ---" + RESET);
        System.out.println("1) Equip Weapon");
        System.out.println("2) Equip Armor");
        System.out.println("3) Cancel");
        System.out.print("> ");
        String type = scanner.nextLine().trim();

        if ("1".equals(type)) {
            if (weapons.isEmpty()) {
                System.out.println("No weapons.");
                return false;
            }
            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                System.out.println(i + ") " + w.getName()
                        + " Dmg:" + w.getDamage());
            }
            try {
                System.out.print("Index: ");
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < weapons.size()) {
                    hero.getEquipment().equipWeapon(weapons.get(idx));
                    System.out.println("Equipped " + weapons.get(idx).getName());
                    addEvent(hero.getName() + " equipped weapon "
                            + weapons.get(idx).getName());
                    return true;
                }
            } catch (Exception ignored) {
            }
        } else if ("2".equals(type)) {
            if (armors.isEmpty()) {
                System.out.println("No armor.");
                return false;
            }
            for (int i = 0; i < armors.size(); i++) {
                Armor a = armors.get(i);
                System.out.println(i + ") " + a.getName()
                        + " Red:" + a.getDamageReduction());
            }
            try {
                System.out.print("Index: ");
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < armors.size()) {
                    hero.getEquipment().equipArmor(armors.get(idx));
                    System.out.println("Equipped " + armors.get(idx).getName());
                    addEvent(hero.getName() + " equipped armor "
                            + armors.get(idx).getName());
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    // ----------------------------------------------------
    // MONSTER TURN LOGIC
    // ----------------------------------------------------
    private void processMonsterTurn() {
        System.out.println();
        System.out.println(BOLD + "--- Monsters Turn ---" + RESET);
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            ValorTile mTile = findMonsterTile(m);
            if (mTile == null) {
                continue;
            }

            List<Hero> targets = getHeroesInRange(mTile);
            if (!targets.isEmpty()) {
                Hero target = targets.get(0);
                System.out.println(BRIGHT_RED + m.getName() + RESET
                        + " attacks "
                        + BRIGHT_GREEN + target.getName() + RESET);

                int range = m.getMaxDamage() - m.getMinDamage() + 1;
                int rawDmg = random.nextInt(range) + m.getMinDamage();
                int armorRed = 0;
                if (target.getEquipment().getArmor() != null) {
                    armorRed = target.getEquipment().getArmor().getDamageReduction();
                }
                int actualDmg = Math.max(0, rawDmg - armorRed);

                target.takeDamage(actualDmg);
                System.out.println("Hit for " + RED + actualDmg + " damage" + RESET + "!");

                addEvent(m.getName() + " hit " + target.getName()
                        + " for " + actualDmg + " dmg");

                if (target.isFainted()) {
                    System.out.println(BRIGHT_RED + "*** " + target.getName()
                            + " has fainted! ***" + RESET);
                    ValorTile t = findHeroTile(target);
                    if (t != null) {
                        t.setHero(null);
                    }
                    addHeroFaint(target);
                    addEvent(target.getName() + " has fainted!");
                }
            } else {
                int nextRow = mTile.getRow() + 1;
                if (map.isValidCoordinate(nextRow, mTile.getCol())) {
                    ValorTile nextTile = map.getTile(nextRow, mTile.getCol());
                    if (nextTile.isAccessible() && !nextTile.hasMonster()) {
                        mTile.setMonster(null);
                        nextTile.setMonster(m);
                        System.out.println(m.getName() + " moved forward.");
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // HELPERS
    // ----------------------------------------------------
    private ValorTile findHeroTile(Hero h) {
        for (int r = 0; r < map.getHeight(); r++) {
            for (int c = 0; c < map.getWidth(); c++) {
                ValorTile t = map.getTile(r, c);
                if (t.getHero() == h) {
                    return t;
                }
            }
        }
        return null;
    }

    private ValorTile findMonsterTile(Monster m) {
        for (int r = 0; r < map.getHeight(); r++) {
            for (int c = 0; c < map.getWidth(); c++) {
                ValorTile t = map.getTile(r, c);
                if (t.getMonster() == m) {
                    return t;
                }
            }
        }
        return null;
    }

    private List<Monster> getTargetsInRange(ValorTile center) {
        List<Monster> targets = new ArrayList<Monster>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = center.getRow() + dr;
                int c = center.getCol() + dc;
                if (map.isValidCoordinate(r, c)) {
                    ValorTile t = map.getTile(r, c);
                    if (t.hasMonster()) {
                        targets.add(t.getMonster());
                    }
                }
            }
        }
        return targets;
    }

    private List<Hero> getHeroesInRange(ValorTile center) {
        List<Hero> targets = new ArrayList<Hero>();
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = center.getRow() + dr;
                int c = center.getCol() + dc;
                if (map.isValidCoordinate(r, c)) {
                    ValorTile t = map.getTile(r, c);
                    if (t.hasHero()) {
                        targets.add(t.getHero());
                    }
                }
            }
        }
        return targets;
    }

    private boolean checkWinCondition() {
        // Heroes win if they reach Monster Nexus (Row 0)
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            ValorTile t = findHeroTile(h);
            if (t != null && t.getType() == TileType.NEXUS && t.getRow() == 0) {
                System.out.println(BRIGHT_GREEN + BOLD
                        + "HEROES WIN! The Monster Nexus has been destroyed!"
                        + RESET);
                endReason = "Heroes reached the Monster Nexus.";
                gameOver = true;
                return true;
            }
        }
        // Monsters win if they reach Hero Nexus (Row 7)
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            ValorTile t = findMonsterTile(m);
            if (t != null && t.getType() == TileType.NEXUS && t.getRow() == 7) {
                System.out.println(BRIGHT_RED + BOLD
                        + "MONSTERS WIN! The Hero Nexus has been overrun!"
                        + RESET);
                endReason = "Monsters reached your Nexus.";
                gameOver = true;
                return true;
            }
        }
        return false;
    }

    private void endOfRound() {
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            if (!h.isFainted()) {
                h.heal((int) (h.getMaxHealth() * 0.10));
                h.gainMana((int) (h.getMaxMana() * 0.10));
            } else {
                System.out.println(h.getName() + " is reviving...");
                h.restoreFullHealth();
                h.restoreFullMana();

                Integer colObj = heroSpawnCols.get(h);
                int col = (colObj == null) ? 0 : colObj.intValue();
                ValorTile spawn = map.getTile(7, col);

                if (!spawn.hasHero()) {
                    spawn.setHero(h);
                    System.out.println(h.getName() + " respawned at Nexus.");
                } else {
                    boolean placed = false;
                    for (int c = 0; c < 8; c++) {
                        ValorTile alt = map.getTile(7, c);
                        if (alt.getType() == TileType.NEXUS && !alt.hasHero() && alt.isAccessible()) {
                            alt.setHero(h);
                            placed = true;
                            System.out.println(h.getName()
                                    + " respawned at Nexus (alternate spot).");
                            break;
                        }
                    }
                    if (!placed) {
                        System.out.println("Nexus is full! " + h.getName()
                                + " must wait for space.");
                    }
                }
            }
        }

        if (round % MONSTER_SPAWN_RATE == 0) {
            spawnMonsters();
        }

        // show mini event log at end of round
        printRecentEvents();
        eventLog.clear();
    }

    // ====== Event log & stats helpers ======
    private void addEvent(String text) {
        if (text == null || text.length() == 0) {
            return;
        }
        if (eventLog.size() == EVENT_LOG_SIZE) {
            eventLog.remove(0);
        }
        eventLog.add(text);
    }

    private void printRecentEvents() {
        if (eventLog.isEmpty()) {
            return;
        }
        System.out.println();
        System.out.println(BOLD + "Recent events:" + RESET);
        for (int i = 0; i < eventLog.size(); i++) {
            System.out.println("  - " + eventLog.get(i));
        }
    }

    private void addHeroDamage(Hero h, int dmg) {
        if (h == null || dmg <= 0) return;
        Integer cur = heroDamage.get(h);
        if (cur == null) cur = Integer.valueOf(0);
        heroDamage.put(h, Integer.valueOf(cur.intValue() + dmg));
    }

    private void addHeroKill(Hero h) {
        if (h == null) return;
        Integer cur = heroKills.get(h);
        if (cur == null) cur = Integer.valueOf(0);
        heroKills.put(h, Integer.valueOf(cur.intValue() + 1));
    }

    private void addHeroFaint(Hero h) {
        if (h == null) return;
        Integer cur = heroFaints.get(h);
        if (cur == null) cur = Integer.valueOf(0);
        heroFaints.put(h, Integer.valueOf(cur.intValue() + 1));
    }

    private void printPartyOverview() {
        System.out.println();
        System.out.println(BOLD + CYAN + "=== Party Overview ===" + RESET);
        System.out.format("%-18s %-4s %-12s %-12s %-8s %-8s %-8s%n",
                "Name", "Lvl", "HP", "MP", "Gold", "STR", "DEX/AGI");
        System.out.println("----------------------------------------------------------------");
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            String hp = h.getHealth() + "/" + h.getMaxHealth();
            String mp = h.getMana() + "/" + h.getMaxMana();
            String dexAgi = h.getDexterity() + "/" + h.getAgility();
            System.out.format("%-18s %-4d %-12s %-12s %-8d %-8d %-8s%n",
                    h.getName(), h.getLevel(), hp, mp, h.getGold(),
                    h.getStrength(), dexAgi);
        }
    }

    private void printHeroSheet(Hero h) {
        if (h == null) return;
        System.out.println();
        System.out.println(BOLD + CYAN + "=== Hero Sheet: " + h.getName() + " ===" + RESET);
        System.out.println("Level : " + h.getLevel());
        System.out.println("HP    : " + h.getHealth() + " / " + h.getMaxHealth());
        System.out.println("MP    : " + h.getMana() + " / " + h.getMaxMana());
        System.out.println("Gold  : " + h.getGold());
        System.out.println("STR   : " + h.getStrength());
        System.out.println("DEX   : " + h.getDexterity());
        System.out.println("AGI   : " + h.getAgility());
        Integer kills  = heroKills.get(h);
        Integer faints = heroFaints.get(h);
        Integer dmg    = heroDamage.get(h);
        System.out.println("Kills : " + (kills  == null ? 0 : kills.intValue()));
        System.out.println("Faints: " + (faints == null ? 0 : faints.intValue()));
        System.out.println("Damage: " + (dmg    == null ? 0 : dmg.intValue()));
        System.out.println();
    }

    private void printHelp() {
        System.out.println();
        System.out.println(BOLD + CYAN + "=== Help ===" + RESET);
        System.out.println("Movement: W/A/S/D to move within your lane.");
        System.out.println("  - Heroes move north (toward the Monster Nexus).");
        System.out.println("  - You cannot pass through Monsters or Inaccessible tiles.");
        System.out.println("Teleport (T): From a Nexus tile, move to an ally's tile in another lane.");
        System.out.println("Recall   (R): Return to your spawn Nexus.");
        System.out.println("Terrain bonuses:");
        System.out.println("  BUSH   (B): +10% Dexterity while standing on it.");
        System.out.println("  CAVE   (C): +10% Agility while standing on it.");
        System.out.println("  KOULOU (K): +10% Strength while standing on it.");
        System.out.println("Other commands:");
        System.out.println("  F: Physical attack   C: Cast spell   P: Use potion   E: Equip");
        System.out.println("  I: Show detailed hero stats");
        System.out.println("  H: Show party overview");
        System.out.println("  V: Re-print the map");
        System.out.println("  M: Visit Market (if on a Nexus tile)");
        System.out.println("  Q: Quit to game hub");
        System.out.println();
    }

    private void printRoundHeroBanner(Hero hero) {
        System.out.println();
        String title = " ROUND " + round + " – Hero Turn ";
        String border = repeatChar('=', title.length() + 8);
        System.out.println(BOLD + CYAN + border + RESET);
        System.out.println(BOLD + CYAN + "== " + title + "==" + RESET);
        System.out.println(BOLD + CYAN + border + RESET);

        ValorTile tile = findHeroTile(hero);
        int row = (tile == null) ? -1 : tile.getRow();
        int col = (tile == null) ? -1 : tile.getCol();
        String laneName = getLaneNameForCol(col);
        String bonus = (tile != null) ? tile.getBonusText() : "";

        System.out.println("Hero: " + BRIGHT_GREEN + hero.getName() + RESET
                + "  Pos: (" + row + "," + col + ")"
                + "  Lane: " + laneName + " " + bonus);
        System.out.println("HP: " + hero.getHealth() + "/" + hero.getMaxHealth()
                + "  MP: " + hero.getMana() + "/" + hero.getMaxMana()
                + "  Gold: " + hero.getGold()
                + "  Lvl: " + hero.getLevel());
        System.out.println();
    }

    private String getLaneNameForCol(int col) {
        if (col == 0 || col == 1) return "TOP";
        if (col == 3 || col == 4) return "MID";
        if (col == 6 || col == 7) return "BOT";
        return "?";
    }

    private void printActionMenu() {
        System.out.println(BOLD + "Actions:" + RESET);
        System.out.println(" [W] Move Up       [A] Move Left      [S] Move Down      [D] Move Right");
        System.out.println(" [F] Physical Attack   [C] Cast Spell   [P] Use Potion   [E] Equip");
        System.out.println(" [T] Teleport          [R] Recall       [I] Hero Info    [H] Party");
        System.out.println(" [V] View Map          [M] Market       [?] Help         [Q] Quit");
    }

    private void printEndGameSummary(String reason) {
        System.out.println();
        System.out.println(BOLD + CYAN + "===== Game Summary =====" + RESET);
        if (reason != null && reason.length() > 0) {
            System.out.println(reason);
        }
        System.out.println("Final round: " + round);
        System.out.println();
        System.out.format("%-18s %-4s %-12s %-12s %-8s %-6s %-6s %-6s %-6s%n",
                "Hero", "Lvl", "HP", "MP", "Gold", "Kills", "Faints", "Damage", "Lane");
        System.out.println("----------------------------------------------------------------------------");
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            ValorTile tile = findHeroTile(h);
            String laneName = (tile == null) ? "?" : getLaneNameForCol(tile.getCol());
            String hp = h.getHealth() + "/" + h.getMaxHealth();
            String mp = h.getMana() + "/" + h.getMaxMana();
            Integer kills  = heroKills.get(h);
            Integer faints = heroFaints.get(h);
            Integer dmg    = heroDamage.get(h);
            System.out.format("%-18s %-4d %-12s %-12s %-8d %-6d %-6d %-6d %-6s%n",
                    h.getName(), h.getLevel(), hp, mp, h.getGold(),
                    kills  == null ? 0 : kills.intValue(),
                    faints == null ? 0 : faints.intValue(),
                    dmg    == null ? 0 : dmg.intValue(),
                    laneName);
        }
        System.out.println("Thank you for playing Legends of Valor!");
    }

    private static String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
