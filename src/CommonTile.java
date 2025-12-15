/**
 * Basic walkable tile used in the classic Heroes & Monsters map.
 * Extends {@link Tile} to satisfy the space hierarchy and allows access.
 */
public class CommonTile extends Tile {

    /**
     * Creates a common tile at the given coordinates.
     */
    public CommonTile(int row, int col) {
        super(row, col);
    }

    /**
     * Common tiles are always accessible.
     */
    @Override
    public boolean isAccessible() {
        return true;
    }
}
