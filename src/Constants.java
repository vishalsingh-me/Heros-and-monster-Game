/**
 * Centralized text and color constants for shared UI usage.
 * Scope: GameLauncher, LegendsGame (current batch).
 */
class GameText {
    private GameText() {}

    // ANSI Colors
    public static final String RESET = "\u001B[0m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RED = "\u001B[31m";
    public static final String BLUE = "\u001B[34m";
    public static final String GRAY = "\u001B[90m";

    // Game Hub
    public static final String HUB_BORDER = "======================================";
    public static final String HUB_TITLE = "GAME HUB";
    public static final String MENU_OPTION_1 = "1. Legends: Monsters and Heroes";
    public static final String MENU_OPTION_2 = "2. Legends of Valor";
    public static final String MENU_OPTION_3 = "3. Quit";
    public static final String MENU_PROMPT = "Select game: ";
    public static final String MENU_EXIT_MESSAGE = "Exiting game. Goodbye!";
    public static final String MENU_INVALID_CHOICE = "Invalid choice. Please enter 1, 2, or 3.";

    // Legends of Valor loading
    public static final String ERROR_LOADING_LOV = "Error loading Legends of Valor data: ";
    public static final String RETURNING_TO_HUB = "Returning to game hub.";

    // Hero selection (Legends of Valor)
    public static final String HERO_SELECT_PROMPT_PREFIX = "Select hero ";
    public static final String HERO_SELECT_OF = " of 3";
    public static final String HERO_INDEX_PROMPT = "Index (or 'q' to cancel): ";
    public static final String HERO_INVALID_INDEX = "Invalid index.";
    public static final String HERO_ALREADY_IN_PARTY = "That hero is already in your party.";
    public static final String HERO_ADDED_SUFFIX = " added.";
    public static final String HERO_INPUT_NUMBER = "Please enter a valid number.";

    // LegendsGame intro
    public static final String LEGENDS_BORDER = "====================================";
    public static final String LEGENDS_TITLE = "   LEGENDS: MONSTERS AND HEROES";
    public static final String LEGENDS_WELCOME = "Welcome! Build a team of heroes, explore the map, visit markets, and battle monsters.";
    public static final String LEGENDS_HOW_TO_PLAY = "How to play:";
    public static final String LEGENDS_MOVE = " - Move: W/A/S/D";
    public static final String LEGENDS_MAP = " - Map:  M (view map)";
    public static final String LEGENDS_INVENTORY = " - Inventory: I (view what you carry)";
    public static final String LEGENDS_MARKET = " - Market: Step on M tiles to shop (list, buy, sell, b to exit)";
    public static final String LEGENDS_BATTLES = " - Battles: Choose actions (Attack/Spell/Potion/Equip/Skip), then target by index";
    public static final String LEGENDS_QUIT = " - Quit: Q (with confirmation)";
    public static final String LEGENDS_PRESS_ENTER = "Press Enter to continue...";

    // LegendsGame hero selection
    public static final String LEGENDS_CHOOSE_HEROES = "Choose 1-3 heroes by index:";
    public static final String LEGENDS_ENTER_INDEX = "Enter index (or blank to finish): ";
    public static final String LEGENDS_INVALID_INDEX = "Invalid index.";
    public static final String LEGENDS_ENTER_NUMBER = "Please enter a number.";
    public static final String LEGENDS_DEFAULT_HERO = "No heroes chosen; defaulting to first hero.";

    // LegendsGame loop prompts
    public static final String LEGENDS_GAME_START = "Game start! Use W/A/S/D to move, Q to quit, M to view map.";
    public static final String LEGENDS_MOVE_AGAIN = "Use W/A/S/D to move around the world.";
    public static final String LEGENDS_PROMPT = "> ";
    public static final String LEGENDS_COMMANDS = "Commands: W/A/S/D move, M map, I inventory, Q quit";
    public static final String LEGENDS_MAP_VIEW = "Map view. Press B to return to exploring.";
    public static final String LEGENDS_INVENTORY_OPENED = "Inventory opened. Press B to go back.";
    public static final String LEGENDS_GOODBYE = "Goodbye!";

    // LegendsGame movement and market
    public static final String LEGENDS_CANNOT_MOVE = "Cannot move there.";
    public static final String LEGENDS_MOVED_TO = "Moved to (%d, %d).%n";
    public static final String LEGENDS_ENTERED_MARKET = "Entered Market.";
    public static final String LEGENDS_MARKET_COMMANDS = "Entered Market. Commands: list, buy, sell, b (back)";
    public static final String LEGENDS_MARKET_PROMPT = "Market> ";
    public static final String LEGENDS_MARKET_LIST_CMDS = "Commands: list, buy, sell, b";
    public static final String LEGENDS_MARKET_BUY_WHICH = "Buy which category? weapon/armor/potion/spell";
    public static final String LEGENDS_MARKET_UNKNOWN_CAT = "Unknown category.";
    public static final String LEGENDS_MARKET_INDEX_BUY = "Index to buy: ";
    public static final String LEGENDS_PURCHASED = "Purchased %s";
    public static final String LEGENDS_CANNOT_BUY = "Cannot buy (level/gold).";
    public static final String LEGENDS_MARKET_INDEX_SELL = "Index to sell: ";
    public static final String LEGENDS_SOLD = "Sold %s";
    public static final String LEGENDS_CANNOT_SELL = "Cannot sell.";
    public static final String LEGENDS_MARKET_COMMANDS_SHORT = "Commands: list, buy, sell, b";
    public static final String LEGENDS_MARKET_WEAPONS = "Weapons:";
    public static final String LEGENDS_MARKET_ARMORS = "Armors:";
    public static final String LEGENDS_MARKET_POTIONS = "Potions:";
    public static final String LEGENDS_MARKET_SPELLS = "Spells:";
    public static final String LEGENDS_ENTERED_MARKET_TILE = "Entered Market.";

    // LegendsGame inventory and battle
    public static final String LEGENDS_INVENTORY_TITLE = "=== INVENTORY ===";
    public static final String LEGENDS_NONE = "    (none)";
    public static final String LEGENDS_BATTLE_BEGINS = "A battle begins!";
    public static final String LEGENDS_NO_MONSTERS = "No monsters could be found to match your level. You feel a strange calm...";
    public static final String LEGENDS_PARTY_DEFEATED = "Party defeated. Game over.";
    public static final String LEGENDS_VICTORY = "Victory! Earned gold and experience.";
    public static final String LEGENDS_NO_SPELLS = "No spells.";
    public static final String LEGENDS_NO_POTIONS = "No potions.";
    public static final String LEGENDS_NO_EQUIPMENT = "No equipment available.";
    public static final String LEGENDS_NO_MONSTERS_RANGE = "No monsters in range.";
    public static final String LEGENDS_INVALID_TARGET = "Invalid target. Try again.";
    public static final String LEGENDS_INVALID_SPELL = "Invalid spell. Try again.";
    public static final String LEGENDS_INVALID_POTION = "Invalid potion. Try again.";
    public static final String LEGENDS_NO_POTION = "No potions.";
    public static final String LEGENDS_NO_SPELL_INVENTORY = "No spells.";
    public static final String LEGENDS_TURN_SKIPPED = "Turn skipped.";
    public static final String LEGENDS_INVALID_CHOICE = "Invalid choice. Please enter a number 1-5.";
    public static final String LEGENDS_NOT_ENOUGH_MANA = "Not enough mana.";
    public static final String LEGENDS_NO_TARGETS = "No targets in range.";
    public static final String LEGENDS_NO_MONSTERS_IN_RANGE = "No monsters in range.";
    public static final String LEGENDS_NO_EQUIP = "No equipment available.";
    public static final String LEGENDS_CHOOSE_TARGET = "Choose target:";
    public static final String LEGENDS_CHOOSE_SPELL = "Choose spell:";
    public static final String LEGENDS_CHOOSE_POTION = "Choose potion:";
    public static final String LEGENDS_EQUIP_MENU = "Equip menu:";
    public static final String LEGENDS_WEAPON_INDEX = "Weapon index (blank to skip): ";
    public static final String LEGENDS_ARMOR_INDEX = "Armor index (blank to skip): ";

    public static final String LEGENDS_ENTER_OBSTACLE_REMOVE = "Path blocked by Obstacle. Destroy it? (y/n): ";
    public static final String LEGENDS_OBSTACLE_REMOVED = "Obstacle removed! (Turn consumed)";
    public static final String LEGENDS_PATH_BLOCKED = "Path blocked.";
    public static final String LEGENDS_TILE_OCCUPIED = "Tile occupied by another hero.";
    public static final String LEGENDS_CANNOT_MOVE_PAST_MONSTER = "Cannot move past a monster in this lane without killing it!";

    // ==============================================================
    // VALORGAME STRINGS
    // ==============================================================
    public static final String VALOR_WELCOME = "Welcome to Legends of Valor!";
    public static final String VALOR_GAME_OVER = "Game Over!";
    public static final String VALOR_ROUND_HEADER = "========== ROUND %d ==========";
    public static final String VALOR_QUIT_TO_HUB_PROMPT = "Quit to game hub? (y/N): ";
    public static final String VALOR_THREE_HEROES_REQUIRED = "Legends of Valor requires exactly 3 heroes.";
    public static final String VALOR_MONSTER_DATA_WARNING = "Warning: Could not load monster data: ";
    public static final String VALOR_QUIT_REASON = "You chose to quit the game.";
    public static final String VALOR_EXITED_TO_MENU = "Exited to main menu.";
    public static final String VALOR_END_REASON_QUIT = "You quit the battle.";
    public static final String VALOR_END_REASON_DONE = "Battle finished.";
    public static final String VALOR_INTRO_LINE1 = "Three lanes connect the Hero Nexus (bottom) to the Monster Nexus (top).";
    public static final String VALOR_INTRO_LINE2 = "Heroes push north, Monsters push south.";
    public static final String VALOR_INTRO_LINE3 = "Reach the enemy Nexus to win. Don't let monsters reach yours!";
    public static final String VALOR_INTRO_LINE4 = "Use W/A/S/D to move, F for physical attack, C to cast spells.";
    public static final String VALOR_INTRO_LINE5 = "Teleport (T) only from your Nexus to an ally in another lane.";
    public static final String VALOR_INTRO_LINE6 = "Recall (R) to return to your spawn Nexus tile.";

    public static final String VALOR_PROMPT = "> ";
    public static final String VALOR_NEED_COMMAND = "Please enter a command. Type '?' for help.";
    public static final String VALOR_INVALID_INPUT = "Invalid input. Type '?' for help.";

    public static final String VALOR_CANNOT_OUT_OF_BOUNDS = "Cannot move out of bounds.";
    public static final String VALOR_OBSTACLE_PROMPT = "Path blocked by Obstacle. Destroy it? (y/n): ";
    public static final String VALOR_OBSTACLE_REMOVED = "Obstacle removed! (Turn consumed)";
    public static final String VALOR_PATH_BLOCKED = "Path blocked.";
    public static final String VALOR_TILE_OCCUPIED = "Tile occupied by another hero.";
    public static final String VALOR_BLOCKED_BY_MONSTER = "Cannot move past a monster in this lane without killing it!";

    public static final String VALOR_NO_MONSTERS_IN_RANGE = "No monsters in range.";
    public static final String VALOR_CHOOSE_TARGET = "Choose target:";
    public static final String VALOR_DEALT_DAMAGE = "Dealt %d damage.";

    public static final String VALOR_NO_SPELLS = "No spells in inventory.";
    public static final String VALOR_CHOOSE_SPELL = "Choose Spell:";
    public static final String VALOR_CHOOSE_SPELL_INDEX = "Choose spell index: ";
    public static final String VALOR_INVALID_SELECTION = "Invalid selection.";
    public static final String VALOR_INVALID_INPUT_GENERIC = "Invalid input.";
    public static final String VALOR_NOT_ENOUGH_MANA = "Not enough mana.";
    public static final String VALOR_NO_TARGETS = "No targets in range.";
    public static final String VALOR_NO_HERO_TILE = "Hero not on map.";

    public static final String VALOR_NO_VALID_TELEPORT = "No valid heroes in other lanes.";
    public static final String VALOR_CHOOSE_HERO_TELEPORT = "Choose target hero to teleport to:";
    public static final String VALOR_INVALID_CHOICE = "Invalid choice.";
    public static final String VALOR_ENTER_CHOICE = "Enter choice: ";
    public static final String VALOR_TELEPORTED = "Teleported!";
    public static final String VALOR_NO_SAFE_SPACE = "No safe space around target hero.";
    public static final String VALOR_SPAWN_BLOCKED = "Spawn point blocked!";
    public static final String VALOR_RECALLED_TO_NEXUS = "%s recalled to Nexus.";

    public static final String VALOR_NO_POTIONS = "No potions.";
    public static final String VALOR_USED_POTION = "Used %s";
    public static final String VALOR_NO_ITEMS_AVAILABLE = "No items available.";
    public static final String VALOR_BUY_INDEX_PROMPT = "Buy index (or -1 to cancel): ";
    public static final String VALOR_MARKET_INVALID_SIMPLE = "Invalid. Enter 1-6 or I for hero info.";

    public static final String VALOR_MARKET_NEED_NEXUS = "You must be on a Nexus tile to visit a market.";
    public static final String VALOR_MARKET_MENU = "--- MARKET ---";
    public static final String VALOR_MARKET_EMPTY = "No items available.";
    public static final String VALOR_MARKET_BUY_PROMPT = "Buy index (or -1 to cancel): ";
    public static final String VALOR_MARKET_INVALID_INDEX = "Invalid index.";
    public static final String VALOR_MARKET_CANNOT_AFFORD = "Cannot afford or level too low.";
    public static final String VALOR_MARKET_INVALID_INPUT = "Invalid input.";

    public static final String VALOR_MONSTER_APPEARED = "A wild %s appeared in %s lane!";
    public static final String VALOR_MONSTERS_TURN = "--- Monsters Turn ---";
    public static final String VALOR_NO_VALID_HEROES_OTHER_LANES = "No valid heroes in other lanes.";
    public static final String VALOR_MONSTER_FLESH_HIT = "Hit for %d damage!";
    public static final String VALOR_MONSTER_FAINTED = "*** %s has fainted! ***";
    public static final String VALOR_RESPAWN = "%s respawned at Nexus.";
    public static final String VALOR_RESPAWN_ALT = "%s respawned at Nexus (alternate spot).";
    public static final String VALOR_RESPAWN_FULL = "Nexus is full! %s must wait for space.";
    public static final String VALOR_RECENT_EVENTS = "Recent events:";
    public static final String VALOR_MONSTER_DIED = "%s died!";
    public static final String VALOR_HEROES_WIN = "HEROES WIN! The Monster Nexus has been destroyed!";
    public static final String VALOR_MONSTERS_WIN = "MONSTERS WIN! The Hero Nexus has been overrun!";
    public static final String VALOR_END_REASON_HERO = "Heroes reached the Monster Nexus.";
    public static final String VALOR_END_REASON_MONSTER = "Monsters reached your Nexus.";

    public static final String VALOR_MONSTER_ATTACKS = "%s attacks %s";
    public static final String VALOR_MONSTER_MOVED_FORWARD = "%s moved forward.";
    public static final String VALOR_IS_REVIVING = "%s is reviving...";

    public static final String VALOR_ATTACKS = "%s attacks %s!";
    public static final String VALOR_CASTS = "%s casts %s on %s for %d damage!";
    public static final String VALOR_DEFENSE_REDUCED = "%s's defense reduced by %d";
    public static final String VALOR_DAMAGE_REDUCED = "%s's damage reduced by %d";
    public static final String VALOR_DODGE_REDUCED = "%s's dodge chance reduced by %s";
    public static final String VALOR_MOVE_TO = "%s moved to (%d, %d)";

    public static final String VALOR_MARKET_HERO_STATUS = "Hero: %s  Level: %d  HP: %d/%d  MP: %d/%d  Gold: %d";
    public static final String VALOR_MARKET_INFO_HINT = "Type 'I' at any time to view hero stats.";
    public static final String VALOR_MARKET_MENU_1 = "1) Buy Potion";
    public static final String VALOR_MARKET_MENU_2 = "2) Buy Spell";
    public static final String VALOR_MARKET_MENU_3 = "3) Buy Armor/Weapon";
    public static final String VALOR_MARKET_MENU_4 = "4) Sell Item";
    public static final String VALOR_MARKET_MENU_5 = "5) View Hero Info";
    public static final String VALOR_MARKET_MENU_6 = "6) Exit";
    public static final String VALOR_MARKET_WEAPONS_OR_ARMORS = "Weapons or Armors? (W/A)";
    public static final String VALOR_INVENTORY_EMPTY = "Inventory empty.";
    public static final String VALOR_SOLD_ITEM = "Sold %s";
    public static final String VALOR_BOUGHT = "Bought!";
    public static final String VALOR_CANNOT_AFFORD_LEVEL = "Cannot afford or level too low.";
    public static final String VALOR_INVALID_INDEX_GENERIC = "Invalid index.";

    public static final String VALOR_EQUIP_MENU_HEADER = "--- EQUIP MENU ---";
    public static final String VALOR_EQUIP_WEAPON = "1) Equip Weapon";
    public static final String VALOR_EQUIP_ARMOR = "2) Equip Armor";
    public static final String VALOR_EQUIP_CANCEL = "3) Cancel";
    public static final String VALOR_NO_WEAPONS = "No weapons.";
    public static final String VALOR_NO_ARMOR = "No armor.";
    public static final String VALOR_INDEX_PROMPT = "Index: ";
    public static final String VALOR_EQUIPPED_WEAPON = "Equipped %s";
    public static final String VALOR_EQUIPPED_ARMOR = "Equipped %s";

    public static final String VALOR_PARTY_OVERVIEW_TITLE = "=== Party Overview ===";
    public static final String VALOR_PARTY_OVERVIEW_DIVIDER = "----------------------------------------------------------------";
    public static final String VALOR_HERO_SHEET_TITLE = "=== Hero Sheet: %s ===";
    public static final String VALOR_HELP_TITLE = "=== Help ===";
    public static final String VALOR_HELP_MOVE = "Movement: W/A/S/D to move within your lane.";
    public static final String VALOR_HELP_MOVE_DETAILS = "  - Heroes move north (toward the Monster Nexus).";
    public static final String VALOR_HELP_BLOCKS = "  - You cannot pass through Monsters or Inaccessible tiles.";
    public static final String VALOR_HELP_TELEPORT = "Teleport (T): From a Nexus tile, move to an ally's tile in another lane.";
    public static final String VALOR_HELP_RECALL = "Recall   (R): Return to your spawn Nexus.";
    public static final String VALOR_HELP_TERRAIN = "Terrain bonuses:";
    public static final String VALOR_HELP_BUSH = "  BUSH   (B): +10% Dexterity while standing on it.";
    public static final String VALOR_HELP_CAVE = "  CAVE   (C): +10% Agility while standing on it.";
    public static final String VALOR_HELP_KOULOU = "  KOULOU (K): +10% Strength while standing on it.";
    public static final String VALOR_HELP_OTHER = "Other commands:";
    public static final String VALOR_HELP_COMMANDS_LINE1 = "  F: Physical attack   C: Cast spell   P: Use potion   E: Equip";
    public static final String VALOR_HELP_COMMANDS_LINE2 = "  I: Show detailed hero stats";
    public static final String VALOR_HELP_COMMANDS_LINE3 = "  H: Show party overview";
    public static final String VALOR_HELP_COMMANDS_LINE4 = "  V: Re-print the map";
    public static final String VALOR_HELP_COMMANDS_LINE5 = "  M: Visit Market (if on a Nexus tile)";
    public static final String VALOR_HELP_COMMANDS_LINE6 = "  Q: Quit to game hub";

    public static final String VALOR_ACTIONS_HEADER = "Actions:";
    public static final String VALOR_ACTION_LINE1 = " [W] Move Up       [A] Move Left      [S] Move Down      [D] Move Right";
    public static final String VALOR_ACTION_LINE2 = " [F] Physical Attack   [C] Cast Spell   [P] Use Potion   [E] Equip";
    public static final String VALOR_ACTION_LINE3 = " [T] Teleport          [R] Recall       [I] Hero Info    [H] Party";
    public static final String VALOR_ACTION_LINE4 = " [V] View Map          [M] Market       [?] Help         [Q] Quit";

    public static final String VALOR_ROUND_HERO_TITLE = " ROUND %d – Hero Turn ";
    public static final String VALOR_GAME_SUMMARY_TITLE = "===== Game Summary =====";
    public static final String VALOR_FINAL_ROUND = "Final round: %d";
    public static final String VALOR_GAME_THANKS = "Thank you for playing Legends of Valor!";
    public static final String VALOR_SUMMARY_DIVIDER = "----------------------------------------------------------------------------";
}
