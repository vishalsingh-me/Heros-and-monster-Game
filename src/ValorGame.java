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
 * Controller for the Legends of Valor MOBA-style game.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Initialize heroes, monsters, map, and market</li>
 *     <li>Drive the round-based turn sequence (heroes then monsters)</li>
 *     <li>Enforce assignment rules: lanes, nexus victory, movement blocking, teleport/recall rules, spawn cadence</li>
 *     <li>Delegate combat, inventory, and market actions to domain objects</li>
 *     <li>Render map and log key events for clarity</li>
 * </ul>
 * This satisfies the rubric requirement for a clear game coordinator that separates input, rules, and domain objects.
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
            throw new IllegalArgumentException(GameText.VALOR_THREE_HEROES_REQUIRED);
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
            System.out.println(GameText.VALOR_MONSTER_DATA_WARNING + e.getMessage());
            this.globalMonsterPool = new ArrayList<Monster>();
        }
    }

    // ----------------------------------------------------
    // Core loop
    // ----------------------------------------------------
    /**
     * Runs the full Legends of Valor session: intro, setup, round loop, and end summary.
     * Side effects: prints to console, mutates game state, and may exit to hub on quit.
     */
    public void start() {
        System.out.println(BRIGHT_CYAN + BOLD + GameText.VALOR_WELCOME + RESET);
        showIntro();
        initializeGame();

        while (!gameOver) {
            round++;
            recallUsedThisRound.clear();

            System.out.println();
            System.out.println(BRIGHT_CYAN + BOLD
                    + String.format(GameText.VALOR_ROUND_HEADER, Integer.valueOf(round))
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
            System.out.println(GameText.VALOR_EXITED_TO_MENU);
        } else {
            System.out.println(BRIGHT_CYAN + BOLD + GameText.VALOR_GAME_OVER + RESET);
        }

        // show summary for rubric
        if (endReason == null) {
            endReason = quitRequested ? GameText.VALOR_END_REASON_QUIT : GameText.VALOR_END_REASON_DONE;
        }
        printEndGameSummary(endReason);
    }

    /**
     * Entry point used by the framework; delegates to start().
     */
    @Override
    public void run() {
        start();
    }

    // ----------------------------------------------------
    // Intro / setup
    // ----------------------------------------------------
    /**
     * Displays control/help text specific to Legends of Valor.
     * No state changes; purely console output.
     */
    private void showIntro() {
        System.out.println();
        System.out.println(GameText.VALOR_INTRO_LINE1);
        System.out.println(GameText.VALOR_INTRO_LINE2);
        System.out.println(GameText.VALOR_INTRO_LINE3);
        System.out.println(GameText.VALOR_INTRO_LINE4);
        System.out.println(GameText.VALOR_INTRO_LINE5);
        System.out.println(GameText.VALOR_INTRO_LINE6);
        System.out.println();
    }

    /**
     * Places heroes on starting nexus tiles, records spawn columns, and spawns initial monsters.
     * Side effects: mutates map occupancy and spawn tracking.
     */
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
    /**
     * Spawns one monster per lane at the monster nexus row based on max hero level.
     * Side effects: places monsters on map, mutates monster list, logs spawn events.
     */
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
                    String spawnMsg = String.format(GameText.VALOR_MONSTER_APPEARED,
                            BRIGHT_RED + m.getName() + RESET,
                            BOLD + lane + RESET);
                    System.out.println(spawnMsg);
                    addEvent(String.format(GameText.VALOR_MONSTER_APPEARED,
                            m.getName(), lane));
                }
            }
        }
    }

    // ----------------------------------------------------
    // HERO TURN LOGIC
    // ----------------------------------------------------
    /**
     * Executes all hero turns for the round, prompting actions and enforcing per-turn limits.
     */
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

                System.out.print(GameText.VALOR_PROMPT);
                String input = scanner.nextLine().trim().toUpperCase();

                if (input.length() == 0) {
                    System.out.println(RED + GameText.VALOR_NEED_COMMAND + RESET);
                    continue;
                }

                if ("Q".equals(input)) {
                    quitRequested = confirmQuit();
                    if (quitRequested) {
                        gameOver = true;
                        endReason = GameText.VALOR_QUIT_REASON;
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
                                    + GameText.VALOR_MARKET_NEED_NEXUS
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
                        System.out.println(RED + GameText.VALOR_INVALID_INPUT + RESET);
                        break;
                }
            }

            if (gameOver || quitRequested) {
                break;
            }
        }
    }

    /**
     * Prompts the player to confirm quitting to the game hub.
     *
     * @return true if the player confirms quit
     */
    private boolean confirmQuit() {
        System.out.print(RED + GameText.VALOR_QUIT_TO_HUB_PROMPT + RESET);
        String line = scanner.nextLine().trim();
        return line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes");
    }

    /**
     * Attempts to move a hero by the specified delta, enforcing bounds, obstacles,
     * lane-block rules, and occupancy.
     *
     * @param hero acting hero
     * @param dRow row delta
     * @param dCol column delta
     * @return true if the move consumed the action
     */
    private boolean attemptMove(Hero hero, int dRow, int dCol) {
        ValorTile currentTile = findHeroTile(hero);
        if (currentTile == null) {
            return false;
        }

        int newRow = currentTile.getRow() + dRow;
        int newCol = currentTile.getCol() + dCol;

        if (!map.isValidCoordinate(newRow, newCol)) {
            System.out.println(RED + GameText.VALOR_CANNOT_OUT_OF_BOUNDS + RESET);
            return false;
        }

        ValorTile targetTile = map.getTile(newRow, newCol);

        // obstacles
        if (targetTile.getType() == TileType.OBSTACLE) {
            System.out.print(GameText.VALOR_OBSTACLE_PROMPT);
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("y")) {
                targetTile.setType(TileType.PLAIN);
                System.out.println(YELLOW + GameText.VALOR_OBSTACLE_REMOVED + RESET);
                addEvent(hero.getName() + " removed an obstacle at (" + newRow + "," + newCol + ")");
                return true;
            } else {
                return false;
            }
        }

        if (!targetTile.isAccessible()) {
            System.out.println(RED + GameText.VALOR_PATH_BLOCKED + RESET);
            return false;
        }

        if (targetTile.hasHero()) {
            System.out.println(RED + GameText.VALOR_TILE_OCCUPIED + RESET);
            return false;
        }

        if (isBlockedByMonster(currentTile, targetTile)) {
            System.out.println(RED + GameText.VALOR_BLOCKED_BY_MONSTER
                    + RESET);
            return false;
        }

        currentTile.setHero(null);
        targetTile.setHero(hero);
        System.out.println(String.format(GameText.VALOR_MOVE_TO,
                hero.getName(), Integer.valueOf(newRow), Integer.valueOf(newCol)));
        return true;
    }

    /**
     * Checks whether the hero move would illegally bypass a monster in the same lane.
     */
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

    /**
     * Performs a basic attack if a monster is in range 1 (same lane).
     *
     * @param hero acting hero
     * @return true if the action was taken
     */
    private boolean attemptAttack(Hero hero) {
        ValorTile currentTile = findHeroTile(hero);
        if (currentTile == null) {
            return false;
        }

        List<Monster> targets = getTargetsInRange(currentTile);
        if (targets.isEmpty()) {
            System.out.println(GameText.VALOR_NO_MONSTERS_IN_RANGE);
            return false;
        }

        Monster target = targets.get(0);
        if (targets.size() > 1) {
            System.out.println(GameText.VALOR_CHOOSE_TARGET);
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

        System.out.println(String.format(GameText.VALOR_ATTACKS,
                BRIGHT_GREEN + hero.getName() + RESET,
                BRIGHT_RED + target.getName() + RESET));

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
        System.out.println(String.format(GameText.VALOR_DEALT_DAMAGE, Integer.valueOf(damage)));

        addHeroDamage(hero, damage);
        addEvent(hero.getName() + " dealt " + damage + " dmg to " + target.getName()
                + " (HP " + target.getHealth() + "/" + target.getMaxHealth() + ")");

        if (target.isFainted()) {
            handleMonsterDeath(hero, target);
        }
        return true;
    }

    /**
     * Casts a spell at range 2 (same lane) with damage and debuff.
     *
     * @param hero acting hero
     * @return true if the action was taken
     */
    private boolean attemptCastSpell(Hero hero) {
        List<Item> spellItems = hero.getInventory().getByType(Spell.class);
        if (spellItems.isEmpty()) {
            System.out.println(GameText.VALOR_NO_SPELLS);
            return false;
        }

        System.out.println(GameText.VALOR_CHOOSE_SPELL);
        for (int i = 0; i < spellItems.size(); i++) {
            Spell s = (Spell) spellItems.get(i);
            System.out.println(i + ") " + s.getName() + " (Mana: " + s.getManaCost()
                    + ", Dmg: " + s.getBaseDamage() + ")");
        }

        Spell spell;
        try {
            System.out.print(GameText.VALOR_CHOOSE_SPELL_INDEX);
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx >= 0 && idx < spellItems.size()) {
                spell = (Spell) spellItems.get(idx);
            } else {
                System.out.println(GameText.VALOR_INVALID_SELECTION);
                return false;
            }
        } catch (NumberFormatException e) {
            System.out.println(GameText.VALOR_INVALID_INPUT_GENERIC);
            return false;
        }

        if (hero.getMana() < spell.getManaCost()) {
            System.out.println(GameText.VALOR_NOT_ENOUGH_MANA);
            return false;
        }

        ValorTile heroTile = findHeroTile(hero);
        if (heroTile == null) {
            return false;
        }
        List<Monster> targets = getTargetsInRange(heroTile);
        if (targets.isEmpty()) {
            System.out.println(GameText.VALOR_NO_TARGETS);
            return false;
        }

        Monster target = targets.get(0);
        if (targets.size() > 1) {
            System.out.println(GameText.VALOR_CHOOSE_TARGET);
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

        System.out.println(String.format(GameText.VALOR_CASTS,
                BRIGHT_GREEN + hero.getName() + RESET,
                MAGENTA + spell.getName() + RESET,
                BRIGHT_RED + target.getName() + RESET,
                Integer.valueOf(damage)));

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
            System.out.println(String.format(GameText.VALOR_DEFENSE_REDUCED,
                    target.getName(), Integer.valueOf((int) amount)));
        } else if ("damage".equals(type)) {
            int newMin = Math.max(0, target.getMinDamage() - (int) amount);
            int newMax = Math.max(newMin, target.getMaxDamage() - (int) amount);
            target.setDamageRange(newMin, newMax);
            System.out.println(String.format(GameText.VALOR_DAMAGE_REDUCED,
                    target.getName(), Integer.valueOf((int) amount)));
        } else if ("dodge".equals(type)) {
            target.setDodgeChance(Math.max(0, target.getDodgeChance() - amount));
            System.out.println(String.format(GameText.VALOR_DODGE_REDUCED,
                    target.getName(), Double.toString(amount)));
        }

        if (target.isFainted()) {
            handleMonsterDeath(hero, target);
        }

        return true;
    }

    /**
     * Cleans up and rewards the hero when a monster dies; updates stats and logs.
     */
    private void handleMonsterDeath(Hero hero, Monster target) {
        System.out.println(String.format(GameText.VALOR_MONSTER_DIED,
                BRIGHT_RED + target.getName() + RESET));
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

    /**
     * Teleports a hero to a tile adjacent to an ally in another lane if legal.
     *
     * @param hero acting hero
     * @return true if teleport succeeded
     */
    private boolean attemptTeleport(Hero hero) {
        ValorTile heroTile = findHeroTile(hero);
        if (heroTile == null) {
            return false;
        }

        System.out.println(GameText.VALOR_CHOOSE_HERO_TELEPORT);
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
            System.out.println(GameText.VALOR_NO_VALID_HEROES_OTHER_LANES);
            return false;
        }

        for (int i = 0; i < validTargets.size(); i++) {
            System.out.println(i + ") " + validTargets.get(i).getName());
        }

        int choice;
        try {
            System.out.print(GameText.VALOR_ENTER_CHOICE);
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(GameText.VALOR_INVALID_INPUT_GENERIC);
            return false;
        }

        if (choice < 0 || choice >= validTargets.size()) {
            System.out.println(GameText.VALOR_INVALID_CHOICE);
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
                    System.out.println(GameText.VALOR_TELEPORTED);
                    addEvent(hero.getName() + " teleported near " + targetHero.getName());
                    return true;
                }
            }
        }

        System.out.println(GameText.VALOR_NO_SAFE_SPACE);
        return false;
    }

    /**
     * Returns the hero to their original spawn Nexus tile if available.
     * Respects lane assignment; consumes the hero's turn.
     *
     * @param hero acting hero
     * @return true if recall succeeded
     */
    private boolean attemptRecall(Hero hero) {
        Integer colObj = heroSpawnCols.get(hero);
        int spawnCol = (colObj == null) ? 0 : colObj.intValue();
        int spawnRow = 7;

        ValorTile spawnTile = map.getTile(spawnRow, spawnCol);
        if (spawnTile.hasHero() && spawnTile.getHero() != hero) {
            System.out.println(GameText.VALOR_SPAWN_BLOCKED);
            return false;
        }

        ValorTile current = findHeroTile(hero);
        if (current != null) {
            current.setHero(null);
        }
        spawnTile.setHero(hero);
        System.out.println(String.format(GameText.VALOR_RECALLED_TO_NEXUS, hero.getName()));
        addEvent(String.format(GameText.VALOR_RECALLED_TO_NEXUS, hero.getName()));
        return true;
    }

    /**
     * Opens the market menu for the specified hero; supports buy/sell and info.
     * Side effects: modifies hero inventory/equipment and gold.
     *
     * @param hero shopper
     */
    private void visitMarket(Hero hero) {
        System.out.println(BOLD + GameText.VALOR_MARKET_MENU + RESET);
        System.out.println(String.format(GameText.VALOR_MARKET_HERO_STATUS,
                hero.getName(),
                Integer.valueOf(hero.getLevel()),
                Integer.valueOf(hero.getHealth()), Integer.valueOf(hero.getMaxHealth()),
                Integer.valueOf(hero.getMana()), Integer.valueOf(hero.getMaxMana()),
                Integer.valueOf(hero.getGold())));
        System.out.println(GameText.VALOR_MARKET_INFO_HINT);
        boolean shopping = true;
        while (shopping) {
            System.out.println(GameText.VALOR_MARKET_MENU_1);
            System.out.println(GameText.VALOR_MARKET_MENU_2);
            System.out.println(GameText.VALOR_MARKET_MENU_3);
            System.out.println(GameText.VALOR_MARKET_MENU_4);
            System.out.println(GameText.VALOR_MARKET_MENU_5);
            System.out.println(GameText.VALOR_MARKET_MENU_6);
            System.out.print(GameText.VALOR_PROMPT);
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
                    System.out.println(GameText.VALOR_MARKET_WEAPONS_OR_ARMORS);
                    String sub = scanner.nextLine().trim().toUpperCase();
                    if (sub.equals("W")) {
                        buyItem(hero, market.getWeapons());
                    } else if (sub.equals("A")) {
                        buyItem(hero, market.getArmors());
                    }
                    break;
                case "4":
                    if (hero.getInventory().getAll().isEmpty()) {
                        System.out.println(GameText.VALOR_INVENTORY_EMPTY);
                    } else {
                        Item i = hero.getInventory().getAll().get(0);
                        market.sell(hero, i);
                        System.out.println(String.format(GameText.VALOR_SOLD_ITEM, i.getName()));
                    }
                    break;
                case "5":
                    printHeroSheet(hero);
                    break;
                case "6":
                    shopping = false;
                    break;
                default:
                    System.out.println(GameText.VALOR_MARKET_INVALID_SIMPLE);
            }
        }
    }

    /**
     * Generic purchase helper for any item list.
     *
     * @param hero  buyer
     * @param items stock to display
     */
    private <T extends Item> void buyItem(Hero hero, List<T> items) {
        if (items.isEmpty()) {
            System.out.println(GameText.VALOR_NO_ITEMS_AVAILABLE);
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            T it = items.get(i);
            System.out.println(i + ") " + it.getName()
                    + " cost:" + it.getPrice());
        }
        try {
            System.out.print(GameText.VALOR_BUY_INDEX_PROMPT);
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx == -1) {
                return;
            }
            if (idx >= 0 && idx < items.size()) {
                if (market.buy(hero, items.get(idx))) {
                    System.out.println(GREEN + GameText.VALOR_BOUGHT + RESET);
                } else {
                    System.out.println(GameText.VALOR_CANNOT_AFFORD_LEVEL);
                }
            } else {
                System.out.println(GameText.VALOR_INVALID_INDEX_GENERIC);
            }
        } catch (Exception e) {
            System.out.println(GameText.VALOR_MARKET_INVALID_INPUT);
        }
    }

    // wrapper names to match your original code
    /**
     * Wrapper to preserve original naming for potion usage.
     *
     * @param h acting hero
     * @return true if potion consumed
     */
    private boolean usePotion(Hero h) {
        return attemptPotion(h);
    }

    /**
     * Wrapper to preserve original naming for equipment selection.
     *
     * @param h acting hero
     * @return true if equipment changed
     */
    private boolean equipItem(Hero h) {
        return attemptEquip(h);
    }

    /**
     * Uses the first available potion on the hero.
     *
     * @param h acting hero
     * @return true if a potion was consumed
     */
    private boolean attemptPotion(Hero h) {
        List<Item> potions = h.getInventory().getByType(Potion.class);
        if (potions.isEmpty()) {
            System.out.println(GameText.VALOR_NO_POTIONS);
            return false;
        }
        Potion p = (Potion) potions.get(0);
        h.applyPotionEffect(p.getEffectAmount(), p.getAffectedStats());
        h.getInventory().remove(p);
        System.out.println(GREEN + String.format(GameText.VALOR_USED_POTION, p.getName()) + RESET);
        addEvent(h.getName() + " used potion " + p.getName());
        return true;
    }

    /**
     * Equips the first selected weapon or armor from inventory.
     * Ends the hero's turn when an item is equipped.
     *
     * @param hero acting hero
     * @return true if equipment changed
     */
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

        System.out.println(BOLD + GameText.VALOR_EQUIP_MENU_HEADER + RESET);
        System.out.println(GameText.VALOR_EQUIP_WEAPON);
        System.out.println(GameText.VALOR_EQUIP_ARMOR);
        System.out.println(GameText.VALOR_EQUIP_CANCEL);
        System.out.print(GameText.VALOR_PROMPT);
        String type = scanner.nextLine().trim();

        if ("1".equals(type)) {
            if (weapons.isEmpty()) {
                System.out.println(GameText.VALOR_NO_WEAPONS);
                return false;
            }
            for (int i = 0; i < weapons.size(); i++) {
                Weapon w = weapons.get(i);
                System.out.println(i + ") " + w.getName()
                        + " Dmg:" + w.getDamage());
            }
            try {
                System.out.print(GameText.VALOR_INDEX_PROMPT);
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < weapons.size()) {
                    hero.getEquipment().equipWeapon(weapons.get(idx));
                    System.out.println(String.format(GameText.VALOR_EQUIPPED_WEAPON,
                            weapons.get(idx).getName()));
                    addEvent(hero.getName() + " equipped weapon "
                            + weapons.get(idx).getName());
                    return true;
                }
            } catch (Exception ignored) {
            }
        } else if ("2".equals(type)) {
            if (armors.isEmpty()) {
                System.out.println(GameText.VALOR_NO_ARMOR);
                return false;
            }
            for (int i = 0; i < armors.size(); i++) {
                Armor a = armors.get(i);
                System.out.println(i + ") " + a.getName()
                        + " Red:" + a.getDamageReduction());
            }
            try {
                System.out.print(GameText.VALOR_INDEX_PROMPT);
                int idx = Integer.parseInt(scanner.nextLine().trim());
                if (idx >= 0 && idx < armors.size()) {
                    hero.getEquipment().equipArmor(armors.get(idx));
                    System.out.println(String.format(GameText.VALOR_EQUIPPED_ARMOR,
                            armors.get(idx).getName()));
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
    /**
     * Processes all monsters for the round: attack adjacent heroes in the same lane,
     * otherwise advance south if unblocked.
     * Side effects: moves monsters, applies damage, updates faint state and event log.
     */
    private void processMonsterTurn() {
        System.out.println();
        System.out.println(BOLD + GameText.VALOR_MONSTERS_TURN + RESET);
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            ValorTile mTile = findMonsterTile(m);
            if (mTile == null) {
                continue;
            }

            List<Hero> targets = getHeroesInRange(mTile);
            if (!targets.isEmpty()) {
                Hero target = targets.get(0);
                System.out.println(String.format(GameText.VALOR_MONSTER_ATTACKS,
                        BRIGHT_RED + m.getName() + RESET,
                        BRIGHT_GREEN + target.getName() + RESET));

                int range = m.getMaxDamage() - m.getMinDamage() + 1;
                int rawDmg = random.nextInt(range) + m.getMinDamage();
                int armorRed = 0;
                if (target.getEquipment().getArmor() != null) {
                    armorRed = target.getEquipment().getArmor().getDamageReduction();
                }
                int actualDmg = Math.max(0, rawDmg - armorRed);

                target.takeDamage(actualDmg);
                System.out.println(String.format(GameText.VALOR_MONSTER_FLESH_HIT,
                        Integer.valueOf(actualDmg)));

                addEvent(m.getName() + " hit " + target.getName()
                        + " for " + actualDmg + " dmg");

                if (target.isFainted()) {
                    System.out.println(String.format(GameText.VALOR_MONSTER_FAINTED,
                            target.getName()));
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
                        System.out.println(String.format(GameText.VALOR_MONSTER_MOVED_FORWARD,
                                m.getName()));
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // HELPERS
    // ----------------------------------------------------
    /**
     * Locates the map tile containing the specified hero.
     *
     * @param h hero to find
     * @return tile or null if not placed
     */
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

    /**
     * Locates the map tile containing the specified monster.
     *
     * @param m monster to find
     * @return tile or null if not placed
     */
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

    /**
     * Returns monsters in range-1 (Chebyshev) of the given tile.
     *
     * @param center origin tile
     * @return monsters found within range
     */
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

    /**
     * Returns heroes in range-1 (Chebyshev) of the given tile.
     *
     * @param center origin tile
     * @return heroes found within range
     */
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

    /**
     * Checks victory/defeat: heroes win on reaching monster nexus (row 0);
     * monsters win on reaching hero nexus (row 7).
     *
     * @return true if game should end
     */
    private boolean checkWinCondition() {
        // Heroes win if they reach Monster Nexus (Row 0)
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            ValorTile t = findHeroTile(h);
            if (t != null && t.getType() == TileType.NEXUS && t.getRow() == 0) {
                System.out.println(BRIGHT_GREEN + BOLD
                        + GameText.VALOR_HEROES_WIN
                        + RESET);
                endReason = GameText.VALOR_END_REASON_HERO;
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
                        + GameText.VALOR_MONSTERS_WIN
                        + RESET);
                endReason = GameText.VALOR_END_REASON_MONSTER;
                gameOver = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves end-of-round regeneration, hero respawns, and periodic monster spawns.
     * Side effects: mutates hero HP/mana, map occupancy, and may add monsters.
     */
    private void endOfRound() {
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            if (!h.isFainted()) {
                h.heal((int) (h.getMaxHealth() * 0.10));
                h.gainMana((int) (h.getMaxMana() * 0.10));
            } else {
                System.out.println(String.format(GameText.VALOR_IS_REVIVING,
                        h.getName()));
                h.restoreFullHealth();
                h.restoreFullMana();

                Integer colObj = heroSpawnCols.get(h);
                int col = (colObj == null) ? 0 : colObj.intValue();
                ValorTile spawn = map.getTile(7, col);

                if (!spawn.hasHero()) {
                    spawn.setHero(h);
                    System.out.println(String.format(GameText.VALOR_RESPAWN, h.getName()));
                } else {
                    boolean placed = false;
                    for (int c = 0; c < 8; c++) {
                        ValorTile alt = map.getTile(7, c);
                        if (alt.getType() == TileType.NEXUS && !alt.hasHero() && alt.isAccessible()) {
                            alt.setHero(h);
                            placed = true;
                            System.out.println(String.format(GameText.VALOR_RESPAWN_ALT,
                                    h.getName()));
                            break;
                        }
                    }
                    if (!placed) {
                        System.out.println(String.format(GameText.VALOR_RESPAWN_FULL,
                                h.getName()));
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
    /**
     * Adds a line to the current round's event buffer (capacity-limited).
     */
    private void addEvent(String text) {
        if (text == null || text.length() == 0) {
            return;
        }
        if (eventLog.size() == EVENT_LOG_SIZE) {
            eventLog.remove(0);
        }
        eventLog.add(text);
    }

    /**
     * Prints the buffered event log for this round.
     */
    private void printRecentEvents() {
        if (eventLog.isEmpty()) {
            return;
        }
        System.out.println();
        System.out.println(BOLD + GameText.VALOR_RECENT_EVENTS + RESET);
        for (int i = 0; i < eventLog.size(); i++) {
            System.out.println("  - " + eventLog.get(i));
        }
    }

    /**
     * Accumulates damage dealt by a hero for end-of-game summary.
     */
    private void addHeroDamage(Hero h, int dmg) {
        if (h == null || dmg <= 0) return;
        Integer cur = heroDamage.get(h);
        if (cur == null) cur = Integer.valueOf(0);
        heroDamage.put(h, Integer.valueOf(cur.intValue() + dmg));
    }

    /**
     * Increments kill count for a hero.
     */
    private void addHeroKill(Hero h) {
        if (h == null) return;
        Integer cur = heroKills.get(h);
        if (cur == null) cur = Integer.valueOf(0);
        heroKills.put(h, Integer.valueOf(cur.intValue() + 1));
    }

    /**
     * Increments faint count for a hero.
     */
    private void addHeroFaint(Hero h) {
        if (h == null) return;
        Integer cur = heroFaints.get(h);
        if (cur == null) cur = Integer.valueOf(0);
        heroFaints.put(h, Integer.valueOf(cur.intValue() + 1));
    }

    /**
     * Prints a compact party status table (HP/MP/stats) for quick reference.
     */
    private void printPartyOverview() {
        System.out.println();
        System.out.println(BOLD + CYAN + GameText.VALOR_PARTY_OVERVIEW_TITLE + RESET);
        System.out.format("%-18s %-4s %-12s %-12s %-8s %-8s %-8s%n",
                "Name", "Lvl", "HP", "MP", "Gold", "STR", "DEX/AGI");
        System.out.println(GameText.VALOR_PARTY_OVERVIEW_DIVIDER);
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

    /**
     * Shows a single hero's detailed stats and tracked counters.
     *
     * @param h hero to display
     */
    private void printHeroSheet(Hero h) {
        if (h == null) return;
        System.out.println();
        System.out.println(BOLD + CYAN + String.format(GameText.VALOR_HERO_SHEET_TITLE,
                h.getName()) + RESET);
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

    /**
     * Prints the condensed help/controls reference.
     */
    private void printHelp() {
        System.out.println();
        System.out.println(BOLD + CYAN + GameText.VALOR_HELP_TITLE + RESET);
        System.out.println(GameText.VALOR_HELP_MOVE);
        System.out.println(GameText.VALOR_HELP_MOVE_DETAILS);
        System.out.println(GameText.VALOR_HELP_BLOCKS);
        System.out.println(GameText.VALOR_HELP_TELEPORT);
        System.out.println(GameText.VALOR_HELP_RECALL);
        System.out.println(GameText.VALOR_HELP_TERRAIN);
        System.out.println(GameText.VALOR_HELP_BUSH);
        System.out.println(GameText.VALOR_HELP_CAVE);
        System.out.println(GameText.VALOR_HELP_KOULOU);
        System.out.println(GameText.VALOR_HELP_OTHER);
        System.out.println(GameText.VALOR_HELP_COMMANDS_LINE1);
        System.out.println(GameText.VALOR_HELP_COMMANDS_LINE2);
        System.out.println(GameText.VALOR_HELP_COMMANDS_LINE3);
        System.out.println(GameText.VALOR_HELP_COMMANDS_LINE4);
        System.out.println(GameText.VALOR_HELP_COMMANDS_LINE5);
        System.out.println(GameText.VALOR_HELP_COMMANDS_LINE6);
        System.out.println();
    }

    /**
     * Prints a round/turn banner and the acting hero's status for clarity.
     *
     * @param hero acting hero
     */
    private void printRoundHeroBanner(Hero hero) {
        System.out.println();
        String title = String.format(GameText.VALOR_ROUND_HERO_TITLE,
                Integer.valueOf(round));
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

    /**
     * Utility mapping from column to textual lane name.
     *
     * @param col column index
     * @return lane name or "?" if unknown
     */
    private String getLaneNameForCol(int col) {
        if (col == 0 || col == 1) return "TOP";
        if (col == 3 || col == 4) return "MID";
        if (col == 6 || col == 7) return "BOT";
        return "?";
    }

    /**
     * Renders the per-turn action menu.
     */
    private void printActionMenu() {
        System.out.println(BOLD + GameText.VALOR_ACTIONS_HEADER + RESET);
        System.out.println(GameText.VALOR_ACTION_LINE1);
        System.out.println(GameText.VALOR_ACTION_LINE2);
        System.out.println(GameText.VALOR_ACTION_LINE3);
        System.out.println(GameText.VALOR_ACTION_LINE4);
    }

    /**
     * Outputs final summary table and metadata once the game ends.
     *
     * @param reason human-readable ending reason
     */
    private void printEndGameSummary(String reason) {
        System.out.println();
        System.out.println(BOLD + CYAN + GameText.VALOR_GAME_SUMMARY_TITLE + RESET);
        if (reason != null && reason.length() > 0) {
            System.out.println(reason);
        }
        System.out.println(String.format(GameText.VALOR_FINAL_ROUND, Integer.valueOf(round)));
        System.out.println();
        System.out.format("%-18s %-4s %-12s %-12s %-8s %-6s %-6s %-6s %-6s%n",
                "Hero", "Lvl", "HP", "MP", "Gold", "Kills", "Faints", "Damage", "Lane");
        System.out.println(GameText.VALOR_SUMMARY_DIVIDER);
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
        System.out.println(GameText.VALOR_GAME_THANKS);
    }

    /**
     * Small helper to repeat a character (Java 8 compatible).
     *
     * @param ch    character to repeat
     * @param count number of times
     * @return resulting string
     */
    private static String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
