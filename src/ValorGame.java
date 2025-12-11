/**
 * ValorGame orchestrates the Legends of Valor MOBA-style mode.
 * It coordinates map setup, hero/monster turns, movement/combat rules, market access,
 * and victory conditions across the three-lane board.
 *
 * Author: Codex (cleanup and rule refinements)
 *
 * Core rules summary:
 * - Heroes push north from Hero Nexus (row 7) toward Monster Nexus (row 0); monsters push south.
 * - Heroes attack at range 1; spells at range 2; monsters advance unless blocked by heroes.
 * - Market only at Hero Nexus; teleport only from Hero Nexus to an adjacent tile of an ally in another lane.
 * - Recall returns a hero to their spawn tile with partial restore and can be used once per turn.
 * - Q at any prompt exits back to the main menu.
 *
 * High-level algorithm:
 * - Initialize map and spawn heroes/monsters.
 * - For each round: print header, process each hero turn (movement/combat/shop), process monster turns,
 *   resolve respawns/regeneration/spawns, and check win conditions.
 */
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ValorGame {
    private ValorMap map;
    private final List<Hero> heroes;
    private final List<Monster> monsters;
    private final Map<Hero, Integer> heroSpawnCols; // Tracks which column a hero spawns in
    private final MonsterFactory monsterFactory;
    private final Market market;
    private final Scanner scanner; // Shared scanner
    private List<Monster> globalMonsterPool; // Cache loaded monsters
    private int round;
    private boolean gameOver;
    private final Random random = new Random();
    private final Set<Hero> recallUsedThisRound = new HashSet<Hero>();
    private boolean quitRequested;

    // Configuration
    private static final int MONSTER_SPAWN_RATE = 8; // Monsters spawn every 8 rounds

    public ValorGame(List<Hero> selectedHeroes, Market market, Scanner scanner) {
        if (selectedHeroes.size() != 3) {
            throw new IllegalArgumentException("Legends of Valor requires exactly 3 heroes.");
        }
        this.heroes = new ArrayList<Hero>(selectedHeroes);
        this.monsters = new ArrayList<Monster>();
        this.heroSpawnCols = new HashMap<Hero, Integer>();
        this.monsterFactory = new MonsterFactory();
        this.market = market;
        this.scanner = scanner;
        this.round = 0;
        this.gameOver = false;

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

    public void start() {
        System.out.println("Welcome to Legends of Valor!");
        showIntro();
        initializeGame();

        while (!gameOver) {
            round++;
            recallUsedThisRound.clear();
            System.out.println("\n========== ROUND " + round + " ==========");

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

            // 3. End of Round Updates (Respawn, Regen, etc.)
            endOfRound();
        }
        if (quitRequested) {
            System.out.println("Exited to main menu.");
        } else {
            System.out.println("Game Over!");
        }
    }

    private void showIntro() {
        System.out.println();
        System.out.println("+======================================+");
        System.out.println("|          LEGENDS OF VALOR            |");
        System.out.println("+======================================+");
        System.out.println("|  Push through three lanes, break     |");
        System.out.println("|  enemy lines, and reach their nexus. |");
        System.out.println("+======================================+");
        System.out.println();
        System.out.println("Exploration: Advance up lanes, respect obstacles, and hold your nexus.");
        System.out.println("Combat: Move into range, use basic attacks (F) or spells (C) to drop monsters.");
        System.out.println("Potions & Spells: Potions give instant stat boosts; spells deal damage and debuff foes.");
        System.out.println("Level-up: Earn gold/exp from kills; shop at your nexus to gear up and grow stronger.");
        System.out.println();
        System.out.println("Controls:");
        System.out.println("  W/A/S/D - Move");
        System.out.println("  F - Basic attack");
        System.out.println("  C - Cast spell");
        System.out.println("  T - Teleport to ally lane");
        System.out.println("  R - Recall to your nexus");
        System.out.println("  P - Use potion");
        System.out.println("  E - Equip gear");
        System.out.println("  I - Hero info");
        System.out.println("  M - Market (when on your nexus)");
        System.out.println("  Q - Quit to menu");
        System.out.println();
        System.out.print("Press ENTER to begin...");
        scanner.nextLine();
        System.out.println();
    }

    private void initializeGame() {
        // Assign heroes to lanes (Cols 0, 3, 6 are standard start points for lanes)
        int[] startCols = {0, 3, 6};

        for (int i = 0; i < 3; i++) {
            Hero h = heroes.get(i);
            int col = startCols[i];
            int row = 7; // Heroes start at bottom (Hero Nexus)

            heroSpawnCols.put(h, col);

            // Place hero on map
            ValorTile tile = map.getTile(row, col);
            tile.setHero(h);
        }

        // Spawn initial monsters
        spawnMonsters();
    }

    /**
     * Spawns one monster per lane at the top, finding the nearest free tile in that lane if the spawn is blocked.
     */
    private void spawnMonsters() {
        // New monsters have a level equal to highest level among the 3 heroes
        int maxHeroLevel = 1;
        for (Hero h : heroes) {
            if (h.getLevel() > maxHeroLevel) {
                maxHeroLevel = h.getLevel();
            }
        }

        int[] spawnCols = {1, 4, 7}; // Right side of each lane (Top of map)

        for (int col : spawnCols) {
            ValorTile spawnTile = findSpawnTileInLane(col);
            List<Monster> candidates = monsterFactory.spawnForLevel(globalMonsterPool, maxHeroLevel, 1);
            if (!candidates.isEmpty() && spawnTile != null) {
                Monster m = candidates.get(0);
                spawnTile.setMonster(m);
                monsters.add(m);
                System.out.println("A wild " + m.getName() + " appeared in " + Lane.getLaneForCol(col));
            } else if (spawnTile == null) {
                System.out.println("No space to spawn a monster in lane " + Lane.getLaneForCol(col));
            }
        }
    }

    // --- HERO TURN LOGIC ---

    /**
     * Handles a full round of hero actions, iterating each alive hero once.
     * Manages movement, combat, shopping access, skip/quit handling, and ensures menu prompts stay concise.
     */
    private void processHeroTurn() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                continue;
            }

            map.printMap();
            System.out.println("\nTurn: " + hero.getName() + " (" + getHeroLocationString(hero) + ")");
            boolean actionTaken = false;

            while (!actionTaken && !gameOver) {
                // Dynamic Menu: Show Market if on Nexus
                ValorTile tile = findHeroTile(hero);
                boolean onHeroNexus = (tile != null && isHeroOnHeroNexus(tile));

                System.out.println("Actions: W/A/S/D Move | F Attack | C Spell | T Teleport | R Recall | P Potion | E Equip | I Info | K Skip");
                System.out.print("         ");
                if (onHeroNexus) {
                    System.out.print("M Market | ");
                }
                System.out.println("Q Quit");

                System.out.print("> ");
                String input = scanner.nextLine().trim().toUpperCase();
                if (handleQuitInput(input)) {
                    return;
                }

                if ("W".equals(input)) {
                    actionTaken = attemptMove(hero, -1, 0);
                } else if ("A".equals(input)) {
                    actionTaken = attemptMove(hero, 0, -1);
                } else if ("S".equals(input)) {
                    actionTaken = attemptMove(hero, 1, 0);
                } else if ("D".equals(input)) {
                    actionTaken = attemptMove(hero, 0, 1);
                } else if ("F".equals(input)) {
                    actionTaken = attemptAttack(hero);
                } else if ("C".equals(input)) {
                    actionTaken = attemptCastSpell(hero);
                } else if ("T".equals(input)) {
                    actionTaken = attemptTeleport(hero);
                } else if ("R".equals(input)) {
                    actionTaken = attemptRecall(hero);
                } else if ("P".equals(input)) {
                    actionTaken = attemptPotion(hero);
                } else if ("E".equals(input)) {
                    actionTaken = attemptEquip(hero);
                } else if ("M".equals(input)) {
                    if (onHeroNexus) {
                        visitMarket(hero);
                        // Buying/selling do not count as actions
                    } else {
                        System.out.println("Must be at Hero Nexus to shop.");
                    }
                } else if ("I".equals(input)) {
                    System.out.println(hero.getName() + ": HP " + hero.getHealth() + "/" + hero.getMaxHealth()
                            + ", Mana " + hero.getMana() + ", Gold " + hero.getGold());
                } else if ("K".equals(input)) {
                    System.out.println("Turn skipped.");
                    actionTaken = true;
                } else {
                    System.out.println("Invalid command.");
                }
            }
        }
    }

    /**
     * Attempts to move the hero by the provided delta if the path is accessible and not blocked by monsters.
     */
    private boolean attemptMove(Hero hero, int dRow, int dCol) {
        ValorTile currentTile = findHeroTile(hero);
        if (currentTile == null) {
            return false;
        }

        int newRow = currentTile.getRow() + dRow;
        int newCol = currentTile.getCol() + dCol;

        Lane oldLane = Lane.getLaneForCol(currentTile.getCol());
        Lane newLane = Lane.getLaneForCol(newCol);
        if (newLane != null && oldLane != newLane) {
            System.out.println("You cannot leave your lane except by teleport.");
            return false;
        }

        if (!map.isValidCoordinate(newRow, newCol)) {
            System.out.println("Cannot move out of bounds.");
            return false;
        }

        ValorTile targetTile = map.getTile(newRow, newCol);

        // 1. Obstacle Handling
        if (targetTile.getType() == TileType.OBSTACLE) {
            System.out.print("Path blocked by Obstacle. Destroy it? (y/n): ");
            String ans = scanner.nextLine().trim();
            if (handleQuitInput(ans)) {
                return false;
            }
            if (ans.equalsIgnoreCase("y")) {
                targetTile.setType(TileType.PLAIN);
                System.out.println("Obstacle removed! (Turn consumed)");
                return true; // Removing obstacle consumes turn
            } else {
                return false;
            }
        }

        // 2. Accessibility
        if (!targetTile.isAccessible()) {
            System.out.println("Path blocked.");
            return false;
        }

        // 3. Hero Collision
        if (targetTile.hasHero()) {
            System.out.println("Tile occupied by another hero.");
            return false;
        }

        // 4. Monster Blocking Logic
        if (isBlockedByMonster(currentTile, targetTile)) {
            System.out.println("Cannot move past a monster in this lane without killing it!");
            return false;
        }

        // Apply Move
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

            // Only consider monsters that are in FRONT of the hero (towards row 0)
            if (mRow <= currentRow) {
                // If the hero tries to move to a row index LESS than the monster's row,
                // they are trying to bypass it.
                if (targetRow < mRow) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Executes a basic attack with range 1 (orthogonal/diagonal) if a monster is in reach.
     */
    private boolean attemptAttack(Hero hero) {
        ValorTile currentTile = findHeroTile(hero);
        List<Monster> targets = getMonstersInRange(currentTile, 1);

        if (targets.isEmpty()) {
            System.out.println("No monsters in range.");
            return false;
        }

        // Target selection
        Monster target = targets.get(0);
        if (targets.size() > 1) {
            System.out.println("Choose target:");
            for (int i = 0; i < targets.size(); i++) {
                System.out.println(i + ") " + targets.get(i).getName() + " (HP: " + targets.get(i).getHealth() + ")");
            }
            try {
                System.out.print("> ");
                String line = scanner.nextLine().trim();
                if (handleQuitInput(line)) {
                    return false;
                }
                int idx = Integer.parseInt(line);
                if (idx >= 0 && idx < targets.size()) {
                    target = targets.get(idx);
                }
            } catch (Exception e) {
                // ignore, keep default target
            }
        }

        System.out.println(hero.getName() + " attacks " + target.getName() + "!");

        // Calculate Damage with Terrain Bonus
        double terrainBonus = 1.0;
        if (currentTile.getType() == TileType.KOULOU) {
            terrainBonus = 1.1; // +10% Str
        }

        int weaponDmg = 0;
        if (hero.getEquipment().getWeapon() != null) {
            weaponDmg = hero.getEquipment().getWeapon().getDamage();
        }
        int totalStr = (int) ((hero.getStrength() + weaponDmg) * terrainBonus);

        int damage = Math.max(0, totalStr - target.getDefense());

        target.takeDamage(damage);
        System.out.println("Dealt " + damage + " damage.");

        if (target.isFainted()) {
            handleMonsterDeath(hero, target);
        }
        return true;
    }

    /**
     * Casts a spell at range 2, applying damage and debuff if a target is in reach.
     */
    private boolean attemptCastSpell(Hero hero) {
        List<Item> spellItems = hero.getInventory().getByType(Spell.class);
        if (spellItems.isEmpty()) {
            System.out.println("No spells in inventory.");
            return false;
        }

        System.out.println("Choose Spell:");
        for (int i = 0; i < spellItems.size(); i++) {
            Spell s = (Spell) spellItems.get(i);
            System.out.println(i + ") " + s.getName() + " (Mana: " + s.getManaCost() + ", Dmg: " + s.getBaseDamage() + ")");
        }

        Spell spell;
        try {
            System.out.print("Choose spell index: ");
            String input = scanner.nextLine().trim();
            if (handleQuitInput(input)) {
                return false;
            }
            int idx = Integer.parseInt(input);
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

        List<Monster> targets = getMonstersInRange(findHeroTile(hero), 2);
        if (targets.isEmpty()) {
            System.out.println("No targets in range.");
            return false;
        }

        // Target Selection (Same as Attack)
        Monster target = targets.get(0);
        if (targets.size() > 1) {
            System.out.println("Choose target:");
            for (int i = 0; i < targets.size(); i++) {
                System.out.println(i + ") " + targets.get(i).getName() + " (HP: " + targets.get(i).getHealth() + ")");
            }
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (handleQuitInput(input)) {
                    return false;
                }
                int idx = Integer.parseInt(input);
                if (idx >= 0 && idx < targets.size()) {
                    target = targets.get(idx);
                }
            } catch (Exception e) {
                // ignore, keep default
            }
        }

        hero.spendMana(spell.getManaCost());

        // Spell Damage + Dexterity Scaling
        int damage = spell.getBaseDamage() + (int) (hero.getDexterity() * 0.1);
        target.takeDamage(damage);
        System.out.println(hero.getName() + " casts " + spell.getName() + " on " + target.getName() + " for " + damage + " damage!");

        // Apply Debuff
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
        System.out.println(target.getName() + " died!");
        ValorTile mTile = findMonsterTile(target);
        if (mTile != null) {
            mTile.setMonster(null);
        }
        monsters.remove(target);
        hero.addGold(500 * target.getLevel());
        hero.addExperience(2 * target.getLevel());
        hero.levelUpIfReady();
    }

    /**
     * Teleports a hero from their own Nexus to a tile adjacent (side/behind) to an ally in another lane.
     */
    private boolean attemptTeleport(Hero hero) {
        // Teleport to adjacent space of a hero in a DIFFERENT lane
        ValorTile heroTile = findHeroTile(hero);
        if (heroTile == null || !isHeroOnHeroNexus(heroTile)) {
            System.out.println("Teleport is only available from your Nexus.");
            return false;
        }
        System.out.println("Choose target hero to teleport to:");
        List<Hero> validTargets = new ArrayList<Hero>();
        Lane currentLane = Lane.getLaneForCol(heroTile.getCol());

        for (Hero h : heroes) {
            if (h == hero) {
                continue;
            }
            ValorTile t = findHeroTile(h);
            Lane l = Lane.getLaneForCol(t.getCol());
            // Must be different lane
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
            String input = scanner.nextLine().trim();
            if (handleQuitInput(input)) {
                return false;
            }
            choice = Integer.parseInt(input);
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

        // Cannot teleport ahead (Row-1). Only Side or Behind.
        int[][] validOffsets = {{0, 1}, {0, -1}, {1, 0}}; // Right, Left, Behind

        for (int i = 0; i < validOffsets.length; i++) {
            int nr = r + validOffsets[i][0];
            int nc = c + validOffsets[i][1];
            if (nr < r) {
                continue; // cannot teleport into squares ahead of the target hero
            }
            if (map.isValidCoordinate(nr, nc)) {
                ValorTile dest = map.getTile(nr, nc);
                if (dest.isAccessible() && !dest.hasHero() && !dest.hasMonster()) {
                    heroTile.setHero(null);
                    dest.setHero(hero);
                    System.out.println("Teleported!");
                    return true;
                }
            }
        }

        System.out.println("No safe space around target hero.");
        return false;
    }

    /**
     * Recalls a hero to their original spawn tile (Hero Nexus) once per turn, restoring 50% HP/Mana.
     */
    private boolean attemptRecall(Hero hero) {
        if (recallUsedThisRound.contains(hero)) {
            System.out.println("Recall already used this turn.");
            return false;
        }
        int spawnCol = heroSpawnCols.get(hero);
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
        hero.heal(hero.getMaxHealth() / 2);
        hero.gainMana(hero.getMaxMana() / 2);
        System.out.println(hero.getName() + " recalled to Nexus.");
        recallUsedThisRound.add(hero);
        return true;
    }

    private void visitMarket(Hero hero) {
        ValorTile tile = findHeroTile(hero);
        if (!isHeroOnHeroNexus(tile)) {
            System.out.println("You must stand on Hero Nexus to shop.");
            return;
        }
        System.out.println("--- MARKET ---");
        System.out.println("Welcome, " + hero.getName() + ". You have " + hero.getGold() + " gold.");
        boolean shopping = true;
        while (shopping) {
            System.out.println("1) Buy Potion");
            System.out.println("2) Buy Spell");
            System.out.println("3) Buy Armor/Weapon");
            System.out.println("4) Sell Item");
            System.out.println("5) Exit");
            System.out.println("Q) Quit to menu");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
            if (handleQuitInput(choice)) {
                return;
            }
            if ("1".equals(choice)) {
                buyItem(hero, market.getPotions());
            } else if ("2".equals(choice)) {
                buyItem(hero, market.getSpells());
            } else if ("3".equals(choice)) {
                System.out.println("Weapons or Armors? (W/A)");
                String sub = scanner.nextLine().trim().toUpperCase();
                if ("W".equals(sub)) {
                    buyItem(hero, market.getWeapons());
                } else if ("A".equals(sub)) {
                    buyItem(hero, market.getArmors());
                }
            } else if ("4".equals(choice)) {
                List<Item> inv = hero.getInventory().getAll();
                if (inv.isEmpty()) {
                    System.out.println("Inventory empty.");
                } else {
                    for (int i = 0; i < inv.size(); i++) {
                        Item it = inv.get(i);
                        System.out.println(i + ") " + it.getName() + " price:" + it.getPrice());
                    }
                    System.out.print("Index to sell: ");
                    String idxStr = scanner.nextLine().trim();
                    if (!idxStr.equalsIgnoreCase("q")) {
                        try {
                            int idx = Integer.parseInt(idxStr);
                            if (idx >= 0 && idx < inv.size()) {
                                market.sell(hero, inv.get(idx));
                                System.out.println("Sold " + inv.get(idx).getName());
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid input.");
                        }
                    }
                }
            } else if ("5".equals(choice)) {
                shopping = false;
            } else {
                System.out.println("Invalid.");
            }
        }
    }

    private <T extends Item> void buyItem(Hero hero, List<T> items) {
        for (int i = 0; i < items.size(); i++) {
            System.out.println(i + ") " + items.get(i).getName() + " cost:" + items.get(i).getPrice());
        }
        try {
            System.out.print("Buy index: ");
            String input = scanner.nextLine().trim();
            if (handleQuitInput(input)) {
                return;
            }
            int idx = Integer.parseInt(input);
            if (idx >= 0 && idx < items.size()) {
                if (market.buy(hero, items.get(idx))) {
                    System.out.println("Bought!");
                } else {
                    System.out.println("Cannot afford or low level.");
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid.");
        }
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
        System.out.println("Used " + p.getName());
        return true;
    }

    private boolean attemptEquip(Hero hero) {
        // Equipping ends the turn
        List<Item> weaponItems = hero.getInventory().getByType(Weapon.class);
        List<Item> armorItems = hero.getInventory().getByType(Armor.class);

        List<Weapon> weapons = new ArrayList<Weapon>();
        for (Item it : weaponItems) {
            weapons.add((Weapon) it);
        }

        List<Armor> armors = new ArrayList<Armor>();
        for (Item it : armorItems) {
            armors.add((Armor) it);
        }

        System.out.println("--- EQUIP MENU ---");
        System.out.println("1) Equip Weapon");
        System.out.println("2) Equip Armor");
        System.out.println("3) Cancel");
        System.out.print("> ");
        String type = scanner.nextLine().trim();
        if (handleQuitInput(type)) {
            return false;
        }

        if ("1".equals(type)) {
            if (weapons.isEmpty()) {
                System.out.println("No weapons.");
                return false;
            }
            for (int i = 0; i < weapons.size(); i++) {
                System.out.println(i + ") " + weapons.get(i).getName() + " Dmg:" + weapons.get(i).getDamage());
            }
            try {
                System.out.print("Index: ");
                String input = scanner.nextLine().trim();
                if (handleQuitInput(input)) {
                    return false;
                }
                int idx = Integer.parseInt(input);
                if (idx >= 0 && idx < weapons.size()) {
                    hero.getEquipment().equipWeapon(weapons.get(idx));
                    System.out.println("Equipped " + weapons.get(idx).getName());
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        } else if ("2".equals(type)) {
            if (armors.isEmpty()) {
                System.out.println("No armor.");
                return false;
            }
            for (int i = 0; i < armors.size(); i++) {
                System.out.println(i + ") " + armors.get(i).getName() + " Red:" + armors.get(i).getDamageReduction());
            }
            try {
                System.out.print("Index: ");
                String input = scanner.nextLine().trim();
                if (handleQuitInput(input)) {
                    return false;
                }
                int idx = Integer.parseInt(input);
                if (idx >= 0 && idx < armors.size()) {
                    hero.getEquipment().equipArmor(armors.get(idx));
                    System.out.println("Equipped " + armors.get(idx).getName());
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return false;
    }

    // --- MONSTER TURN LOGIC ---

    /**
     * Resolves all monster turns: attack if a hero is in range 1, otherwise advance one step if lane is clear.
     * Monsters respect hero blocks in their lane and cannot move past them.
     */
    private void processMonsterTurn() {
        System.out.println("\n--- Monsters Turn ---");
        for (Monster m : new ArrayList<Monster>(monsters)) {
            ValorTile mTile = findMonsterTile(m);
            if (mTile == null) {
                continue;
            }

            List<Hero> targets = getHeroesInRange(mTile, 1);
            if (!targets.isEmpty()) {
                // Attack Hero
                Hero target = targets.get(0);
                System.out.println(m.getName() + " attacks " + target.getName());

                int rawDmg = random.nextInt(m.getMaxDamage() - m.getMinDamage() + 1) + m.getMinDamage();
                int armorReduction = 0;
                if (target.getEquipment().getArmor() != null) {
                    armorReduction = target.getEquipment().getArmor().getDamageReduction();
                }
                int actualDmg = Math.max(0, rawDmg - armorReduction);

                target.takeDamage(actualDmg);
                System.out.println("Hit for " + actualDmg + " damage!");

                if (target.isFainted()) {
                    System.out.println(target.getName() + " has fainted!");
                    ValorTile t = findHeroTile(target);
                    if (t != null) {
                        t.setHero(null);
                    }
                }
            } else if (!gameOver) {
                // Move Forward (South/Down) if clear
                int nextRow = mTile.getRow() + 1;
                Lane lane = Lane.getLaneForCol(mTile.getCol());
                if (map.isValidCoordinate(nextRow, mTile.getCol())
                        && lane != null
                        && !isHeroBlockingLane(nextRow, lane)) {
                    ValorTile nextTile = map.getTile(nextRow, mTile.getCol());
                    if (nextTile.isAccessible() && !nextTile.hasMonster() && !nextTile.hasHero()) {
                        mTile.setMonster(null);
                        nextTile.setMonster(m);
                        System.out.println(m.getName() + " moved forward.");
                    }
                }
            }
        }
    }

    // --- HELPER METHODS ---

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

    private boolean handleQuitInput(String input) {
        if ("q".equalsIgnoreCase(input)) {
            System.out.println("Exiting to main menu...");
            gameOver = true;
            quitRequested = true;
            return true;
        }
        return false;
    }

    private boolean isHeroOnHeroNexus(ValorTile tile) {
        return tile != null && tile.getType() == TileType.NEXUS && tile.getRow() == map.getHeight() - 1;
    }

    private boolean isHeroOnMonsterNexus(ValorTile tile) {
        return tile != null && tile.getType() == TileType.NEXUS && tile.getRow() == 0;
    }

    private boolean isMonsterOnHeroNexus(ValorTile tile) {
        return tile != null
                && tile.getType() == TileType.NEXUS
                && tile.getRow() == map.getHeight() - 1;
    }

    private List<Monster> getMonstersInRange(ValorTile center, int range) {
        List<Monster> targets = new ArrayList<Monster>();
        if (center == null) {
            return targets;
        }
        Lane attackerLane = Lane.getLaneForCol(center.getCol());
        for (int r = 0; r < map.getHeight(); r++) {
            for (int c = 0; c < map.getWidth(); c++) {
                Lane targetLane = Lane.getLaneForCol(c);
                if (attackerLane != targetLane) {
                    continue; // must be same lane
                }
                ValorTile t = map.getTile(r, c);
                if (t.hasMonster() && withinRange(center.getRow(), center.getCol(), r, c, range)) {
                    targets.add(t.getMonster());
                }
            }
        }
        return targets;
    }

    private boolean withinRange(int r1, int c1, int r2, int c2, int range) {
        if (r1 == r2 && c1 == c2) {
            return false;
        }
        return Math.max(Math.abs(r1 - r2), Math.abs(c1 - c2)) <= range;
    }

    private boolean isHeroBlockingLane(int row, Lane lane) {
        List<Integer> cols = getLaneColumns(lane);
        for (Integer c : cols) {
            if (map.isValidCoordinate(row, c)) {
                ValorTile t = map.getTile(row, c);
                if (t.hasHero()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Integer> getLaneColumns(Lane lane) {
        List<Integer> cols = new ArrayList<Integer>();
        if (lane == Lane.TOP) {
            cols.add(0);
            cols.add(1);
        } else if (lane == Lane.MID) {
            cols.add(3);
            cols.add(4);
        } else if (lane == Lane.BOT) {
            cols.add(6);
            cols.add(7);
        }
        return cols;
    }

    private ValorTile findSpawnTileInLane(int spawnCol) {
        Lane lane = Lane.getLaneForCol(spawnCol);
        if (lane == null) {
            return null;
        }
        List<Integer> cols = getLaneColumns(lane);
        for (int row = 0; row < map.getHeight(); row++) {
            for (Integer col : cols) {
                ValorTile tile = map.getTile(row, col);
                if (tile.isAccessible() && !tile.hasMonster() && !tile.hasHero()) {
                    return tile;
                }
            }
        }
        return null;
    }

    private List<Hero> getHeroesInRange(ValorTile center, int range) {
        List<Hero> targets = new ArrayList<Hero>();
        if (center == null) {
            return targets;
        }
        Lane attackerLane = Lane.getLaneForCol(center.getCol());
        for (int r = 0; r < map.getHeight(); r++) {
            for (int c = 0; c < map.getWidth(); c++) {
                Lane targetLane = Lane.getLaneForCol(c);
                if (attackerLane != targetLane) {
                    continue; // must be same lane
                }
                ValorTile t = map.getTile(r, c);
                if (t.hasHero() && withinRange(center.getRow(), center.getCol(), r, c, range)) {
                    targets.add(t.getHero());
                }
            }
        }
        return targets;
    }

    private String getHeroLocationString(Hero h) {
        ValorTile t = findHeroTile(h);
        if (t != null) {
            return t.getRow() + "," + t.getCol();
        }
        return "Unknown";
    }

    private boolean checkWinCondition() {
        // Heroes win if they reach Monster Nexus (Row 0)
        for (Hero h : heroes) {
            ValorTile t = findHeroTile(h);
            if (isHeroOnMonsterNexus(t)) {
                System.out.println("HEROES WIN! The Monster Nexus has been destroyed!");
                gameOver = true;
                return true;
            }
        }
        // Monsters win if they reach Hero Nexus (Row 7)
        for (Monster m : monsters) {
            ValorTile t = findMonsterTile(m);
            if (isMonsterOnHeroNexus(t)) {
                System.out.println("MONSTERS WIN! The Hero Nexus has been overrun!");
                gameOver = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves end-of-round updates: regen or respawn heroes, and periodically spawn monsters.
     */
    private void endOfRound() {
        // Recover HP/Mana
        for (Hero h : heroes) {
            if (!h.isFainted()) {
                h.heal((int) (h.getMaxHealth() * 0.10));
                h.gainMana((int) (h.getMaxMana() * 0.10));
            } else {
                // Respawn Logic
                System.out.println(h.getName() + " is reviving...");
                h.restoreFullHealth();
                h.restoreFullMana();

                int col = heroSpawnCols.get(h);
                ValorTile spawn = map.getTile(7, col);

                if (!spawn.hasHero()) {
                    spawn.setHero(h);
                    System.out.println(h.getName() + " respawned at Nexus.");
                } else {
                    // Safety valve: Try adjacent Nexus spots if primary is blocked
                    boolean placed = false;
                    for (int c = 0; c < 8; c++) {
                        ValorTile alt = map.getTile(7, c);
                        if (alt.getType() == TileType.NEXUS && !alt.hasHero() && alt.isAccessible()) {
                            alt.setHero(h);
                            placed = true;
                            System.out.println(h.getName() + " respawned at Nexus (alternate spot).");
                            break;
                        }
                    }
                    if (!placed) {
                        System.out.println("Nexus is full! " + h.getName() + " must wait for space.");
                    }
                }
            }
        }

        // Periodic Monster Spawns
        if (round % MONSTER_SPAWN_RATE == 0) {
            spawnMonsters();
        }
    }
}
