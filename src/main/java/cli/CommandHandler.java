package cli;

public class CommandHandler {
    public CommandResult handle(String input) {
        if (input == null || input.isBlank()) return CommandResult.CONTINUE;

        String[] parts = input.split("\\s+");
        String command = parts[0];

        switch (command) {
            case "add" -> {
                System.out.println("Adding entry...");
                return CommandResult.CONTINUE;
            }
            case "list" -> {
                System.out.println("Listing entries...");
                return CommandResult.CONTINUE;
            }
            case "get" -> {
                System.out.println("Getting entry...");
                return CommandResult.CONTINUE;
            }
            case "delete" -> {
                System.out.println("Deleting entry...");
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
}
