package poa.poadisplayeditor.events;

import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import poa.packets.Metadata;
import poa.packets.SendPacket;
import poa.poadisplayeditor.PoaDisplayEditor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class LoadEntity implements Listener {

    @EventHandler
    public void playerLoadEntity(PlayerTrackEntityEvent e){
        final Player player = e.getPlayer();
        final Entity entity = e.getEntity();

        if(!(entity instanceof TextDisplay display))
            return;

        changeText(player, display);
    }

    public static void changeText(Player player, TextDisplay display, long delay){
        final Metadata metadata = new Metadata(display.getEntityId());

        String text = InventoryClick.getTextComponentText(display);

        text = PlaceholderAPI.setPlaceholders(player, text);

        metadata.setText(text);
        Bukkit.getScheduler().runTaskLaterAsynchronously(PoaDisplayEditor.getINSTANCE(), () -> SendPacket.sendPacket(player, metadata.build()), delay);
    }
    public static void changeText(Player player, TextDisplay display){
        Bukkit.getScheduler().runTask(PoaDisplayEditor.getINSTANCE(), () -> {
            changeText(player, display, 1);
        });
    }

    public static void updateTextForAll(TextDisplay display, long delay){
        Bukkit.getScheduler().runTask(PoaDisplayEditor.getINSTANCE(), () -> {
            final Collection<Player> nearbyPlayers = display.getLocation().getNearbyPlayers(200);
            if (nearbyPlayers.isEmpty())
                return;

            for (Player p : nearbyPlayers) {
                changeText(p, display, delay);
            }
        });
    }
    public static void updateTextForAll(TextDisplay display){
        updateTextForAll(display, 1);
    }



    @EventHandler
    public void unloadEntity(PlayerUntrackEntityEvent e){
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        if(!InventoryClick.entityListMap.containsKey(uuid))
            return;

        final List<Entity> entityList = InventoryClick.entityListMap.get(uuid);
        final Entity entity = e.getEntity();
        if(!entityList.contains(entity))
            return;

        entityList.remove(entity);
        entity.remove();
        if(entityList.isEmpty())
            InventoryClick.entityListMap.remove(uuid);

        InventoryClick.entityListMap.put(uuid, entityList);
    }

}
