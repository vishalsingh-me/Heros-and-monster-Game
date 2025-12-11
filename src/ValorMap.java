import java.util.Random;

public class ValorMap {
    private static final int SIZE = 8;
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String GRAY = "\u001B[90m";
    private static final int CELL_WIDTH = 5;
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
     * Renders the map to the console using fixed-size ASCII blocks per tile.
     */
    public void printMap() {
        printColumnHeader();
        for (int r = 0; r < SIZE; r++) {
            String[][] blocks = new String[SIZE][];
            for (int c = 0; c < SIZE; c++) {
                blocks[c] = grid[r][c].renderBlock();
            }
            int blockLines = blocks[0].length;
            for (int line = 0; line < blockLines; line++) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%2d ", r));
                for (int c = 0; c < SIZE; c++) {
                    sb.append(blocks[c][line]);
                }
                System.out.println(sb.toString());
            }
        }
        printColumnHeader();
        printLegend();
    }

    private void printColumnHeader() {
        int blockWidth = visibleLength(grid[0][0].renderBlock()[0]);
        StringBuilder header = new StringBuilder("   ");
        for (int c = 0; c < SIZE; c++) {
            header.append(String.format("%" + blockWidth + "d", c));
        }
        System.out.println(header.toString());
    }

    private void printLegend() {
        System.out.println("Legend: "
                + colorize("H", GREEN) + "=Hero "
                + colorize("M", RED) + "=Monster "
                + colorize("N", YELLOW) + "=Nexus "
                + colorize("I", GRAY) + "=Inaccessible "
                + colorize("X", GRAY) + "=Obstacle "
                + colorize("B", BLUE) + "=Bush "
                + colorize("C", BLUE) + "=Cave "
                + colorize("K", BLUE) + "=Koulou");
    }

    private String colorize(String text, String color) {
        return color + text + RESET;
    }

    private int visibleLength(String s) {
        boolean inEsc = false;
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (inEsc) {
                if (ch == 'm') {
                    inEsc = false;
                }
                continue;
            }
            if (ch == 27) {
                inEsc = true;
                continue;
            }
            count++;
        }
        return count;
    }
}
