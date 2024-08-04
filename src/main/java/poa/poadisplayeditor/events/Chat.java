package poa.poadisplayeditor.events;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import poa.poadisplayeditor.PoaDisplayEditor;
import poa.poalib.Messages.Messages;

import java.util.UUID;

public class Chat implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        String stringMessage = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (InventoryClick.yawMap.containsKey(uuid) || InventoryClick.pitchMap.containsKey(uuid)) {
            e.setCancelled(true);

            Entity entity = InventoryClick.editingMap.get(uuid);

            final float amount = Float.parseFloat(stringMessage);

            Location clone = entity.getLocation().clone();

            if (InventoryClick.pitchMap.containsKey(uuid)) {
                clone.setPitch(amount);
                InventoryClick.pitchMap.remove(uuid);
                player.sendRichMessage("<green>Set pitch to " + amount);

            } else if (InventoryClick.yawMap.containsKey(uuid)) {
                clone.setYaw(amount);
                InventoryClick.yawMap.remove(uuid);
                player.sendRichMessage("<green>Set pitch to " + amount);
            }
            Bukkit.getScheduler().runTask(PoaDisplayEditor.getINSTANCE(), () -> entity.teleport(clone));
        } else if (InventoryClick.newLineTextMap.containsKey(uuid)) {
            e.setCancelled(true);
            TextDisplay display = InventoryClick.newLineTextMap.get(uuid);

            String string = InventoryClick.getTextComponentText(display);

            string = string + "\n" + Messages.essentialsToMinimessage(stringMessage);

            display.text(MiniMessage.miniMessage().deserialize(string));
            player.sendRichMessage("<green>Text display updated");

            InventoryClick.newLineTextMap.remove(uuid);


        } else if (InventoryClick.appendTextMap.containsKey(uuid)) {
            e.setCancelled(true);
            TextDisplay display = InventoryClick.appendTextMap.get(uuid);

            String string = InventoryClick.getTextComponentText(display);

            string = string + Messages.essentialsToMinimessage(stringMessage);

            display.text(MiniMessage.miniMessage().deserialize(string));
            player.sendRichMessage("<green>Text display updated");

            InventoryClick.appendTextMap.remove(uuid);
            LoadEntity.updateTextForAll(display);
        } else if (InventoryClick.textOpacityMap.containsKey(uuid)) {
            e.setCancelled(true);
            TextDisplay display = InventoryClick.textOpacityMap.get(uuid);

            display.setTextOpacity(Byte.parseByte(stringMessage));

            player.sendRichMessage("<green>Text opacity updated");

            InventoryClick.textOpacityMap.remove(uuid);
            LoadEntity.updateTextForAll(display);
        } else if (InventoryClick.glowMap.containsKey(uuid)) {
            e.setCancelled(true);
            Display display = InventoryClick.glowMap.get(uuid);

            display.setGlowColorOverride(getColorFromMessage(stringMessage));

            player.sendRichMessage("<green>Updated Glow");
            InventoryClick.glowMap.remove(uuid);
        }
        else if (InventoryClick.backgroundMap.containsKey(uuid)) {
            e.setCancelled(true);
            TextDisplay display = InventoryClick.backgroundMap.get(uuid);

            display.setBackgroundColor(getColorFromMessage(stringMessage));

            player.sendRichMessage("<green>Updated background");
            InventoryClick.backgroundMap.remove(uuid);
        }
        else if (InventoryClick.widthMap.containsKey(uuid)) {
            e.setCancelled(true);
            TextDisplay display = InventoryClick.widthMap.get(uuid);

            display.setLineWidth(Integer.parseInt(stringMessage));

            player.sendRichMessage("<green>Updated background");
            InventoryClick.widthMap.remove(uuid);
        }


    }

    private static Color getColorFromMessage(String message) {
        message = message.replaceAll(",", "");
        final String[] split = message.split(" ");

        int r = Integer.parseInt(split[0]);
        int g = Integer.parseInt(split[1]);
        int b = Integer.parseInt(split[2]);
        int a = 255;
        if(split.length == 4)
            a = Integer.parseInt(split[3]);

        return Color.fromARGB(a, r, g, b);
    }

}
