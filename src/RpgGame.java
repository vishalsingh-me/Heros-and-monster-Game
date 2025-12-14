import java.util.Scanner;

/**
 * Simple shared base class for all text-based RPG games in this project.
 * It owns the Scanner so different games played in the same process
 * do not compete for System.in.
 */
public abstract class RpgGame {

    protected final Scanner scanner;

    protected RpgGame(Scanner scanner) {
        if (scanner != null) {
            this.scanner = scanner;
        } else {
            this.scanner = new Scanner(System.in);
        }
    }

    /**
     * Entry point for one full play-through of the game.
     * Each concrete game implements its own main loop here.
     */
    public abstract void run();
}
