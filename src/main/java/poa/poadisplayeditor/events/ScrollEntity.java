package poa.poadisplayeditor.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.util.Vector;
import poa.poadisplayeditor.util.holders.GUIHolder;

import java.util.UUID;

public class ScrollEntity implements Listener {


    @EventHandler
    public void onPlayerScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        final UUID uuid = player.getUniqueId();
        final Entity selectedEntity = InventoryClick.easyMoveMap.getOrDefault(uuid, null);

        if (selectedEntity == null) {
            return;
        }
        event.setCancelled(true);
        final Float move = InventoryClick.moveAmountMap.get(uuid);

        int previousSlot = event.getPreviousSlot();
        int newSlot = event.getNewSlot();
        boolean scrollUp = newSlot < previousSlot;

        Location playerEyeLocation = player.getEyeLocation();
        Vector direction = playerEyeLocation.getDirection().normalize();

        Vector stepVector = direction.clone().multiply(move * (scrollUp ? -1 : 1));

        Location entityLocation = selectedEntity.getLocation();
        Location newLocation = entityLocation.clone().add(stepVector);

        selectedEntity.teleport(newLocation);
    }


}
