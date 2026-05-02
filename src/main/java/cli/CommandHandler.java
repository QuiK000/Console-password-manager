package cli;

import cli.commands.AddCommand;
import cli.commands.DeleteCommand;
import cli.commands.ListCommand;
import lombok.AllArgsConstructor;
import model.Entry;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

@AllArgsConstructor
public class CommandHandler {
    private final AddCommand addCommand;
    private final ListCommand listCommand;
    private final DeleteCommand deleteCommand;

    public CommandResult handle(String input) {
        if (input == null || input.isBlank()) return CommandResult.CONTINUE;

        String[] parts = input.split("\\s+");
        String command = parts[0];
        String arg = null;

        if (parts.length > 1) arg = parts[1];

        switch (command) {
            case "add" -> {
                Scanner scanner = new Scanner(System.in);

                System.out.print("site > ");
                String site = scanner.nextLine().trim();

                System.out.print("login > ");
                String login = scanner.nextLine().trim();

                System.out.print("password > ");
                String password = scanner.nextLine().trim();

                Entry entry = Entry.builder()
                        .id(UUID.randomUUID().toString())
                        .site(site)
                        .login(login)
                        .password(password)
                        .notes("")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(null)
                        .build();

                addCommand.addEntry(entry);
                return CommandResult.CONTINUE;
            }
            case "list" -> {
                var entries = listCommand.getEntries();

                if (entries.isEmpty()) {
                    System.out.println("No entries found");
                    return CommandResult.CONTINUE;
                }

                int index = 1;

                for (Entry entry : entries) {
                    System.out.printf(
                            "%d. %s (%s)%n",
                            index++, entry.getSite(), entry.getLogin()
                    );
                }

                return CommandResult.CONTINUE;
            }
            case "get" -> {
                Entry entry = resolveEntry(arg);
                if (entry == null) return CommandResult.CONTINUE;

                System.out.println("Site: " + entry.getSite());
                System.out.println("Login: " + entry.getLogin());
                System.out.println("Password: " + entry.getPassword());

                return CommandResult.CONTINUE;
            }
            case "delete" -> {
                Entry entry = resolveEntry(arg);
                if (entry == null) return CommandResult.CONTINUE;

                deleteCommand.removeEntry(entry);
                return CommandResult.CONTINUE;
            }
            case "help" -> {
                System.out.println("""
                        Commands:
                         add     - add new entry
                         list    - list all entries
                         get     - get entry by id
                         delete  - delete entry
                         exit    - exit program
                        """);
                return CommandResult.CONTINUE;
            }
            case "exit" -> {
                System.out.println("Bye!");
                return CommandResult.EXIT;
            }
            default -> {
                System.out.println("Unknown command. Type 'help' for info.");
                return CommandResult.CONTINUE;
            }
        }
    }

    private Integer resolveIndexArgument(String arg) {
        if (arg == null) {
            System.out.println("Please provide an index");
            return null;
        }

        try {
            int index = Integer.parseInt(arg);
            return index - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number");
            return null;
        }
    }

    private Entry resolveEntry(String arg) {
        Integer index = resolveIndexArgument(arg);
        if (index == null) return null;

        var entries = listCommand.getEntries();
        if (index < 0 || index >= entries.size()) {
            System.out.println("Entry not found");
            return null;
        }

        return entries.get(index);
    }
}
