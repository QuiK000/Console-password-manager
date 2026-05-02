import cli.CommandHandler;
import cli.ConsoleUI;
import cli.commands.AddCommand;
import cli.commands.ListCommand;
import model.Vault;
import service.impl.VaultServiceImpl;

public class Main {
    public static void main(String[] args) {
        Vault vault = new Vault();
        VaultServiceImpl vaultService = new VaultServiceImpl(vault);

        AddCommand addCommand = new AddCommand(vaultService);
        ListCommand listCommand = new ListCommand(vaultService);

        CommandHandler commandHandler = new CommandHandler(addCommand, listCommand);
        ConsoleUI console = new ConsoleUI(commandHandler);

        console.run();
    }
}
