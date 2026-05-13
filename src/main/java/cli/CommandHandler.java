package cli;

import cli.commands.AddCommand;
import cli.commands.DeleteCommand;
import cli.commands.ExitCommand;
import cli.commands.ListCommand;
import cli.commands.UpdateCommand;
import lombok.AllArgsConstructor;
import model.Entry;
import service.IVaultService;
import util.ConsoleUtils;
import util.PasswordGenerator;
import util.TableUtils;
import util.TotpUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class CommandHandler {
    private static final DateTimeFormatter BACKUP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AddCommand addCommand;
    private final ListCommand listCommand;
    private final UpdateCommand updateCommand;
    private final DeleteCommand deleteCommand;
    private final ExitCommand exitCommand;
    private final IVaultService vaultService;
    private final SessionSettings sessionSettings;

    public CommandResult handle(String input) {
        if (input == null || input.isBlank()) return CommandResult.CONTINUE;

        String[] parts = input.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String arg = parts.length > 1 ? parts[1].trim() : null;

        switch (command) {
            case "add" -> {
                String site = ConsoleUtils.readLine("site > ");
                String login = ConsoleUtils.readLine("login > ");
                String notes = ConsoleUtils.readLine("notes (optional, press Enter to skip) > ");
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
                        .notes(notes.isBlank() ? null : notes)
                        .totpSecret(totpSecret.isBlank() ? null : totpSecret)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(null)
                        .build();

                addCommand.addEntry(entry);

                if (!generated) System.out.println("Saved password: " + ConsoleUtils.mask(new String(password)));
                return CommandResult.CONTINUE;
            }
            case "list" -> {
                List<Entry> entries = filterEntries(arg);
                printEntries(entries);
                return CommandResult.CONTINUE;
            }
            case "search" -> {
                if (arg == null || arg.isBlank()) {
                    System.out.println("Please provide a search query");
                    return CommandResult.CONTINUE;
                }

                List<Entry> entries = filterEntries(arg);
                printEntries(entries);

                return CommandResult.CONTINUE;
            }
            case "get" -> {
                Entry entry = resolveEntry(arg);
                if (entry == null) return CommandResult.CONTINUE;

                System.out.println("Site: " + entry.getSite());
                System.out.println("Login: " + entry.getLogin());

                if (entry.getNotes() != null && !entry.getNotes().isBlank()) {
                    System.out.println("Notes: " + entry.getNotes());
                }

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

                String newSite = ConsoleUtils.readLine("New site (press Enter to keep current) > ");
                if (!newSite.isBlank()) entry.setSite(newSite);

                String newLogin = ConsoleUtils.readLine("New login (press Enter to keep current) > ");
                if (!newLogin.isBlank()) entry.setLogin(newLogin);

                String updateNotes = ConsoleUtils.readLine("Update notes? (y/n) > ");
                if (updateNotes.equalsIgnoreCase("y")) {
                    String newNotes = ConsoleUtils.readLine("New notes (or leave empty to remove) > ");
                    entry.setNotes(newNotes.isBlank() ? null : newNotes);
                }

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
            case "lock" -> {
                vaultService.lock();
                System.out.println("Vault locked.");
                return CommandResult.LOCK;
            }
            case "timeout" -> {
                handleTimeout(arg);
                return CommandResult.CONTINUE;
            }
            case "change-master" -> {
                handleChangeMasterPassword();
                return CommandResult.CONTINUE;
            }
            case "backup" -> {
                Path backupPath = resolveBackupPath(arg);
                vaultService.backup(backupPath);

                System.out.println("Backup created: " + backupPath.toAbsolutePath());
                return CommandResult.CONTINUE;
            }
            case "restore" -> {
                handleRestore(arg);
                return CommandResult.CONTINUE;
            }
            case "clear" -> {
                ConsoleUtils.clearScreen();
                return CommandResult.CONTINUE;
            }
            case "help" -> {
                System.out.println("""
                        Commands:
                         add                 - add new entry
                         list [filter]       - list all entries, optionally filtered by site/login/notes
                         search <query>      - search entries by site/login/notes
                         get [id]            - get entry by id (index)
                         update [id]         - update entry by id (index)
                         delete [id]         - delete entry by id (index)
                         lock                - lock vault and return to login prompt
                         timeout [30s|5m|1h] - show or set auto-lock timeout
                         change-master       - change master password and re-encrypt vault
                         backup [path]       - create encrypted vault backup
                         restore <path>      - restore encrypted vault backup
                         clear               - clear console screen
                         exit                - exit program
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

    private void printEntries(List<Entry> entries) {
        if (entries.isEmpty()) {
            System.out.println("No entries found");
        } else {
            TableUtils.printEntries(entries, listCommand.getEntries());
        }
    }

    private List<Entry> filterEntries(String query) {
        var entries = listCommand.getEntries();
        if (query == null || query.isBlank()) return entries;

        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return entries.stream()
                .filter(entry -> containsIgnoreCase(entry.getSite(), normalizedQuery)
                        || containsIgnoreCase(entry.getLogin(), normalizedQuery)
                        || containsIgnoreCase(entry.getNotes(), normalizedQuery))
                .toList();
    }

    private boolean containsIgnoreCase(String value, String normalizeQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizeQuery);
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

    private void handleTimeout(String arg) {
        if (arg == null || arg.isBlank()) {
            System.out.println("Current auto-lock timeout: " + sessionSettings.describeAutoLockTimeout());
            return;
        }

        try {
            sessionSettings.setAutoLockTimeoutMillis(parseDurationMillis(arg));
            System.out.println("Auto-lock timeout set to " + sessionSettings.describeAutoLockTimeout());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid timeout: " + e.getMessage());
        }
    }

    private long parseDurationMillis(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        String number = normalized;

        long multiplier = 1000;

        if (normalized.endsWith("ms")) {
            multiplier = 1;
            number = normalized.substring(0, normalized.length() - 2);
        } else if (normalized.endsWith("s")) {
            number = normalized.substring(0, normalized.length() - 1);
        } else if (normalized.endsWith("m")) {
            multiplier = 60_000;
            number = normalized.substring(0, normalized.length() - 1);
        } else if (normalized.endsWith("h")) {
            multiplier = 3_600_000;
            number = normalized.substring(0, normalized.length() - 1);
        }

        return Long.parseLong(number) * multiplier;
    }

    private void handleChangeMasterPassword() {
        char[] currentPassword = null;
        char[] newPassword = null;
        char[] confirmation = null;

        try {
            currentPassword = ConsoleUtils.readPassword("Current master password: ");
            newPassword = ConsoleUtils.readPassword("New master password: ");
            confirmation = ConsoleUtils.readPassword("Repat new master password: ");

            if (!Arrays.equals(newPassword, confirmation)) {
                System.out.println("New master passwords do not match.");
                return;
            }

            vaultService.changeMasterPassword(currentPassword, newPassword);
            System.out.println("Master password changed successfully.");
        } catch (Exception e) {
            System.out.println("Failed to change master password: " + e.getMessage());
        } finally {
            clear(currentPassword);
            clear(newPassword);
            clear(confirmation);
        }
    }

    private Path resolveBackupPath(String arg) {
        if (arg != null && !arg.isBlank()) return Path.of(arg);

        String filename = "vault-" + BACKUP_TIMESTAMP_FORMAT.format(LocalDateTime.now()) + ".bak";
        return Path.of("backups", filename);
    }

    private void handleRestore(String arg) {
        if (arg == null || arg.isBlank()) {
            System.out.println("Please provide a backup path");
            return;
        }

        String confirmation = ConsoleUtils.readLine("Restore will replace the current vault. Type RESTORE to continue > ");

        if (!"RESTORE".equals(confirmation)) {
            System.out.println("Restore cancelled.");
            return;
        }

        char[] masterPassword = null;
        try {
            masterPassword = ConsoleUtils.readPassword("Backup master password: ");
            vaultService.restore(Path.of(arg), masterPassword);
            System.out.println("Backup restored successfully.");
        } catch (Exception e) {
            System.out.println("Failed to restore backup: " + e.getMessage());
        } finally {
            clear(masterPassword);
        }
    }

    private void clear(char[] value) {
        if (value != null) Arrays.fill(value, '\0');
    }
}
