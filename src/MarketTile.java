public class MarketTile extends Tile {
    private final Market market;

    public MarketTile(int row, int col, Market market) {
        super(row, col);
        this.market = market;
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    public Market getMarket() {
        return market;
    }
}
