/**
 * Lane enum for Valor map columns.
 */
public enum Lane {
    TOP,
    MID,
    BOT;

    /**
     * Returns the Lane associated with a specific column index.
     * Based on PDF:
     * - Cols 0, 1: Top Lane
     * - Cols 3, 4: Mid Lane
     * - Cols 6, 7: Bot Lane
     * - Cols 2, 5: Walls (returns null)
     */
    public static Lane getLaneForCol(int col) {
        if (col == 0 || col == 1) return TOP;
        if (col == 3 || col == 4) return MID;
        if (col == 6 || col == 7) return BOT;
        return null; // Wall columns
    }
}
