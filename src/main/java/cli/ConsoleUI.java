package cli;

import lombok.AllArgsConstructor;

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
            if (!scanner.hasNextLine()) break;

            String input = scanner.nextLine().toLowerCase().trim();
            CommandResult result = commandHandler.handle(input);

            if (result == CommandResult.EXIT) break;
        }

        scanner.close();
    }
}
