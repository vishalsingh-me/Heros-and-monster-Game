# Legends of Valor

A console-based, turn-based RPG built in Java using object-oriented design and classic design patterns.  
You explore a randomly generated world, assemble a party of heroes, visit markets, and battle monsters in a tactical, turn-based combat system.

---

## Student Information

Name:Priya Dilip Bajaria
BU ID: U08184333

Name:Vishal Singh
BU ID:U36704631

Name: Samahitha chakkodbail Madhavaprasad
BU ID:U77295190

---

## File & Package Overview

File & Package Overview

- Project Organization
  |- Organized into logical subsystems
  |- Separates gameplay flow, domain logic, and infrastructure utilities

- Core Game Flow
  |- Handles overall execution of the game
  |- Manages state transitions and user interaction
  |- Main
  |  |- Program entry point
  |  |- Starts the game
  |- GameLauncher
  |  |- Initializes and launches the game
  |  |- Prepares game data and resources
  |  |- Starts the main game loop
  |- Game
  |- LegendsGame
  |- RpgGame
  |- ValorGame
  |  |- Central game controllers
  |  |- Initialize game data and map
  |  |- Maintain the current game state
  |  |- Delegate rendering, input handling, and updates

-----------------------------------------------------------------------------------------------------------------

- Key Classes (by package)

- Core Game Classes
  |- Main
  |  |- Entry point of the application
  |  |- Initializes and launches the game
  |  |- Calls the main game controller to start execution
  |  |- Responsible only for bootstrapping the program

  |- GameLauncher
  |  |- Handles initial setup and configuration
  |  |- Prepares game data and resources
  |  |- Starts the main game loop

  |- LegendsGame
  |  |- Central controller of the game
  |  |- Maintains the current GameState
  |  |  |- EXPLORING
  |  |  |- MAP
  |  |  |- MARKET
  |  |  |- INVENTORY
  |  |  |- BATTLE
  |  |- Coordinates interactions between map, party, market, and battle systems
  |  |- Primary dispatcher for player input and state transitions

  |- Game / RpgGame / ValorGame
  |  |- Organize shared game-loop functionality
  |  |- Provide reusable structure for game variants
  |  |- Ensure consistent exploration, combat, and UI flow

-----------------------------------------------------------------------------------------------------------------

- Characters (Heroes & Monsters)

  |- Entity
  |  |- Base class for all combat-capable objects
  |  |- Defines HP, level, and combat stats
  |  |- Enables polymorphic combat behavior

  |- Hero (abstract)
  |  |- Base class for playable heroes
  |  |- Stores HP, MP, strength, dexterity, agility
  |  |- Manages leveling, gold, inventory, and equipment
  |  |- Provides attack, dodge, and item-usage helpers

  |- Warrior
  |  |- Strength-focused melee hero
  |- Paladin
  |  |- Balanced defensive hero
  |- Sorcerer
  |  |- Magic-oriented hero with high spell efficiency

  |- Monster
  |  |- Base enemy class
  |  |- Contains HP, damage, defense, dodge chance, and level
  |  |- Difficulty scales with party level

  |- Dragon
  |- Spirit
  |  |- Specialized monster types with unique combat behavior

  |- Party
  |  |- Represents the player’s hero team
  |  |- Maintains hero list and map position
  |  |- Utility methods
  |  |  |- getAliveHeroes()
  |  |  |- allDead()
  |  |  |- moveTo(...)
  |  |  |- printStats()

  |- Attackable (interface)
  |  |- Contract for any entity participating in combat

-----------------------------------------------------------------------------------------------------------------

- Combat System

  |- Battle
  |  |- Manages turn-based combat
  |  |- Controls hero and monster turn order
  |  |- Supported actions
  |  |  |- Attack
  |  |  |- Cast spell
  |  |  |- Use potion
  |  |  |- Change equipment
  |  |  |- Flee
  |  |- Applies damage calculations and dodge chance
  |  |- Applies spell effects
  |  |- Handles victory rewards and defeat conditions

-----------------------------------------------------------------------------------------------------------------

