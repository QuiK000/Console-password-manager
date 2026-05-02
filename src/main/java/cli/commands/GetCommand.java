package cli.commands;

import lombok.AllArgsConstructor;
import model.Entry;
import service.IVaultService;

@AllArgsConstructor
public class GetCommand {
    private final IVaultService vaultService;

    public Entry getEntry(Entry entry) {
        return vaultService.getEntry(entry);
    }
}
