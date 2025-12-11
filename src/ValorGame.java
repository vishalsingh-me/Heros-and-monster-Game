import java.io.IOException;
import java.nio.file.Path;
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
        initializeGame();

        while (!gameOver) {
            round++;
            System.out.println("\n=== ROUND " + round + " ===");

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
        System.out.println("Game Over!");
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
            ValorTile tile = map.getTile(0, col);
            // Only spawn if spot is free of monsters
            if (!tile.hasMonster()) {
                List<Monster> candidates = monsterFactory.spawnForLevel(globalMonsterPool, maxHeroLevel, 1);
                if (!candidates.isEmpty()) {
                    Monster m = candidates.get(0);
                    tile.setMonster(m);
                    monsters.add(m);
                    System.out.println("A wild " + m.getName() + " appeared in " + Lane.getLaneForCol(col));
                }
            }
        }
    }

    // --- HERO TURN LOGIC ---

    private void processHeroTurn() {
        for (Hero hero : heroes) {
            if (hero.isFainted()) {
                continue;
            }

            map.printMap();
            System.out.println("\nTurn: " + hero.getName() + " (" + getHeroLocationString(hero) + ")");
            boolean actionTaken = false;

            while (!actionTaken) {
                // Dynamic Menu: Show Market if on Nexus
                ValorTile tile = findHeroTile(hero);
                boolean onNexus = (tile != null && tile.getType() == TileType.NEXUS);

                System.out.print("Actions: (W/A/S/D) Move | (F) Attack | (C) Cast Spell | (T) Teleport | (R) Recall | (P) Potion | (E) Equip | (I) Info");
                if (onNexus) {
                    System.out.print(" | (M) Market");
                }
                System.out.println(" | (Q) Quit");

                System.out.print("> ");
                String input = scanner.nextLine().trim().toUpperCase();

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
                    if (onNexus) {
                        visitMarket(hero);
                        // Buying/selling do not count as actions
                    } else {
                        System.out.println("Must be at Nexus to shop.");
                    }
                } else if ("I".equals(input)) {
                    System.out.println(hero.getName() + ": HP " + hero.getHealth() + "/" + hero.getMaxHealth()
                            + ", Mana " + hero.getMana() + ", Gold " + hero.getGold());
                } else if ("Q".equals(input)) {
                    System.out.println("Quitting game...");
                    System.exit(0);
                } else {
                    System.out.println("Invalid command.");
                }
            }
        }
    }

    private boolean attemptMove(Hero hero, int dRow, int dCol) {
        ValorTile currentTile = findHeroTile(hero);
        if (currentTile == null) {
            return false;
        }

        int newRow = currentTile.getRow() + dRow;
        int newCol = currentTile.getCol() + dCol;

        if (!map.isValidCoordinate(newRow, newCol)) {
            System.out.println("Cannot move out of bounds.");
            return false;
        }

        ValorTile targetTile = map.getTile(newRow, newCol);

        // 1. Obstacle Handling
        if (targetTile.getType() == TileType.OBSTACLE) {
            System.out.print("Path blocked by Obstacle. Destroy it? (y/n): ");
            String ans = scanner.nextLine().trim();
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

    private boolean attemptAttack(Hero hero) {
        ValorTile currentTile = findHeroTile(hero);
        List<Monster> targets = getTargetsInRange(currentTile);

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
                int idx = Integer.parseInt(scanner.nextLine().trim());
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

        List<Monster> targets = getTargetsInRange(findHeroTile(hero));
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
                int idx = Integer.parseInt(scanner.nextLine().trim());
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

    private boolean attemptTeleport(Hero hero) {
        // Teleport to adjacent space of a hero in a DIFFERENT lane
        System.out.println("Choose target hero to teleport to:");
        List<Hero> validTargets = new ArrayList<Hero>();
        ValorTile heroTile = findHeroTile(hero);
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

        // Cannot teleport ahead (Row-1). Only Side or Behind.
        int[][] validOffsets = {{0, 1}, {0, -1}, {1, 0}}; // Right, Left, Behind

        for (int i = 0; i < validOffsets.length; i++) {
            int nr = r + validOffsets[i][0];
            int nc = c + validOffsets[i][1];
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

    private boolean attemptRecall(Hero hero) {
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
        System.out.println(hero.getName() + " recalled to Nexus.");
        return true;
    }

    private void visitMarket(Hero hero) {
        System.out.println("--- MARKET ---");
        System.out.println("Welcome, " + hero.getName() + ". You have " + hero.getGold() + " gold.");
        boolean shopping = true;
        while (shopping) {
            System.out.println("1) Buy Potion");
            System.out.println("2) Buy Spell");
            System.out.println("3) Buy Armor/Weapon");
            System.out.println("4) Sell Item");
            System.out.println("5) Exit");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
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
                if (hero.getInventory().getAll().isEmpty()) {
                    System.out.println("Inventory empty.");
                } else {
                    // For brevity, selling first item, or you can implement a sell menu
                    Item i = hero.getInventory().getAll().get(0);
                    market.sell(hero, i);
                    System.out.println("Sold " + i.getName());
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
            int idx = Integer.parseInt(scanner.nextLine().trim());
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
                int idx = Integer.parseInt(scanner.nextLine().trim());
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
                int idx = Integer.parseInt(scanner.nextLine().trim());
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

    private void processMonsterTurn() {
        System.out.println("\n--- Monsters Turn ---");
        for (Monster m : new ArrayList<Monster>(monsters)) {
            ValorTile mTile = findMonsterTile(m);
            if (mTile == null) {
                continue;
            }

            List<Hero> targets = getHeroesInRange(mTile);
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
            } else {
                // Move Forward (South/Down)
                int nextRow = mTile.getRow() + 1;
                if (map.isValidCoordinate(nextRow, mTile.getCol())) {
                    ValorTile nextTile = map.getTile(nextRow, mTile.getCol());
                    // Allow move if accessible AND no monster (Hero presence is OK)
                    if (nextTile.isAccessible() && !nextTile.hasMonster()) {
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

    private List<Monster> getTargetsInRange(ValorTile center) {
        List<Monster> targets = new ArrayList<Monster>();
        // Range: Current + Neighbors (3x3 grid)
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
            if (t != null && t.getType() == TileType.NEXUS && t.getRow() == 0) {
                System.out.println("HEROES WIN! The Monster Nexus has been destroyed!");
                gameOver = true;
                return true;
            }
        }
        // Monsters win if they reach Hero Nexus (Row 7)
        for (Monster m : monsters) {
            ValorTile t = findMonsterTile(m);
            if (t != null && t.getType() == TileType.NEXUS && t.getRow() == 7) {
                System.out.println("MONSTERS WIN! The Hero Nexus has been overrun!");
                gameOver = true;
                return true;
            }
        }
        return false;
    }

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
