public class CommonTile extends Tile {

    public CommonTile(int row, int col) {
        super(row, col);
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
