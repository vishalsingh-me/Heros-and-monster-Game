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
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (r == heroRow && c == heroCol) {
                    System.out.print("H ");
                } else if (grid[r][c] instanceof MarketTile) {
                    System.out.print("M ");
                } else if (grid[r][c] instanceof InaccessibleTile) {
                    System.out.print("X ");
                } else {
                    System.out.print("C ");
                }
            }
            System.out.println();
        }
    }

    public static GameMap generateDefault(int size, Market market) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        Tile[][] grid = new Tile[size][size];
        Random rand = new Random();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int roll = rand.nextInt(100);
                if (roll < 10) {
                    grid[r][c] = new InaccessibleTile(r, c);
                } else if (roll < 20) {
                    grid[r][c] = new MarketTile(r, c, market);
                } else {
                    grid[r][c] = new CommonTile(r, c);
                }
            }
        }
        // ensure start position is accessible; pick top-left accessible by scanning
        int startRow = 0;
        int startCol = 0;
        outer:
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c].isAccessible()) {
                    startRow = r;
                    startCol = c;
                    break outer;
                }
            }
        }
        return new GameMap(size, grid, startRow, startCol);
    }
}
