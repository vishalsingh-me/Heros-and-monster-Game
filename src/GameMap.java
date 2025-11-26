import java.util.Random;

public class GameMap {
    private final Tile[][] grid;
    private int heroRow;
    private int heroCol;
    private final Random random = new Random();

    public GameMap(int size, Tile[][] grid, int startRow, int startCol) {
        if (size <= 0 || grid == null || grid.length != size || grid[0].length != size) {
            throw new IllegalArgumentException("Invalid map configuration");
        }
        this.grid = grid;
        if (!isWithinBounds(startRow, startCol) || !grid[startRow][startCol].isAccessible()) {
            throw new IllegalArgumentException("Invalid start position");
        }
        this.heroRow = startRow;
        this.heroCol = startCol;
    }

    public Tile getCurrentTile() {
        return grid[heroRow][heroCol];
    }

    public int getHeroRow() {
        return heroRow;
    }

    public int getHeroCol() {
        return heroCol;
    }

    public boolean move(int dRow, int dCol) {
        int newRow = heroRow + dRow;
        int newCol = heroCol + dCol;
        if (!isWithinBounds(newRow, newCol)) {
            return false;
        }
        Tile destination = grid[newRow][newCol];
        if (!destination.isAccessible()) {
            return false;
        }
        heroRow = newRow;
        heroCol = newCol;
        return true;
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < grid.length && col < grid[0].length;
    }

    public void render() {
        final String RESET = "\u001B[0m";
        final String CYAN = "\u001B[36m";
        final String GREEN = "\u001B[32m";
        final String RED = "\u001B[31m";
        final String YELLOW = "\u001B[33m";

        System.out.println(CYAN + "=== WORLD MAP ===" + RESET);
        System.out.println("Legend: "
                + YELLOW + "H=Hero " + RESET
                + GREEN + "M=Market " + RESET
                + RED + "X=Blocked " + RESET
                + "C=Common");

        final String TL = "┌";
        final String TR = "┐";
        final String BL = "└";
        final String BR = "┘";
        final String T = "┬";
        final String B = "┴";
        final String L = "├";
        final String R = "┤";
        final String CROSS = "┼";
        final String HOR = "─";
        final String VER = "│";

        int cols = grid[0].length;
        String top = TL + repeat(HOR, 3, cols, T) + TR;
        String mid = L + repeat(HOR, 3, cols, CROSS) + R;
        String bot = BL + repeat(HOR, 3, cols, B) + BR;

        System.out.println(top);
        for (int r = 0; r < grid.length; r++) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append(VER);
            for (int c = 0; c < grid[r].length; c++) {
                String cell;
                if (r == heroRow && c == heroCol) {
                    cell = YELLOW + "H" + RESET;
                } else if (grid[r][c] instanceof MarketTile) {
                    cell = GREEN + "M" + RESET;
                } else if (grid[r][c] instanceof InaccessibleTile) {
                    cell = RED + "X" + RESET;
                } else {
                    cell = "C";
                }
                rowBuilder.append(" ").append(cell).append(" ").append(VER);
            }
            System.out.println(rowBuilder);
            if (r < grid.length - 1) {
                System.out.println(mid);
            }
        }
        System.out.println(bot);
        System.out.println(CYAN + "Controls:\n   W/A/S/D move\n   M map\n   I inventory\n   B back\n   Q quit" + RESET);
    }

    private String repeat(String fill, int count, int cells, String junction) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells; i++) {
            sb.append(fill.repeat(count));
            if (i < cells - 1) {
                sb.append(junction);
            }
        }
        return sb.toString();
    }

    public static GameMap generateDefault(int size, Market market) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        Tile[][] grid = new Tile[size][size];
        Random rand = new Random();

        while (true) {
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    int roll = rand.nextInt(100);
                    if (roll < 5) {
                        grid[r][c] = new InaccessibleTile(r, c);
                    } else if (roll < 15) {
                        grid[r][c] = new MarketTile(r, c, market);
                    } else {
                        grid[r][c] = new CommonTile(r, c);
                    }
                }
            }

            // ensure start position is accessible with at least two accessible neighbors
            int startRow = -1;
            int startCol = -1;
            outer:
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    if (!grid[r][c].isAccessible()) {
                        continue;
                    }
                    if (countAccessibleNeighbors(grid, r, c) >= 2) {
                        startRow = r;
                        startCol = c;
                        break outer;
                    }
                }
            }

            if (startRow == -1) {
                continue; // regenerate
            }

            // If start tile lacks neighbors, adjust surrounding tiles to be accessible
            int neighbors = countAccessibleNeighbors(grid, startRow, startCol);
            if (neighbors < 2) {
                int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] d : dirs) {
                    int nr = startRow + d[0];
                    int nc = startCol + d[1];
                    if (nr >= 0 && nc >= 0 && nr < size && nc < size) {
                        grid[nr][nc] = new CommonTile(nr, nc);
                    }
                    if (countAccessibleNeighbors(grid, startRow, startCol) >= 2) {
                        break;
                    }
                }
            }

            return new GameMap(size, grid, startRow, startCol);
        }
    }

    private static int countAccessibleNeighbors(Tile[][] grid, int r, int c) {
        int count = 0;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nc >= 0 && nr < grid.length && nc < grid[0].length) {
                if (grid[nr][nc].isAccessible()) {
                    count++;
                }
            }
        }
        return count;
    }
}
