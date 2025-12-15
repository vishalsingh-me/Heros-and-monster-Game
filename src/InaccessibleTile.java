/**
 * Represents a permanent blocked tile in the classic Heroes & Monsters map.
 * It inherits from {@link Tile} to satisfy the tile hierarchy and always returns false for access.
 */
public class InaccessibleTile extends Tile {

    /**
     * Creates an inaccessible tile at the given coordinates.
     */
    public InaccessibleTile(int row, int col) {
        super(row, col);
    }

    /**
     * Inaccessible tiles cannot be entered.
     */
    @Override
    public boolean isAccessible() {
        return false;
    }
}
