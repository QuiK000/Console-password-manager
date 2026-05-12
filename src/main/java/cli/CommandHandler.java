package cli;

import cli.commands.AddCommand;
import cli.commands.DeleteCommand;
import cli.commands.ExitCommand;
import cli.commands.ListCommand;
import cli.commands.UpdateCommand;
import lombok.AllArgsConstructor;
import model.Entry;
import util.ConsoleUtils;
import util.PasswordGenerator;
import util.TableUtils;
import util.TotpUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class CommandHandler {
    private final AddCommand addCommand;
    private final ListCommand listCommand;
    private final UpdateCommand updateCommand;
    private final DeleteCommand deleteCommand;
    private final ExitCommand exitCommand;

    public CommandResult handle(String input) {
        if (input == null || input.isBlank()) return CommandResult.CONTINUE;

        String[] parts = input.split("\\s+");
        String command = parts[0];
        String arg = null;

        if (parts.length > 1) arg = parts[1];

        switch (command) {
            case "add" -> {
                String site = ConsoleUtils.readLine("site > ");
                String login = ConsoleUtils.readLine("login > ");
                String totpSecret = ConsoleUtils.readLine("TOTP Secret (optional, press Enter to skip) > ");
                String rawInput = ConsoleUtils.readLine("password (or 'gen') > ");

                char[] password;
                boolean generated = false;

                if (rawInput.equalsIgnoreCase("gen") || rawInput.isBlank()) {
                    int length = ThreadLocalRandom.current().nextInt(12, 25);
                    boolean useSymbols = ConsoleUtils.readLine("Use symbols? (y/n) > ").equalsIgnoreCase("y");

                    password = PasswordGenerator.generate(length, true, true, useSymbols);
                    generated = true;

                    System.out.println("Generated password: " + ConsoleUtils.mask(new String(password)));
                    ConsoleUtils.copyToClipboard(new String(password));
                } else {
                    password = rawInput.toCharArray();
                }

                Entry entry = Entry.builder()
                        .id(UUID.randomUUID().toString())
                        .site(site)
                        .login(login)
                        .password(password)
                        .notes("")
                        .totpSecret(totpSecret.isBlank() ? null : totpSecret)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(null)
                        .build();

                addCommand.addEntry(entry);

                if (!generated) System.out.println("Saved password: " + ConsoleUtils.mask(new String(password)));
                return CommandResult.CONTINUE;
            }
            case "list" -> {
                var entries = listCommand.getEntries();

                if (entries.isEmpty()) {
                    System.out.println("No entries found");
                    return CommandResult.CONTINUE;
                } else {
                    TableUtils.printEntries(entries);
                }

                return CommandResult.CONTINUE;
            }
            case "get" -> {
                Entry entry = resolveEntry(arg);
                if (entry == null) return CommandResult.CONTINUE;

                System.out.println("Site: " + entry.getSite());
                System.out.println("Login: " + entry.getLogin());

                if (entry.getTotpSecret() != null)
                    System.out.println("2FA Code: " + TotpUtils.generateCode(entry.getTotpSecret()));

                String passStr = new String(entry.getPassword());
                System.out.println("Password: " + ConsoleUtils.mask(passStr));

                ConsoleUtils.copyToClipboard(passStr);
                return CommandResult.CONTINUE;
            }
            case "update" -> {
                Entry entry = resolveEntry(arg);
                if (entry == null) return CommandResult.CONTINUE;

                String updateTotp = ConsoleUtils.readLine("Update TOTP Secret? (y/n) > ");
                if (updateTotp.equalsIgnoreCase("y")) {
                    String newSecret = ConsoleUtils.readLine("New TOTP Secret (or leave empty to remove) > ");
                    entry.setTotpSecret(newSecret.isBlank() ? null : newSecret);
                }

                String rawInput = ConsoleUtils.readLine("New password (or 'gen', or press Enter to keep current) > ");

                if (!rawInput.isBlank()) {
                    char[] newPassword;
                    boolean generated = false;

                    if (rawInput.equalsIgnoreCase("gen")) {
                        String lenInput = ConsoleUtils.readLine("Length (default 16) > ");
                        int length = lenInput.isBlank() ? 16 : Integer.parseInt(lenInput);
                        boolean useSymbols = ConsoleUtils.readLine("Use symbols? (y/n) > ").equalsIgnoreCase("y");

                        newPassword = PasswordGenerator.generate(length, true, true, useSymbols);
                        generated = true;
                    } else {
                        newPassword = rawInput.toCharArray();
                    }

                    entry.pushToHistory(entry.getPassword());
                    entry.setPassword(newPassword);

                    if (generated) {
                        System.out.println("Generated: " + ConsoleUtils.mask(new String(newPassword)));
                        ConsoleUtils.copyToClipboard(new String(newPassword));
                    }
                }

                entry.setUpdatedAt(LocalDateTime.now());
                updateCommand.update(entry);

                System.out.println("Entry updated successfully.");
                return CommandResult.CONTINUE;
            }
            case "delete" -> {
                Entry entry = resolveEntry(arg);
                if (entry == null) return CommandResult.CONTINUE;

                deleteCommand.removeEntry(entry);
                return CommandResult.CONTINUE;
            }
            case "clear" -> {
                ConsoleUtils.clearScreen();
                return CommandResult.CONTINUE;
            }
            case "help" -> {
                System.out.println("""
                        Commands:
                         add        - add new entry
                         list       - list all entries
                         get [id]   - get entry by id (index)
                         update [id]- update entry by id (index)
                         delete [id]- delete entry by id (index)
                         clear      - clear console screen
                         exit       - exit program
                        """);
                return CommandResult.CONTINUE;
            }
            case "exit" -> {
                System.out.println("Bye!");
                return exitCommand.exit();
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
