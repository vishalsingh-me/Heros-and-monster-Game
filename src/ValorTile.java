public class ValorTile extends Tile {
    private TileType type;
    private Hero hero;       // Tracks the hero on this tile
    private Monster monster; // Tracks the monster on this tile

    public ValorTile(int row, int col, TileType type) {
        super(row, col);
        this.type = type;
    }

    @Override
    public boolean isAccessible() {
        // Both permanent walls (INACCESSIBLE) and temporary OBSTACLES block movement
        return type != TileType.INACCESSIBLE && type != TileType.OBSTACLE;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public void setHero(Hero hero) {
        this.hero = hero;
    }

    public Hero getHero() {
        return hero;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    public Monster getMonster() {
        return monster;
    }

    public boolean hasHero() {
        return hero != null;
    }

    public boolean hasMonster() {
        return monster != null;
    }

    /**
     * Returns a string description of the bonus provided by this terrain.
     * Used for UI display.
     */
    public String getBonusText() {
        // Java 8-style switch (no arrow syntax)
        switch (type) {
            case BUSH:
                return " (+10% Dex)";
            case CAVE:
                return " (+10% Agi)";
            case KOULOU:
                return " (+10% Str)";
            case NEXUS:
                return " (Nexus/Market)";
            case INACCESSIBLE:
                return " (Blocked)";
            case OBSTACLE:
                return " (Obstacle)";
            default:
                return "";
        }
    }
}
