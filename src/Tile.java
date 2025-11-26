public abstract class Tile {
    private final int row;
    private final int col;

    protected Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public abstract boolean isAccessible();
}
