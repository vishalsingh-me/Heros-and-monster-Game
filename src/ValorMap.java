import java.util.Random;

public class ValorMap {
    private static final int SIZE = 8;
    private final ValorTile[][] grid;
    private final Random random = new Random();

    public ValorMap() {
        this.grid = new ValorTile[SIZE][SIZE];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                // 1. Determine fixed types (Walls and Nexus)
                if (c == 2 || c == 5) {
                    // Columns 2 and 5 are always walls (separators)
                    grid[r][c] = new ValorTile(r, c, TileType.INACCESSIBLE);
                } else if (r == 0) {
                    // Row 0 is the Monsters' Nexus
                    grid[r][c] = new ValorTile(r, c, TileType.NEXUS);
                } else if (r == SIZE - 1) {
                    // Row 7 is the Heroes' Nexus
                    grid[r][c] = new ValorTile(r, c, TileType.NEXUS);
                } else {
                    // 2. Randomize playable terrain (Rows 1-6)
                    grid[r][c] = new ValorTile(r, c, getRandomTerrain());
                }
            }
        }
    }

    private TileType getRandomTerrain() {
        int roll = random.nextInt(100);
        // Adjusted distribution to include Obstacles
        if (roll < 20) return TileType.BUSH;       // 20%
        if (roll < 40) return TileType.CAVE;       // 20%
        if (roll < 60) return TileType.KOULOU;     // 20%
        if (roll < 70) return TileType.OBSTACLE;   // 10%
        return TileType.PLAIN;                     // 30%
    }

    public ValorTile getTile(int row, int col) {
        if (!isValidCoordinate(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public boolean isValidCoordinate(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public int getHeight() {
        return SIZE;
    }

    public int getWidth() {
        return SIZE;
    }

    /**
     * Simple helper to repeat a character (Java 8 replacement for String.repeat).
     */
    private String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Renders the map to the console.
     */
    public void printMap() {
        System.out.println("  " + repeatChar('-', SIZE * 5));
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                ValorTile tile = grid[r][c];
                String symbol = getTileSymbol(tile);
                System.out.printf("| %-3s", symbol);
            }
            System.out.println("|");
            System.out.println("  " + repeatChar('-', SIZE * 5));
        }
        printLegend();
    }

    private String getTileSymbol(ValorTile tile) {
        if (tile.hasHero() && tile.hasMonster()) return "H&M";
        if (tile.hasHero()) return "H";
        if (tile.hasMonster()) return "M";

        // Classic Java 8 switch
        switch (tile.getType()) {
            case NEXUS:
                return "N";
            case INACCESSIBLE:
                return "I";
            case BUSH:
                return "B";
            case CAVE:
                return "C";
            case KOULOU:
                return "K";
            case OBSTACLE:
                return "X"; // X for Obstacle
            case PLAIN:
                return " ";
            default:
                return "?";
        }
    }

    private void printLegend() {
        System.out.println("Legend: N=Nexus | I=Inaccessible | X=Obstacle | B=Bush | C=Cave | K=Koulou | H=Hero | M=Monster");
    }
}
