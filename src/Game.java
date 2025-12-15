import java.util.Scanner;

/**
 * Generic game abstraction using the Template Method pattern.
 * run() is final so subclasses define init/loop/shutdown but cannot change the skeleton.
 */
public abstract class Game {

    protected final Scanner scanner;
    protected boolean running = true;

    /**
     * Wire in a shared Scanner for user input.
     */
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
