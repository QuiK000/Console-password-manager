import cli.CommandHandler;
import cli.ConsoleUI;
import cli.commands.AddCommand;
import cli.commands.DeleteCommand;
import cli.commands.ListCommand;
import model.Vault;
import service.impl.VaultServiceImpl;
import storage.impl.FileStorage;
import storage.impl.JsonVaultSerializer;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var storage = new FileStorage(Path.of("vault.json"));
        var serializer = new JsonVaultSerializer();
        var vaultService = new VaultServiceImpl(new Vault(), storage, serializer);

        vaultService.init();

        AddCommand addCommand = new AddCommand(vaultService);
        ListCommand listCommand = new ListCommand(vaultService);
        DeleteCommand deleteCommand = new DeleteCommand(vaultService);

        CommandHandler commandHandler = new CommandHandler(addCommand, listCommand, deleteCommand);
        ConsoleUI console = new ConsoleUI(commandHandler);

        console.run();
    }
}
