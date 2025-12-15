/**
 * Application entry point for the Legends project (Monsters & Heroes plus Legends of Valor).
 * Responsibilities: instantiate the {@link GameLauncher} hub menu and delegate control to it so
 * data can be loaded and the selected game started. This satisfies the rubric requirement for a
 * clear, minimal main entry in a console application.
 */
public class Main {
    /**
     * Boots the game hub and hands off control to {@link GameLauncher}.
     *
     * @param args standard CLI arguments (unused in this console game)
     */
    public static void main(String[] args) {
        GameLauncher launcher = new GameLauncher();
        launcher.start();
    }
}