- Items & Inventory

  |- Item (interface)
  |  |- getName()
  |  |- getPrice()
  |  |- getRequiredLevel()

  |- Equipment
  |  |- Base class for equipable items

  |- Weapon
  |  |- Increases hero attack damage
  |  |- Tracks damage and hands required

  |- Armor
  |- Exoskeleton
  |  |- Reduce incoming damage

  |- Potion
  |  |- Consumable item
  |  |- Restores or boosts hero attributes

  |- Spell
  |  |- Represents magical attacks
  |  |- Includes damage, mana cost, and spell type
  |  |- Applies debuffs during battle

  |- Inventory
  |  |- Stores a hero’s items
  |  |- Add and remove items
  |  |- Displays formatted inventory tables

  |- Market
  |  |- Handles buying and selling
  |  |- Supports weapons, armor, potions, and spells
  |  |- Enforces gold and level requirements

  |- Purchasable (interface)
  |  |- Marks items tradable in the market

-----------------------------------------------------------------------------------------------------------------

- World & Map

  |- GameMap
  |- ValorMap
  |  |- Represent the grid-based world
  |  |- Store tiles
  |  |- Validate movement
  |  |- Render the map to the console

  |- Tile (abstract)
  |  |- Determines accessibility and display symbol

  |- TileType (enum)
  |  |- Defines tile behavior categories

  |- Lane
  |  |- Primary movement tile
  |- CommonTile
  |  |- Standard accessible tile
  |- InaccessibleTile
  |  |- Blocks movement
  |- ValorTile
  |  |- Special-purpose tile for Valor map

-----------------------------------------------------------------------------------------------------------------

- Data Loading & Factories

  |- DataLoader
  |  |- Centralized data-loading component
  |  |- Reads hero, monster, item, and spell data
  |  |- Constructs Java objects at runtime

  |- Loader Classes
  |  |- Heroes
  |  |  |- PaladinLoader
  |  |  |- SorcererLoader
  |  |  |- WarriorLoader
  |  |- Monsters
  |  |  |- DragonLoader
  |  |  |- SpiritLoader
  |  |- Equipment
  |  |  |- WeaponLoader
  |  |  |- ArmorLoader
  |  |  |- ExoskeletonLoader
  |  |- Consumables
  |  |  |- PotionLoader
  |  |- Spells
  |  |  |- FireSpellLoader
  |  |  |- IceSpellLoader
  |  |  |- LightningSpellLoader

  |- HeroFactory
  |- MonsterFactory
  |  |- Encapsulate object creation logic
  |  |- Simplify scaling and difficulty balancing
  |  |- Prevent duplication of construction logic

-----------------------------------------------------------------------------------------------------------------

- UI Utilities

  |- UiColors
  |  |- Centralized ANSI color definitions
  |  |- Ensures consistent console output
  |  |- Separates UI styling from game logic

--------------------------------------------------------------------------------------------------------------------
# I/O Example

======================================
              GAME HUB
======================================
1. Legends: Monsters and Heroes
2. Legends of Valor
3. Quit
Select game: 2

Select hero 1 of 3
 0) Gaerdal_Ironhand   (Lvl 1)
 1) Sehanine_Monnbow   (Lvl 1)
 2) Muamman_Duathall   (Lvl 1)
 3) Flandal_Steelskin  (Lvl 1)
 4) Undefeated_Yoj     (Lvl 1)
 5) Eunoia_Cyn         (Lvl 1)
 6) Rillifane_Rallathil (Lvl 1)
 7) Segojan_Earthcaller (Lvl 1)
 8) Reign_Havoc        (Lvl 1)
 9) Reverie_Ashels     (Lvl 1)
10) Kalabar            (Lvl 1)
11) Skye_Soar          (Lvl 1)
12) Parzival           (Lvl 1)
13) Sehanine_Moonbow   (Lvl 1)
14) Skoraeus_Stonebones (Lvl 1)
15) Garl_Glittergold   (Lvl 1)
16) Amaryllis_Astra    (Lvl 1)
17) Caliber_Heist      (Lvl 1)
Index (or 'q' to cancel): 1
Sehanine_Monnbow added.

