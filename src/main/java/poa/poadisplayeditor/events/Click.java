package poa.poadisplayeditor.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;
import poa.poadisplayeditor.PoaDisplayEditor;
import poa.poadisplayeditor.util.holders.GUIHolder;
import poa.poalib.Items.CreateItem;
import poa.poalib.shaded.NBT;

import java.util.ArrayList;
import java.util.List;

public class Click implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        final Player player = e.getPlayer();

        final ItemStack item = e.getItem();
        if (item == null || item.getType() != PoaDisplayEditor.getItem())
            return;

        if (!player.hasPermission("poa.displayedit"))
            return;

        e.setCancelled(true);
        player.openInventory(getGui());

    }


    public static Inventory getGui() {
        Inventory inventory = Bukkit.createInventory(new GUIHolder(), 54, MiniMessage.miniMessage().deserialize("<gold>Display Entity Editor"));

        for (int i = 0; i < 54; i++)
            inventory.setItem(i, CreateItem.createBasicItem(Material.BLACK_STAINED_GLASS_PANE, 1, " "));


        inventory.setItem(0, inventoryItem(Material.DIRT, "<green>Spawn Block Display", "spawnblock"));
        inventory.setItem(1, inventoryItem(Material.WOODEN_SWORD, "<green>Spawn Item Display", "spawnitem"));
        inventory.setItem(2, inventoryItem(Material.NAME_TAG, "<green>Spawn Text Display", "spawntext"));

        inventory.setItem(9, inventoryItem(Material.BEDROCK, "<green>Set Block", "setblock"));
        inventory.setItem(10, inventoryItem(Material.DIAMOND_AXE, "<green>Set Item", "setitem"));
        inventory.setItem(11, inventoryItem(Material.WRITABLE_BOOK, "<green>Edit Text", "settext"));

        inventory.setItem(4, inventoryItem(Material.RED_STAINED_GLASS_PANE, "<green>Modify by 0.01", "move0.01"));
        inventory.setItem(5, inventoryItem(Material.RED_STAINED_GLASS_PANE, "<green>Modify by 0.1", "move0.1"));
        inventory.setItem(6, inventoryItem(Material.RED_STAINED_GLASS_PANE, "<green>Modify by 0.5", "move0.5"));
        inventory.setItem(7, inventoryItem(Material.RED_STAINED_GLASS_PANE, "<green>Modify by 1", "move1"));
        inventory.setItem(8, inventoryItem(Material.RED_STAINED_GLASS_PANE, "<green>Modify by 5", "move5"));

        inventory.setItem(25, inventoryItem(Material.MINECART, "<green>Move Up or Down", "movey", "<gray>Left click to increase", "<gray>Right click to decrease"));
        inventory.setItem(24, inventoryItem(Material.MINECART, "<green>Move Along X", "movex", "<gray>Left click to increase", "<gray>Right click to decrease"));
        inventory.setItem(26, inventoryItem(Material.MINECART, "<green>Move Along Z", "movez", "<gray>Left click to increase", "<gray>Right click to decrease"));
        inventory.setItem(16, inventoryItem(Material.MINECART, "<green>Center To Block", "moveblock"));
        inventory.setItem(34, inventoryItem(Material.COMMAND_BLOCK_MINECART, "<green>Easy Move", "moveeasy"));

        inventory.setItem(33, inventoryItem(Material.REDSTONE_TORCH, "<green>Change Pitch", "pitch", "<gray>Middle click to set"));
        inventory.setItem(35, inventoryItem(Material.REDSTONE_TORCH, "<green>Change Yaw", "yaw", "<gray>Middle click to set"));


        inventory.setItem(50, inventoryItem(Material.REPEATING_COMMAND_BLOCK, "<green>Scale All", "scaleall"));
        inventory.setItem(51, inventoryItem(Material.COMMAND_BLOCK, "<green>Scale X", "scalex"));
        inventory.setItem(52, inventoryItem(Material.COMMAND_BLOCK, "<green>Scale Y", "scaley"));
        inventory.setItem(53, inventoryItem(Material.COMMAND_BLOCK, "<green>Scale Z", "scalez"));


        inventory.setItem(19, inventoryItem(Material.POWERED_RAIL, "<green>Translation X", "translationx"));
        inventory.setItem(27, inventoryItem(Material.POWERED_RAIL, "<green>Translation Y", "translationy"));
        inventory.setItem(29, inventoryItem(Material.POWERED_RAIL, "<green>Translation Z", "translationz"));

        inventory.setItem(36, potionColor(inventoryItem(Material.LINGERING_POTION, "<green>Glow Value", "glowoverride"), 0, 0, 0));
        inventory.setItem(37, potionColor(inventoryItem(Material.LINGERING_POTION, "<green>Glow State", "glow"), 255, 255, 255));

        inventory.setItem(39, inventoryItem(Material.LIGHT, "<green>Brightness", "brightness"));

        inventory.setItem(41, inventoryItem(Material.ARMOR_STAND, "<green>Billboard Fixed", "billboardfixed"));
        inventory.setItem(42, inventoryItem(Material.ARMOR_STAND, "<green>Billboard Vertical", "billboardvertical"));
        inventory.setItem(43, inventoryItem(Material.ARMOR_STAND, "<green>Billboard Horizontal", "billboardhorizontal"));
        inventory.setItem(44, inventoryItem(Material.ARMOR_STAND, "<green>Billboard Center", "billboardcenter"));


        inventory.setItem(47, inventoryItem(Material.WRITTEN_BOOK, "<green>Select Nearest Text Display", "selecttext"));
        inventory.setItem(45, inventoryItem(Material.ANVIL, "<green>Select Nearest Block Display", "selectblock"));
        inventory.setItem(46, inventoryItem(Material.DIAMOND, "<green>Select Nearest Item Display", "selectitem"));
        inventory.setItem(48, inventoryItem(Material.PAINTING, "<green>Easy Select", "selecteasy"));
        inventory.setItem(49, inventoryItem(Material.CREEPER_HEAD, "<green>Nearest Entity", "selectentity", "<gray>This selects the nearest Entity", "<gray>Useful for moving but nothing else", "<gray>Scale All works for this for 1.20.6+"));

        inventory.setItem(22, inventoryItem(Material.BARRIER, "<red>DELETE", "delete"));

        inventory.setItem(31, inventoryItem(Material.NETHER_STAR, "<gold>Information", "",
                "<yellow>Left Click To Increase",
                "<yellow>Right Click To Decrease"
        ));
        return inventory;
    }

    public static ItemStack inventoryItem(Material material, String name, String type, String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize("<i:false>" + name));

        if (lore != null) {
            List<Component> itemLore = new ArrayList<>();
            for (String s : lore) {
                itemLore.add(MiniMessage.miniMessage().deserialize("<i:false>" + s));
            }
            meta.lore(itemLore);
        }
        item.setItemMeta(meta);

        NBT.modify(item, nbt -> {
            nbt.setString("PoaType", type);
        });

        return item;
    }

    private static ItemStack inventoryItem(Material material, String name, String type) {
        return inventoryItem(material, name, type, null);
    }

    private static @Nullable ItemStack potionColor(ItemStack itemStack, int r, int g, int b) {
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();

        meta.setColor(Color.fromRGB(r, g, b));

        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        itemStack.setItemMeta(meta);
        return itemStack;
    }


}
