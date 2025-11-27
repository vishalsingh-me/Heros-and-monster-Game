# Legends: Monsters and Heroes

Console, turn-based RPG built for an OOP assignment. You form a small party of heroes, roam a grid map, shop in markets, and fight level-matched monsters. All stats and content load from plain text files under `Data/`.

---

## Student Information

**Name:** Vishal Singh <br/>
**BU ID:** U36704631 <br/>
**Email:** vsingh03@bu.edu

---

## What’s Included
- `src/` – All Java sources (entities, items, loaders, factories, map, market, battle, game loop).
- `Data/` – Text files for heroes, monsters, weapons, armor, potions, spells.
- `run.sh` – Helper script to compile/run (build output goes to `out/`, ignored).

---

## Requirements
- Bash shell to run `run.sh`

---

## Build & Run
```bash
# from project root
bash run.sh          # compile + run
# or split:
bash run.sh compile  # compile to out/
bash run.sh run      # run compiled classes
```

---

## Controls & States
- **Exploring:** `W/A/S/D` move, `M` map, `I` inventory view, `Q` quit (confirm).  
- **Map:** `B` or `Q` back to exploring.  
- **Market:** Auto-enter on `M` tile. Commands: `list`, `buy`, `sell`, `b` back.  
- **Inventory:** View-only; `B` back.  
- **Battle:** Numbered menu per hero:  
  1) Attack  
  2) Cast Spell  
  3) Use Potion  
  4) Equip  
  5) Skip Turn  
  Choose targets/spells by index; combat log shows damage, dodges, HP before/after.

---

## Gameplay Snapshot
- **Intro & Heroes:** Brief intro, then pick 1–3 heroes from loaded templates (Paladin/Sorcerer/Warrior).  
- **Map:** Colored grid with box-drawing borders. Tiles: `H` (hero), `M` (market), `X` (blocked), `C` (common). Start tile is forced to have at least two accessible neighbors; blocked chance is reduced.  
- **Movement:** Valid moves re-render the map and show coordinates; invalid moves print “Cannot move there.”  
- **Market:** Buy/sell with level/gold checks; sell at 50%.  
- **Inventory:** Lists each hero’s weapons/armor/potions/spells (view-only).  
- **Battles:** ~30% chance on common tiles; monsters spawn near highest hero level. Heroes act first, then monsters. Spells apply debuffs (fire→defense, ice→damage, lightning→dodge). Victories revive fainted heroes, give gold/XP, and trigger level-up checks.

---

## Data Loading
Loaders read `Data/` files into objects:
- Heroes: paladins, sorcerers, warriors.
- Monsters: dragons, exoskeletons, spirits.
- Items: weapons, armor, potions.
- Spells: fire, ice, lightning.

Factories combine loaders:
- `HeroFactory.loadAll(...)`
- `MonsterFactory.loadAll(...)` (spawns fallback to closest level)
- `MarketFactory.loadAll(...)` to stock the market.

---

## Key Classes (brief)
- `LegendsGame` – Main controller/state; intro, hero selection, map setup, loop.  
- `GameMap` – Grid generation, movement checks, colored render with box-drawing.  
- `Battle` – Turn actions, debuffs, damage logging, win/lose handling.  
- `Party` – Hero roster; revive after win.  
- `Market` – Buy/sell with checks.  
- `Inventory` / `Equipment` – Store items and equipped weapon/armor.  
- Entities/Items – `Hero` (+ subclasses), `Monster` (+ subclasses), `Weapon`, `Armor`, `Potion`, `Spell` (fire/ice/lightning).

---

## Key Features
- Turn-based combat with indexed menus and combat logs (damage, dodges, HP before/after).  
- Colored, box-drawn map with markets, blocked tiles, and common tiles; start tile ensures accessible neighbors.  
- Data-driven content: heroes, monsters, weapons, armor, potions, spells load from `Data/` text files.  
- Markets with buy/sell (level and gold checks), inventory listing, and basic equip/potion use in battle.  
- Simple state handling for exploration, map view, market, inventory, and battle.  
- Level-up checks after battles; revive fainted heroes on victory; fixed rewards for simplicity.  

---

## Tips
- Visit a market (`M`) early: buy a weapon/armor (and a spell for casters).  
- In battle, pick `1` Attack if you have no spells/potions; avoid repeated skips.  
- If start feels boxed in, rerun to regenerate the map (start neighbors are carved, but RNG can place you near edges).

---

## Design Patterns
- **State:** `GameState` enum with state-specific handling in `LegendsGame` (EXPLORING/MAP/MARKET/INVENTORY/BATTLE).  
- **Factory Method / Simple Factory:** `HeroFactory`, `MonsterFactory`, `MarketFactory` centralize creation from data files.  
- **Template Method:** `DataLoader` interface with concrete loaders encapsulating parsing for heroes, monsters, items, and spells.  
- **Inheritance/Polymorphism:** Shared bases (`Entity`, `Hero`, `Monster`, `Item`, `Spell`) with concrete subclasses for behaviors/types.  
- **Separation of Concerns (architectural principle):** Map, battle, market, inventory, and data loading are kept distinct for clarity and maintainability.

---

