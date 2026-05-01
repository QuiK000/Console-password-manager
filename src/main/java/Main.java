import cli.CommandHandler;
import cli.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        CommandHandler commandHandler = new CommandHandler();
        ConsoleUI console = new ConsoleUI(commandHandler);

        console.run();
    }
}
