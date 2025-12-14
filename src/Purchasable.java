/**
 * Abstraction for things that can be bought & sold in the Market.
 */
public interface Purchasable {

    String getName();

    int getPrice();

    int getRequiredLevel();
}
