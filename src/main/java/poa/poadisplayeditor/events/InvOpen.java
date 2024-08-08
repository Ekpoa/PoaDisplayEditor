package poa.poadisplayeditor.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import poa.poadisplayeditor.util.holders.GUIHolder;

import java.util.UUID;

public class InvOpen implements Listener {

    @EventHandler
    public void invOpen(InventoryOpenEvent e){
        if(e.getInventory().getHolder() instanceof GUIHolder) {
            final UUID uuid = e.getPlayer().getUniqueId();
            InventoryClick.easyMoveMap.remove(uuid);
            InventoryClick.easyEditMap.remove(uuid);
        }
    }

}
