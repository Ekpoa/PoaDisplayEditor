package poa.poadisplayeditor.events;

import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ClickBlockItem implements Listener {


    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!InventoryClick.selectItemMap.containsKey(uuid))
            return;

        e.setCancelled(true);

        final Entity entity = InventoryClick.selectItemMap.get(uuid);

        if (e.getAction().isLeftClick()) {
            final ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                player.sendRichMessage("<red>You are not holding an item");
                return;
            }

            if (entity instanceof BlockDisplay blockDisplay) {
                if (!item.getType().isBlock()) {
                    player.sendRichMessage("<red>Item is not a block");
                    return;
                }
                blockDisplay.setBlock(item.getType().createBlockData());
                player.sendRichMessage("<green>Block set");
            } else if (entity instanceof ItemDisplay itemDisplay) {
                itemDisplay.setItemStack(item);
                player.sendRichMessage("<green>Item set");
            }

            InventoryClick.selectItemMap.remove(uuid);
        } else {
            final Block clickedBlock = e.getClickedBlock();
            if (clickedBlock == null) {
                player.sendRichMessage("<red>You must click a block");
                return;
            }

            if (entity instanceof BlockDisplay display) {
                display.setBlock(clickedBlock.getBlockData());
                player.sendRichMessage("<green>Block set");
            } else if (entity instanceof ItemDisplay display) {
                display.setItemStack(new ItemStack(clickedBlock.getType()));
                player.sendRichMessage("<green>Item set");
            }
            InventoryClick.selectItemMap.remove(uuid);
        }

    }

}
