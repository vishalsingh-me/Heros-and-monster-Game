public class ValorTile extends Tile {
    private TileType type;
    private Hero hero;       // Tracks the hero on this tile
    private Monster monster; // Tracks the monster on this tile
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String GRAY = "\u001B[90m";
    private static final int BLOCK_WIDTH = 9;  // visible characters
    private static final int BLOCK_HEIGHT = 6;

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
     * Renders this tile as a 3-line ASCII block.
     * Returns 6 fixed-width strings.
     */
    public String[] renderBlock() {
        String[] block = new String[BLOCK_HEIGHT];

        if (type == TileType.INACCESSIBLE) {
            String border = "+" + GRAY + repeat("X", BLOCK_WIDTH - 2) + RESET + "+";
            String middle = "|" + GRAY + repeat("X", BLOCK_WIDTH - 2) + RESET + "|";
            block[0] = border;
            block[1] = middle;
            block[2] = middle;
            block[3] = middle;
            block[4] = middle;
            block[5] = border;
            return block;
        }

        char terrainChar = getTerrainSymbol(type);
        String terrainColor = getTerrainColor(type);

        String unitChar = " ";
        String unitColor = "";
        if (hasHero() && hasMonster()) {
            unitChar = "H";
            unitColor = GREEN;
        } else if (hasHero()) {
            unitChar = "H";
            unitColor = GREEN;
        } else if (hasMonster()) {
            unitChar = "M";
            unitColor = RED;
        }

        boolean nexus = type == TileType.NEXUS;
        String borderColor = nexus ? YELLOW : "";
        String borderReset = nexus ? RESET : "";

        String horizontal = repeat("-", BLOCK_WIDTH - 2);
        String top = borderColor + "+" + horizontal + "+" + borderReset;
        String bottom = borderColor + "+" + horizontal + "+" + borderReset;
        String emptyLine = borderColor + "|" + borderReset + repeat(" ", BLOCK_WIDTH - 2) + borderColor + "|" + borderReset;

        String unitLine = borderColor + "|" + borderReset + "   "
                + unitColor + unitChar + RESET + "   "
                + borderColor + "|" + borderReset;
        String terrainLine = borderColor + "|" + borderReset + "   "
                + terrainColor + terrainChar + RESET + "   "
                + borderColor + "|" + borderReset;

        block[0] = top;
        block[1] = emptyLine;
        block[2] = unitLine;
        block[3] = terrainLine;
        block[4] = emptyLine;
        block[5] = bottom;
        return block;
    }

    private char getTerrainSymbol(TileType type) {
        switch (type) {
            case BUSH:
                return 'B';
            case CAVE:
                return 'C';
            case KOULOU:
                return 'K';
            case OBSTACLE:
                return 'X';
            case NEXUS:
                return 'N';
            case INACCESSIBLE:
                return 'X';
            default:
                return ' ';
        }
    }

    private String getTerrainColor(TileType type) {
        switch (type) {
            case BUSH:
            case CAVE:
            case KOULOU:
                return BLUE;
            case OBSTACLE:
            case INACCESSIBLE:
                return GRAY;
            case NEXUS:
                return YELLOW;
            default:
                return "";
        }
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
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
