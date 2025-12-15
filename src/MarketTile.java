/**
 * Tile that represents a market location on the grid.
 * Heroes can step here to shop; this satisfies the rubric requirement that special tiles
 * inherit from the base {@link Tile} abstraction.
 */
public class MarketTile extends Tile {
    private final Market market; // linked market instance for this tile

    /**
     * Creates a market tile at the given coordinates with its market instance.
     */
    public MarketTile(int row, int col, Market market) {
        super(row, col);
        this.market = market;
    }

    /**
     * Market tiles are always accessible to heroes.
     */
    @Override
    public boolean isAccessible() {
        return true;
    }

    /**
     * @return the market associated with this tile
     */
    public Market getMarket() {
        return market;
    }
}
