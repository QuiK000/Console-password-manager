package cli.commands;

import lombok.AllArgsConstructor;
import model.Entry;
import service.IVaultService;

@AllArgsConstructor
public class UpdateCommand {
    private final IVaultService vaultService;

    public void update(Entry entry) {
        vaultService.updateEntry(entry);
    }
}
