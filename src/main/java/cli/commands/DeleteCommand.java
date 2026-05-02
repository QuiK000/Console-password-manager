package cli.commands;

import lombok.AllArgsConstructor;
import model.Entry;
import service.IVaultService;

@AllArgsConstructor
public class DeleteCommand {
    private final IVaultService vaultService;

    public void removeEntry(Entry entry) {
        vaultService.deleteEntry(entry);
    }
}
