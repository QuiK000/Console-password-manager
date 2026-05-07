import cli.CommandHandler;
import cli.ConsoleUI;
import cli.commands.AddCommand;
import cli.commands.DeleteCommand;
import cli.commands.ExitCommand;
import cli.commands.ListCommand;
import crypto.impl.CryptoServiceImpl;
import service.impl.AuthServiceImpl;
import service.impl.VaultServiceImpl;
import storage.impl.FileStorage;
import storage.impl.JsonVaultSerializer;
import util.ConsoleUtils;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        var storage = new FileStorage(Path.of("vault.json"));
        var serializer = new JsonVaultSerializer();
        var crypto = new CryptoServiceImpl();

        var auth = new AuthServiceImpl(crypto, storage);
        var vaultService = new VaultServiceImpl(storage, serializer, crypto);

        char[] password = null;

        try {
            byte[] salt;
            var key = (SecretKey) null;

            if (auth.isFirstRun()) {
                password = ConsoleUtils.readPassword("Create master password: ");

                key = auth.setupMasterPassword(password);
                salt = auth.getSalt();
            } else {
                byte[] data = storage.load();
                salt = Arrays.copyOfRange(data, 0, 16);

                password = ConsoleUtils.readPassword("Enter master password: ");
                key = auth.login(password, salt);
            }

            vaultService.setSecurity(key, salt);
            vaultService.init();
        } catch (Exception e) {
            System.out.println("Wrong password or corrupted vault");
            return;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }

        var handler = new CommandHandler(
                new AddCommand(vaultService),
                new ListCommand(vaultService),
                new DeleteCommand(vaultService),
                new ExitCommand()
        );

        new ConsoleUI(handler).run();
    }
}