Select hero 2 of 3
 0) Gaerdal_Ironhand   (Lvl 1)
 1) Sehanine_Monnbow   (Lvl 1)
 2) Muamman_Duathall   (Lvl 1)
 3) Flandal_Steelskin  (Lvl 1)
 4) Undefeated_Yoj     (Lvl 1)
 5) Eunoia_Cyn         (Lvl 1)
 6) Rillifane_Rallathil (Lvl 1)
 7) Segojan_Earthcaller (Lvl 1)
 8) Reign_Havoc        (Lvl 1)
 9) Reverie_Ashels     (Lvl 1)
10) Kalabar            (Lvl 1)
11) Skye_Soar          (Lvl 1)
12) Parzival           (Lvl 1)
13) Sehanine_Moonbow   (Lvl 1)
14) Skoraeus_Stonebones (Lvl 1)
15) Garl_Glittergold   (Lvl 1)
16) Amaryllis_Astra    (Lvl 1)
17) Caliber_Heist      (Lvl 1)
Index (or 'q' to cancel): 4
Undefeated_Yoj added.

Select hero 3 of 3
 0) Gaerdal_Ironhand   (Lvl 1)
 1) Sehanine_Monnbow   (Lvl 1)
 2) Muamman_Duathall   (Lvl 1)
 3) Flandal_Steelskin  (Lvl 1)
 4) Undefeated_Yoj     (Lvl 1)
 5) Eunoia_Cyn         (Lvl 1)
 6) Rillifane_Rallathil (Lvl 1)
 7) Segojan_Earthcaller (Lvl 1)
 8) Reign_Havoc        (Lvl 1)
 9) Reverie_Ashels     (Lvl 1)
10) Kalabar            (Lvl 1)
11) Skye_Soar          (Lvl 1)
12) Parzival           (Lvl 1)
13) Sehanine_Moonbow   (Lvl 1)
14) Skoraeus_Stonebones (Lvl 1)
15) Garl_Glittergold   (Lvl 1)
16) Amaryllis_Astra    (Lvl 1)
17) Caliber_Heist      (Lvl 1)
Index (or 'q' to cancel): 6
Rillifane_Rallathil added.
Welcome to Legends of Valor!

Three lanes connect the Hero Nexus (bottom) to the Monster Nexus (top).
Heroes push north, Monsters push south.
Reach the enemy Nexus to win. Don't let monsters reach yours!
Use W/A/S/D to move, F for physical attack, C to cast spells.
Teleport (T) only from your Nexus to an ally in another lane.
Recall (R) to return to your spawn Nexus tile.

A wild Natsunomeryu appeared in TOP lane!
A wild BigBad-Wolf appeared in MID lane!
A wild Casper appeared in BOT lane!

========== ROUND 1 ==========
           0        1        2        3        4        5        6        7
 0 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 0 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 0 |       ||   M   ||XXXXXXX||       ||   M   ||XXXXXXX||       ||   M   |
 0 |   N   ||   N   ||XXXXXXX||   N   ||   N   ||XXXXXXX||   N   ||   N   |
 0 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 0 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 1 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 1 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 1 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 1 |   C   ||       ||XXXXXXX||   C   ||   B   ||XXXXXXX||   C   ||   B   |
 1 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 1 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 2 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 2 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 2 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 2 |   K   ||   K   ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 2 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 2 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 3 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 3 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 3 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 3 |   B   ||   B   ||XXXXXXX||   C   ||   K   ||XXXXXXX||   K   ||       |
 3 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 3 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 4 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 4 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 4 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 4 |       ||   B   ||XXXXXXX||   X   ||   B   ||XXXXXXX||   B   ||   C   |
 4 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 4 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 5 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 5 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 5 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 5 |   C   ||       ||XXXXXXX||       ||   K   ||XXXXXXX||   K   ||       |
 5 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 5 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 6 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 6 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 6 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 6 |   C   ||   K   ||XXXXXXX||   B   ||   C   ||XXXXXXX||       ||   K   |
 6 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 6 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 7 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
 7 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 7 |   H   ||       ||XXXXXXX||   H   ||       ||XXXXXXX||   H   ||       |
 7 |   N   ||   N   ||XXXXXXX||   N   ||   N   ||XXXXXXX||   N   ||   N   |
 7 |       ||       ||XXXXXXX||       ||       ||XXXXXXX||       ||       |
 7 +-------++-------++XXXXXXX++-------++-------++XXXXXXX++-------++-------+
           0        1        2        3        4        5        6        7
