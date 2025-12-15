/**
 * Simple ANSI color/style helpers for a nicer terminal UI.
 */
public final class UiColors {

    // Reset
    public static final String RESET  = "\u001B[0m";

    // Styles
    public static final String BOLD   = "\u001B[1m";

    // Basic foreground colors
    public static final String BLACK   = "\u001B[30m";
    public static final String RED     = "\u001B[31m";
    public static final String GREEN   = "\u001B[32m";
    public static final String YELLOW  = "\u001B[33m";
    public static final String BLUE    = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN    = "\u001B[36m";
    public static final String WHITE   = "\u001B[37m";

    // Bright variants
    public static final String BRIGHT_RED     = "\u001B[91m";
    public static final String BRIGHT_GREEN   = "\u001B[92m";
    public static final String BRIGHT_YELLOW  = "\u001B[93m";
    public static final String BRIGHT_BLUE    = "\u001B[94m";
    public static final String BRIGHT_MAGENTA = "\u001B[95m";
    public static final String BRIGHT_CYAN    = "\u001B[96m";
    public static final String BRIGHT_WHITE   = "\u001B[97m";

    // Backgrounds (use sparingly)
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_RED    = "\u001B[41m";

    private UiColors() {
        // no instances
    }
}
