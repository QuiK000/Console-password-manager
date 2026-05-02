package cli.commands;

import lombok.AllArgsConstructor;
import model.Entry;
import service.IVaultService;

@AllArgsConstructor
public class AddCommand {
    private final IVaultService vaultService;

    public void addEntry(Entry entry) {
        vaultService.addEntry(entry);
    }
}
