package poa.poadisplayeditor.events;

import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import poa.packets.FakeEntity;
import poa.packets.SendPacket;
import poa.poadisplayeditor.PoaDisplayEditor;
import poa.poadisplayeditor.util.NearestEntity;
import poa.poadisplayeditor.util.holders.GUIHolder;
import poa.poalib.Items.CreateItem;
import poa.poalib.shaded.NBT;

import java.util.*;

public class InventoryClick implements Listener {

    public static Map<UUID, Entity> editingMap = new HashMap<>();

    public static Map<UUID, String> easyEditMap = new HashMap<>();


    public static Map<UUID, Display> selectItemMap = new HashMap<>();

    public static Map<UUID, Float> moveAmountMap = new HashMap<>();

    public static Map<UUID, Entity> easyMoveMap = new HashMap<>();

    public static Map<UUID, Entity> pitchMap = new HashMap<>();
    public static Map<UUID, Entity> yawMap = new HashMap<>();

    public static Map<UUID, TextDisplay> appendTextMap = new HashMap<>();
    public static Map<UUID, TextDisplay> newLineTextMap = new HashMap<>();

    public static Map<UUID, TextDisplay> textOpacityMap = new HashMap<>();

    public static Map<UUID, Display> glowMap = new HashMap<>();
    public static Map<UUID, TextDisplay> backgroundMap = new HashMap<>();
    public static Map<UUID, TextDisplay> widthMap = new HashMap<>();

    public static Map<UUID, List<Entity>> entityListMap = new HashMap<>();


    @SneakyThrows
    @EventHandler
    public void invClick(InventoryClickEvent e) {
        final Inventory inventory = e.getInventory();
        if (!(inventory.getHolder() instanceof GUIHolder))
            return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();
        final UUID uuid = player.getUniqueId();

        final ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir())
            return;

        final String[] t = new String[1];
        NBT.get(item, nbt -> {
            t[0] = nbt.getString("PoaType");
        });

        String type = t[0];

        if (!List.of("selectblock", "selecttext", "selectitem", "selecteasy", "selectentity", "spawnblock", "spawntext", "spawnitem").contains(type) && !editingMap.containsKey(uuid)) {
            player.sendRichMessage("<red>Select an entity first");
            return;
        }



        final Entity selectedEntity = editingMap.get(uuid);

        float moveAmount = getMoveAmount(player, e.isRightClick());


        if(easyEditMap.containsKey(uuid) && easyEditMap.get(uuid).equalsIgnoreCase("selecting")){
            easyEditMap.put(uuid, type);
            player.closeInventory();
            player.sendRichMessage("<green>Easy edit mode active.. Left click or right to edit. Crouch click to reopen gui");
        }


