package cli.commands;

import lombok.AllArgsConstructor;
import model.Entry;
import service.IVaultService;

import java.util.List;

@AllArgsConstructor
public class ListCommand {
    private final IVaultService vaultService;

    public List<Entry> getEntries() {
        return vaultService.listEntries();
    }
}
