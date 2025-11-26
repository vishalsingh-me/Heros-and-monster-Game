public class InaccessibleTile extends Tile {

    public InaccessibleTile(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isAccessible() {
        return false;
    }
}
