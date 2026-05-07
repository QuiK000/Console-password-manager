package cli;

import lombok.AllArgsConstructor;
import util.ConsoleUtils;

import java.util.Scanner;

@AllArgsConstructor
public class ConsoleUI {
    private final CommandHandler commandHandler;

    public void run() {
        System.out.println("Password Manager started");
        System.out.println("Type 'help' for commands");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!ConsoleUtils.SCANNER.hasNextLine()) break;

            String input = ConsoleUtils.SCANNER.nextLine().toLowerCase().trim();
            CommandResult result = commandHandler.handle(input);

            if (result == CommandResult.EXIT) break;
        }

        scanner.close();
    }
}
