package poa.poadisplayeditor.events;

import io.papermc.paper.event.player.PlayerTrackEntityEvent;
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

public class LoadEntity implements Listener {

    @EventHandler
    public void playerLoadEntity(PlayerTrackEntityEvent e){
        final Player player = e.getPlayer();
        final Entity entity = e.getEntity();

        if(!(entity instanceof TextDisplay display))
            return;

        changeText(player, display);
    }

    public static void changeText(Player player, TextDisplay display){
        final Metadata metadata = new Metadata(display.getEntityId());

        String text = InventoryClick.getTextComponentText(display);

        text = PlaceholderAPI.setPlaceholders(player, text);

        metadata.setText(text);
        Bukkit.getScheduler().runTaskLaterAsynchronously(PoaDisplayEditor.getINSTANCE(), () -> SendPacket.sendPacket(player, metadata.build()), 1L);
    }

    public static void updateTextForAll(TextDisplay display){
        final Collection<Player> nearbyPlayers = display.getLocation().getNearbyPlayers(200);
        if(nearbyPlayers.isEmpty())
            return;

        for (Player p : nearbyPlayers) {
            changeText(p, display);
        }

    }

}
