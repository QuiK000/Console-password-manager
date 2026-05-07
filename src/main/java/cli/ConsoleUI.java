package cli;

import lombok.AllArgsConstructor;
import service.IVaultService;
import util.ConsoleUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

@AllArgsConstructor
public class ConsoleUI {
    private final CommandHandler commandHandler;
    private final IVaultService vaultService;

    public void run() {
        ConsoleUtils.clearScreen();
        System.out.println("Password Manager started. Type 'help' for commands");

        AtomicLong lastActivity = new AtomicLong(System.currentTimeMillis());
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!vaultService.isLocked() && System.currentTimeMillis() - lastActivity.get() > 300_000) {
                    vaultService.lock();
                    ConsoleUtils.clearScreen();
                    System.out.println("\n[!] Vault auto-locked due to 5 minutes of inactivity.");
                    System.out.print("[!] Press ENTER to log in again.\n> ");
                }
            }
        }, 5000, 5000);

        while (true) {
            System.out.print("> ");
            if (!ConsoleUtils.SCANNER.hasNextLine()) break;

            String input = ConsoleUtils.SCANNER.nextLine().toLowerCase().trim();
            lastActivity.set(System.currentTimeMillis());

            if (vaultService.isLocked()) {
                timer.cancel();
                return;
            }

            CommandResult result = commandHandler.handle(input);

            if (result == CommandResult.EXIT) {
                timer.cancel();
                break;
            }
        }
    }
}