Legend: H=Hero M=Monster N=Nexus I=Inaccessible X=Obstacle B=Bush C=Cave K=Koulou

=============================
==  ROUND 1 ? Hero Turn ==
=============================
Hero: Sehanine_Monnbow  Pos: (7,0)  Lane: TOP  (Nexus/Market)
HP: 100/100  MP: 600/600  Gold: 2500  Lvl: 1

Actions:
 [W] Move Up       [A] Move Left      [S] Move Down      [D] Move Right
 [F] Physical Attack   [C] Cast Spell   [P] Use Potion   [E] Equip
 [T] Teleport          [R] Recall       [I] Hero Info    [H] Party
 [V] View Map          [M] Market       [?] Help         [Q] Quit

--- MARKET ---
Hero: Sehanine_Monnbow  Level: 1  HP: 100/100  MP: 600/600  Gold: 2500
Type 'I' at any time to view hero stats.
1) Buy Potion
2) Buy Spell
3) Buy Armor/Weapon
4) Sell Item
5) View Hero Info
6) Exit
>

Choose action for Gaerdal_Ironhand:
```
Design Patterns Used:

- State Pattern
  |- GameState enum + logic inside LegendsGame (EXPLORING, MAP, MARKET, INVENTORY, BATTLE)
  |- LegendsGame maintains the current game state
  |- Renders the correct screen based on state
  |- Handles user input appropriately
  |- Transitions between states (exploration -> battle -> market)
  |- Each state has clearly separated behavior
  |- Easy to add new states (Pause, Settings) without modifying the core loop

- Factory / Factory Method Pattern
  |- HeroFactory centralizes creation of hero objects
  |- Abstracts hero instantiation away from game logic
  |- MonsterFactory generates monsters based on party level and size
  |- PaladinLoader loads Paladin heroes
  |- SorcererLoader loads Sorcerer heroes
  |- WarriorLoader loads Warrior heroes
  |- DragonLoader loads Dragon monsters
  |- SpiritLoader loads Spirit monsters
  |- WeaponLoader loads weapons
  |- ArmorLoader loads armor
  |- ExoskeletonLoader loads exoskeletons
  |- PotionLoader loads potions
  |- FireSpellLoader loads fire spells
  |- IceSpellLoader loads ice spells
  |- LightningSpellLoader loads lightning spells
  |- DataLoader reads text files and constructs objects at startup
  |- Removes hard-coded game data

- Strategy-like Behavior via Interfaces and Composition
  |- Item interface implemented by Weapon, Armor, Potion, and Spell
  |- Market, Inventory, and Battle treat items polymorphically
  |- Attackable interface shared by heroes and monsters
  |- SpellType enum defines Fire, Ice, and Lightning behavior
  |- Spell effects applied dynamically without large if-else chains

- Encapsulation & Information Hiding
  |- Party exposes only movement and hero queries
  |- Market exposes buy and sell operations
  |- GameMap controls movement validation and rendering
  |- Internal implementation details hidden behind clean APIs
  |- Reduced coupling and safer future changes

Object Design Qualities

- Scalability
  |- Clear separation between major subsystems
  |- World
  |  |- GameMap
  |  |- ValorMap
  |  |- Tile
  |  |- Lane
  |  |- CommonTile
  |  |- InaccessibleTile
  |  |- ValorTile
  |- Characters
  |  |- Hero
  |  |- Warrior
  |  |- Paladin
  |  |- Sorcerer
  |  |- Monster
  |  |- Dragon
  |  |- Spirit
  |  |- Party
  |- Items
  |  |- Item hierarchy
  |  |  |- Weapon
  |  |  |- Armor
  |  |  |- Potion
  |  |  |- Spell
  |- Game Flow
  |  |- LegendsGame
  |  |- GameState
  |- To extend the game
  |  |- Add a new hero -> subclass Hero + loader
  |  |- Add a new monster -> subclass Monster + loader
  |  |- Add a new tile -> extend Tile
  |  |- Add a new item -> implement Item

- Extendibility
  |- GameState enum makes adding new modes easy
  |- Enums (SpellType) centralize behavior changes
  |- Market works entirely on the Item interface
  |- UiColors centralizes all console styling
  |- Minimal changes required to introduce new features

- Implementation Qualities
  |- Usability
  |  |- Consistent key bindings
  |  |  |- W / A / S / D -> Movement
  |  |  |- I -> Inventory
  |  |  |- M -> Map
  |  |  |- Q -> Quit / Flee
  |  |- Clear, boxed console layouts
  |  |- Color-coded output improves readability
  |  |- Hero selection screen clearly shows stats and limits (1–3 heroes)

  |- Readability
  |  |- Classes follow Single Responsibility Principle
  |  |- Methods are short and descriptive
  |  |- Combat, market, inventory, and exploration logic clearly separated
  |  |- Meaningful method names reduce need for excessive comments

  |- Best Practices
  |  |- Interfaces and enums prevent hard-coded strings
  |  |- Centralized data loading via DataLoader
  |  |- UI formatting isolated in UiColors and map-printing methods
  |  |- Robust input validation prevents crashes
  |  |- Game state resets cleanly on each run

- Key Highlights & Features
  |- Grid-Based World Map
  |  |- Accessible and inaccessible tiles
  |  |- Structured lanes and regions
  |- Party-Based Gameplay
  |  |- Choose 1–3 heroes
  |  |- Heroes act independently but share progression goals
  |- Turn-Based Combat
  |  |- Sequential hero turns
  |  |- Monsters act afterward
  |  |- Attacks, spells, potions, and equipment changes supported
  |  |- Dodge and spell-type debuffs implemented
  |- Market & Inventory System
  |  |- Buy and sell items
  |  |- Level requirements enforced
  |  |- Per-hero gold and inventory
  |- Regeneration & Revival
  |  |- Heroes regenerate HP/MP between rounds
  |  |- Fallen heroes revive after successful battles
  |  |- XP and gold scale with encounters
  |- File-Driven Data
  |  |- Heroes, monsters, items, and spells loaded from .txt files
  |  |- Easy balancing without code changes
  |- Styled Console UI
  |  |- ANSI colors
  |  |- Unicode borders
  |  |- Clean, readable layouts

- DESIGN.md – UML Overview
  |- High-Level Architecture
  |  |- Game Flow Layer
  |  |  |- Main
  |  |  |- GameLauncher
  |  |  |- LegendsGame
  |  |- Domain Layer
  |  |  |- Characters (Hero, Monster, Party)
  |  |  |- Items (Item hierarchy)
  |  |  |- World (GameMap, Tile hierarchy)
  |  |  |- Market
  |  |- Infrastructure Layer
  |  |  |- Data loading (DataLoader, loaders)
  |  |  |- UI utilities (UiColors)
  |  |- State pattern controls gameplay flow
  |  |- Factories and loaders handle object creation

- Core Class Diagram (Textual Overview)
  |- Game Flow
  |  |- LegendsGame
  |  |  |- state: GameState
  |  |  |- map: GameMap
  |  |  |- party: Party
  |  |  |- market: Market
  |  |  |- run(), setState(), getters
  |  |- Main
  |  |  |- main(): starts the game

  |- Characters
  |  |- Hero (abstract)
  |  |  |- name, level, HP, MP, stats, gold
  |  |  |- inventory
  |  |  |- combat helpers
  |  |- Warrior, Paladin, Sorcerer extend Hero
  |  |- Monster
  |  |  |- name, level, HP, damage, defense, dodge
  |  |- Dragon, Spirit extend Monster
  |  |- Party
  |  |  |- heroes
  |  |  |- position

  |- Items
  |  |- Item (interface)
  |  |- Weapon
  |  |- Armor
  |  |- Exoskeleton
  |  |- Potion
  |  |- Spell

  |- World
  |  |- GameMap
  |  |- ValorMap
  |  |- Tile (abstract)
  |  |- Lane
  |  |- CommonTile
  |  |- InaccessibleTile
  |  |- ValorTile

  |- Data & UI
  |  |- DataLoader
  |  |- Loader classes
  |  |- HeroFactory
  |  |- MonsterFactory
  |  |- UiColors


