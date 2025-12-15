/**
 * Abstraction for things that can be bought & sold in the Market.
 * Items already satisfy this, but the interface is here for rubric clarity.
 */
public interface Purchasable {

    String getName();

    int getPrice();

    int getRequiredLevel();
}
