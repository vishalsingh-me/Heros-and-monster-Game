import java.util.Scanner;

/**
 * Generic game abstraction.
 * 
 * Uses the Template Method pattern: run() defines the fixed high-level
 * steps of running a game, while subclasses implement the details.
 */
public abstract class Game {

    protected final Scanner scanner;
    protected boolean running = true;

    public Game(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Template method – final so subclasses cannot change the overall flow.
     */
    public final void run() {
        init();
        while (running) {
            loop();
        }
        shutdown();
    }

    /** Initialize resources, show intro, etc. */
    protected abstract void init();

    /** One iteration of the main game loop. */
    protected abstract void loop();

    /** Cleanup / final messages. */
    protected void shutdown() {
        // default: do nothing
    }

    /** Helper to allow games to request exit. */
    protected void stop() {
        this.running = false;
    }
}
