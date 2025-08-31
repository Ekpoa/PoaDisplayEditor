package poa.poadisplayeditor.util.holders;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;


@Getter
@Setter
public class GUIHolder implements InventoryHolder {

    boolean leftRot = true;

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
