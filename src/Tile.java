/**
    * Base tile abstraction used by both the classic map and Valor map.
    * Holds row/col coordinates and an accessibility contract implemented by subclasses.
    */
public abstract class Tile {
    private final int row;
    private final int col;

    /**
     * Builds a tile at a specific grid coordinate.
     */
    protected Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /** @return row index for this tile */
    public int getRow() {
        return row;
    }

    /** @return column index for this tile */
    public int getCol() {
        return col;
    }

    /**
     * @return true if the tile can be entered by a unit; implemented by subclasses.
     */
    public abstract boolean isAccessible();
}
