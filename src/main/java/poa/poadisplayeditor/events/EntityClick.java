package poa.poadisplayeditor.events;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import poa.poadisplayeditor.util.NearestEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EntityClick implements Listener {


    @EventHandler //todo fix
    public void onEntityClick(PlayerInteractAtEntityEvent e){
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        final Entity entity = e.getRightClicked();

        if(!InventoryClick.entityListMap.containsKey(uuid))
            return;

        final List<Entity> entityList = InventoryClick.entityListMap.get(uuid);

        if(!entityList.contains(entity))
            return;

        final Display nearestEntityByType = NearestEntity.getNearestEntityByType(entity.getLocation(), Display.class, 0.1);
        if(nearestEntityByType == null){
            player.sendRichMessage("<red>No display entity found at this location, could be an error?");
            return;
        }

        InventoryClick.editingMap.put(uuid, nearestEntityByType);
        player.sendRichMessage("<green>Display selected");

        InventoryClick.entityListMap.remove(uuid);

        for (int i = 0; i < entityList.size(); i++) {
            entityList.get(i).remove();
        }



    }

    @EventHandler
    public void onDeath(EntityDeathEvent e){
        if (InventoryClick.entityListMap.isEmpty())
            return;

        for (List<Entity> value : InventoryClick.entityListMap.values()) {
            if(value.contains(e.getEntity())){
                e.setCancelled(true);
                return;
            }
        }
    }

}