        switch (type.toLowerCase()) {
            case "easymodify" -> {
                easyEditMap.put(uuid, "selecting");

                final ItemStack cloned = item.clone();
                final ItemMeta meta = cloned.getItemMeta();
                meta.lore(new ArrayList<>(CreateItem.stringListToComponent(List.of("<green><b>Select the mode you wish to use"))));
                cloned.setItemMeta(meta);

                inventory.setItem(e.getSlot(), cloned);

                player.sendRichMessage("<green>Click the edit mode you wish to use");
                return;
            }
            case "pitch" -> {
                if (e.getClick() == ClickType.MIDDLE) {
                    player.closeInventory();
                    player.sendRichMessage("<green>Type into chat the amount to set the pitch to");
                    pitchMap.put(uuid, selectedEntity);
                    return;
                }
            }
            case "yaw" -> {
                if (e.getClick() == ClickType.MIDDLE) {
                    player.closeInventory();
                    player.sendRichMessage("<green>Type into chat the amount to set the pitch to");
                    yawMap.put(uuid, selectedEntity);
                    return;
                }
            }
        }
        modifyEntity(type, player, selectedEntity, moveAmount, e.isRightClick());

    }


    public static void modifyEntity(String type, Player player, Entity selectedEntity, float moveAmount, boolean isRightClicked) {
        UUID uuid = player.getUniqueId();
        Display selectedDisplay = null;
        if (selectedEntity instanceof Display display)
            selectedDisplay = display;

        switch (type.toLowerCase()) {
            case "selecttext" -> {
                final Entity entity = NearestEntity.getNearestEntityByType(player.getLocation(), TextDisplay.class, 10);
                if (entity == null) {
                    player.sendRichMessage("<red>No Text Display Found");
                    return;
                }

                editingMap.put(uuid, entity);
                player.sendRichMessage("<green>Selected");
            }
            case "selectblock" -> {
                final Entity entity = NearestEntity.getNearestEntityByType(player.getLocation(), BlockDisplay.class, 10);
                if (entity == null) {
                    player.sendRichMessage("<red>No Text Display Found");
                    return;
                }

                editingMap.put(uuid, entity);
                player.sendRichMessage("<green>Selected");
            }
            case "selectitem" -> {
                final Entity entity = NearestEntity.getNearestEntityByType(player.getLocation(), ItemDisplay.class, 10);
                if (entity == null) {
                    player.sendRichMessage("<red>No Item Display Found");
                    return;
                }

                editingMap.put(uuid, entity);
                player.sendRichMessage("<green>Selected");
            }
            case "selectentity" -> {
                final Entity entity = NearestEntity.getNearestEntityByType(player.getLocation(), LivingEntity.class, 10, true);
                if (entity == null) {
                    player.sendRichMessage("<red>No Living Entity Found");
                    return;
                }

                editingMap.put(uuid, entity);
                player.sendRichMessage("<green>Selected " + entity.getType());
                player.sendRichMessage("<red>Using anything other than move, yaw, pitch or scale will throw console errors");
            }

            case "selecteasy" -> {
                final Location location = player.getLocation();
                final Collection<Display> nearby = location.getNearbyEntitiesByType(Display.class, 10);

                if (nearby.isEmpty()) {
                    player.sendRichMessage("<red>No Nearby Display Entities");
                    return;
                }
                final World world = player.getWorld();

                final Collection<Player> nearbyPlayers = location.getNearbyPlayers(200);

                List<Entity> entityList = new ArrayList<>();
                List<Integer> idList = new ArrayList<>();
                for (Display display : nearby) {
                    final ArmorStand armorStand = world.spawn(display.getLocation(), ArmorStand.class, (stand) -> {
                        stand.setCanTick(false);
                        stand.setSmall(true);
                        stand.setInvulnerable(true);
                        stand.setPersistent(false);
                    });
                    entityList.add(armorStand);
                    idList.add(armorStand.getEntityId());

                }
                entityListMap.put(uuid, entityList);

                for (Player p : nearbyPlayers) {
                    if (p == player)
                        continue;
                    SendPacket.sendPacket(p, FakeEntity.removeFakeEntityPacket(idList));
                }


                player.sendRichMessage("<green>All nearby display entities are shown as armor stands, click on it");
            }


            case "spawnblock" -> {
                final Entity entity = player.getWorld().spawn(player.getLocation(), BlockDisplay.class);
                player.sendRichMessage("<green>Successfully spawned block display");
                editingMap.put(uuid, entity);
            }
            case "spawnitem" -> {
                final Entity entity = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);
                player.sendRichMessage("<green>Successfully spawned item display");
                editingMap.put(uuid, entity);
            }
            case "spawntext" -> {
                final Entity entity = player.getWorld().spawn(player.getLocation(), TextDisplay.class);
                player.sendRichMessage("<green>Successfully spawned text display");
                editingMap.put(uuid, entity);
            }

            case "clone" -> {
                final EntitySnapshot snapshot = selectedEntity.createSnapshot();
                final Entity entity = snapshot.createEntity(selectedEntity.getLocation());

                player.sendRichMessage("<green>A direct clone has been spawned in the same position, it is now selected as the modifying display");
                editingMap.put(uuid, entity);

                if (entity instanceof TextDisplay textDisplay)
                    LoadEntity.updateTextForAll(textDisplay, 4);

            }


            case "setblock", "setitem" -> {
                final Entity entity = editingMap.get(uuid);
                if (entity instanceof TextDisplay) {
                    player.sendRichMessage("<red>You do not have a block or item display selectedDisplay");
                    return;
                }

                player.closeInventory();
                player.sendRichMessage("<green>Right Click on a block to set the block display, this will copy the blockdata OR" +
                        " Left Click while holding a block");

                selectItemMap.put(uuid, (Display) entity);
            }

            case "settext" -> {
                if (!(selectedDisplay instanceof TextDisplay)) {
                    player.sendRichMessage("<red>Selected display is not a text display");
                    return;
                }

                player.openInventory(textGui());
            }

            case "move0.01" -> {
                player.sendRichMessage("<green>Set move amount to 0.01");
                moveAmountMap.put(uuid, 0.01F);
            }
            case "move0.1" -> {
                player.sendRichMessage("<green>Set move amount to 0.1");
                moveAmountMap.put(uuid, 0.1F);
            }
            case "move0.5" -> {
                player.sendRichMessage("<green>Set move amount to 0.5");
                moveAmountMap.put(uuid, 0.5F);
            }
            case "move1" -> {
                player.sendRichMessage("<green>Set move amount to 1");
                moveAmountMap.put(uuid, 1F);
            }
            case "move5" -> {
                player.sendRichMessage("<green>Set move amount to 5");
                moveAmountMap.put(uuid, 5F);
            }


            case "movey" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                selectedEntity.teleport(selectedEntity.getLocation().clone().add(0, moveAmount, 0));
                player.sendRichMessage("<green>Moved along Y by " + moveAmount);
            }
            case "movex" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }


                selectedEntity.teleport(selectedEntity.getLocation().clone().add(moveAmount, 0, 0));
                player.sendRichMessage("<green>Moved along X by " + moveAmount);
            }
            case "movez" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                selectedEntity.teleport(selectedEntity.getLocation().clone().add(0, 0, moveAmount));
                player.sendRichMessage("<green>Moved along Z by " + moveAmount);
            }
            case "moveblock" -> {
                if (selectedDisplay != null && !(selectedEntity instanceof TextDisplay))
                    selectedEntity.teleport(selectedEntity.getLocation().getBlock().getLocation());
                else {
                    final Location clone = selectedEntity.getLocation().getBlock().getLocation().clone();
                    clone.add(0.5, 0.5, 0.5);
                    selectedEntity.teleport(clone);
                }
                player.sendRichMessage("<green>Centered to block");
            }
            case "moveeasy" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                player.closeInventory();
                player.sendRichMessage("<green>Use the scroll wheel to move the entity, reopen the gui when completed");
                easyMoveMap.put(uuid, selectedEntity);
            }


            case "pitch" -> {
                //middle click for setting, handled above
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Location clone = selectedEntity.getLocation().clone();
                clone.setPitch(selectedEntity.getPitch() + moveAmount);
                selectedEntity.teleport(clone);
                player.sendRichMessage("<green>Moved pitch by " + moveAmount + " pitch: " + selectedEntity.getPitch());
            }
            case "yaw" -> {
                //middle click for setting, handled above
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Location clone = selectedEntity.getLocation().clone();
                clone.setYaw(selectedEntity.getYaw() + moveAmount);
                selectedEntity.teleport(clone);
                player.sendRichMessage("<green>Moved yaw by " + moveAmount + " yaw: " + selectedEntity.getYaw());
            }


            case "scalex" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyScale(selectedDisplay, new Vector3f(moveAmount, 0, 0));
                selectedDisplay.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended X scale by " + moveAmount);
            }
            case "scaley" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyScale(selectedDisplay, new Vector3f(0, moveAmount, 0));
                selectedDisplay.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Y scale by " + moveAmount);
            }
            case "scalez" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyScale(selectedDisplay, new Vector3f(0, 0, moveAmount));
                selectedDisplay.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Z scale by " + moveAmount);
            }
            case "scaleall" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                if (selectedDisplay != null) {
                    final Transformation newTransform = modifyScale(selectedDisplay, new Vector3f(moveAmount, moveAmount, moveAmount));
                    selectedDisplay.setTransformation(newTransform);
                    player.sendRichMessage("<green>Extended all values scale by " + moveAmount);
                } else {
                    if (!(selectedEntity instanceof LivingEntity li)) {
                        player.sendRichMessage("<red>Selected entity is not a display or living entity");
                        return;
                    }
                    final AttributeInstance attribute = li.getAttribute(Attribute.GENERIC_SCALE);
                    if (attribute == null) {
                        player.sendRichMessage("<red>This entity cannot be scaled");
                        return;
                    }
                    attribute.setBaseValue(attribute.getValue() + moveAmount);
                }
            }


            case "translationx" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyTranslation(selectedDisplay, new Vector3f(moveAmount, 0, 0));
                selectedDisplay.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended X translation by " + moveAmount);
            }
            case "translationy" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyTranslation(selectedDisplay, new Vector3f(0, moveAmount, 0));
                selectedDisplay.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Y translation by " + moveAmount);
            }
            case "translationz" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyTranslation(selectedDisplay, new Vector3f(0, 0, moveAmount));
                selectedDisplay.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Z translation by " + moveAmount);
            }


            case "brightness" -> {
                final Display.Brightness brightness = selectedDisplay.getBrightness();
                int amount = 15;
                if (brightness != null)
                    amount = brightness.getBlockLight();

                if (isRightClicked)
                    amount--;
                else
                    amount++;

                if (amount > 15)
                    amount = 15;
                else if (amount < 0)
                    amount = 0;

                selectedDisplay.setBrightness(new Display.Brightness(amount, amount));

                player.sendRichMessage("<green>Modified brightness to " + amount);
            }


            case "billboardfixed" -> {
                selectedDisplay.setBillboard(Display.Billboard.FIXED);
                player.sendRichMessage("<green>Set Billboard to Fixed");
            }
            case "billboardvertical" -> {
                selectedDisplay.setBillboard(Display.Billboard.VERTICAL);
                player.sendRichMessage("<green>Set Billboard to Vertical");
            }
            case "billboardhorizontal" -> {
                selectedDisplay.setBillboard(Display.Billboard.HORIZONTAL);
                player.sendRichMessage("<green>Set Billboard to Horizontal");
            }
            case "billboardcenter" -> {
                selectedDisplay.setBillboard(Display.Billboard.CENTER);
                player.sendRichMessage("<green>Set Billboard to Center");
            }

            case "delete" -> {
                selectedEntity.remove();
                player.sendRichMessage("<green>Display Deleted :(");
            }

            case "glowoverride" -> {
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the R G B for the glow, eg, 255 100 255");
                glowMap.put(uuid, selectedDisplay);
            }
            case "glow" -> {
                player.closeInventory();
                selectedDisplay.setGlowing(!selectedDisplay.isGlowing());
                player.sendRichMessage("<green>Toggled glowing state");
            }


            case "cleartext" -> {
                ((TextDisplay) selectedDisplay).text(null);
                player.sendRichMessage("<green>Cleared all text");
            }
            case "appendline" -> {
                final TextDisplay textDisplay = (TextDisplay) selectedDisplay;
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the text to append, MiniMessages supported");
                appendTextMap.put(uuid, textDisplay);
            }

            case "addline" -> {
                final TextDisplay textDisplay = (TextDisplay) selectedDisplay;
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the text to add to a new line, MiniMessages supported");
                newLineTextMap.put(uuid, textDisplay);
            }

            case "opacity" -> {
                TextDisplay textDisplay = (TextDisplay) selectedDisplay;
                player.closeInventory();

                player.sendRichMessage("<green>In chat type a number between 0 and 255 for text opacity OR -1 for full");

                textOpacityMap.put(uuid, textDisplay);

            }

            case "background" -> {
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the R G B A for the background, eg, 255 100 255 255 <gray>The alpha is optional");
                backgroundMap.put(uuid, (TextDisplay) selectedDisplay);
            }

            case "width" -> {
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the line width, default is 200");
                widthMap.put(uuid, (TextDisplay) selectedDisplay);
            }

            case "alignleft" -> {
                final TextDisplay textDisplay = (TextDisplay) selectedDisplay;

                textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
                player.sendRichMessage("<green>Set alignment left");
            }
            case "aligncenter" -> {
                final TextDisplay textDisplay = (TextDisplay) selectedDisplay;

                textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                player.sendRichMessage("<green>Set alignment center");
            }
            case "alignright" -> {
                final TextDisplay textDisplay = (TextDisplay) selectedDisplay;

                textDisplay.setAlignment(TextDisplay.TextAlignment.RIGHT);
                player.sendRichMessage("<green>Set alignment right");
            }

        }
    }


    private static Inventory textGui() {
        Inventory inventory = Bukkit.createInventory(new GUIHolder(), InventoryType.DROPPER, MiniMessage.miniMessage().deserialize("<gold>Text"));

        inventory.setItem(0, Click.inventoryItem(Material.RED_STAINED_GLASS_PANE, "<red>Clear Text", "cleartext"));
        inventory.setItem(1, Click.inventoryItem(Material.YELLOW_STAINED_GLASS_PANE, "<yellow>Append Text", "appendline"));
        inventory.setItem(2, Click.inventoryItem(Material.LIME_STAINED_GLASS_PANE, "<green>Add Line", "addline"));

        inventory.setItem(3, Click.inventoryItem(Material.GLASS_BOTTLE, "<green>Opacity", "opacity"));
        inventory.setItem(4, Click.inventoryItem(Material.INK_SAC, "<green>Line Width", "width"));
        inventory.setItem(5, Click.inventoryItem(Material.OAK_LEAVES, "<green>Background Color", "background"));

        inventory.setItem(6, Click.inventoryItem(Material.BLUE_STAINED_GLASS_PANE, "<green>Align Left", "alignleft"));
        inventory.setItem(7, Click.inventoryItem(Material.CYAN_STAINED_GLASS_PANE, "<green>  Align Center  ", "aligncenter"));
        inventory.setItem(8, Click.inventoryItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "<green>    Align Right", "alignright"));
        return inventory;
    }


    public static String getTextComponentText(TextDisplay textDisplay) {
        final Component text = textDisplay.text();

        return MiniMessage.miniMessage().serialize(text);
    }


    private static @NotNull Transformation translationModify(Display selected, Vector3f modifyScaleBy, Vector3f modifyTranslationBy) {
        Transformation oldTransform = selected.getTransformation();
        Vector3f translation = oldTransform.getTranslation();
        if (modifyTranslationBy != null)
            translation.add(modifyTranslationBy);

        final Vector3f scale = oldTransform.getScale();
        if (modifyScaleBy != null)
            scale.add(modifyScaleBy);


        Quaternionf leftRotation = oldTransform.getLeftRotation();


        Quaternionf rightRotation = oldTransform.getRightRotation();
        return new Transformation(translation, leftRotation, scale, rightRotation);
    }

    private static @NotNull Transformation modifyTranslation(Display selected, Vector3f modifyTranslationBy) {
        return translationModify(selected, null, modifyTranslationBy);
    }

    private static Transformation modifyScale(Display selected, Vector3f modifyScaleBy) {
        return translationModify(selected, modifyScaleBy, null);
    }


    public static float getMoveAmount(Player player, boolean rightClick) {
        final UUID uuid = player.getUniqueId();
        if (!moveAmountMap.containsKey(uuid))
            return 0;


        Float move = moveAmountMap.get(uuid);
        if (rightClick)
            move = move * -1;

        return move;
    }

}
