package poa.poadisplayeditor.events;

import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import poa.packets.FakeEntity;
import poa.packets.SendPacket;
import poa.poadisplayeditor.util.NearestEntity;
import poa.poadisplayeditor.util.holders.GUIHolder;
import poa.poalib.shaded.NBT;

import java.util.*;

public class InventoryClick implements Listener {

    public static Map<UUID, Entity> editingMap = new HashMap<>();


    public static Map<UUID, Display> selectItemMap = new HashMap<>();

    public static Map<UUID, Float> moveAmountMap = new HashMap<>();

    public static Map<UUID, Display> easyMoveMap = new HashMap<>();

    public static Map<UUID, Display> pitchMap = new HashMap<>();
    public static Map<UUID, Display> yawMap = new HashMap<>();

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
        if (!(e.getInventory().getHolder() instanceof GUIHolder))
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

        if (!List.of("selectblock", "selecttext", "selectitem", "selecteasy").contains(type) && !editingMap.containsKey(uuid)) {
            player.sendRichMessage("<red>Select an entity first");
            return;
        }

        Display selected = (Display) editingMap.get(uuid);
        float moveAmount = getMoveAmount(player, e.isRightClick());

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
                    if(p == player)
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
                player.sendRichMessage("<green>Successfully spawned block display");
                editingMap.put(uuid, entity);
            }
            case "spawntext" -> {
                final Entity entity = player.getWorld().spawn(player.getLocation(), TextDisplay.class);
                player.sendRichMessage("<green>Successfully spawned block display");
                editingMap.put(uuid, entity);
            }

            case "setblock", "setitem" -> {
                final Entity entity = editingMap.get(uuid);
                if (entity instanceof TextDisplay) {
                    player.sendRichMessage("<red>You do not have a block or item display selected");
                    return;
                }

                player.closeInventory();
                player.sendRichMessage("<green>Right Click on a block to set the block display, this will copy the blockdata OR" +
                        " Left Click while holding a block");

                selectItemMap.put(uuid, (Display) entity);
            }

            case "settext" -> {
                if (!(selected instanceof TextDisplay)) {
                    player.sendRichMessage("<red>Selected display is not a text display");
                    return;
                }

                player.openInventory(textGui());
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

                selected.teleport(selected.getLocation().clone().add(0, moveAmount, 0));
                player.sendRichMessage("<green>Moved along Y by " + moveAmount);
            }
            case "movex" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }


                selected.teleport(selected.getLocation().clone().add(moveAmount, 0, 0));
                player.sendRichMessage("<green>Moved along X by " + moveAmount);
            }
            case "movez" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                selected.teleport(selected.getLocation().clone().add(0, 0, moveAmount));
                player.sendRichMessage("<green>Moved along Z by " + moveAmount);
            }
            case "moveblock" -> {
                selected.teleport(selected.getLocation().getBlock().getLocation());
                player.sendRichMessage("<green>Centered to block");
            }
            case "moveeasy" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                player.closeInventory();
                player.sendRichMessage("<green>Use the scroll wheel to move the entity, reopen the gui when completed");
                easyMoveMap.put(uuid, selected);
            }


            case "pitch" -> {
                if (e.getClick() == ClickType.MIDDLE) {
                    player.closeInventory();
                    player.sendRichMessage("<green>Type into chat the amount to set the pitch to");
                    pitchMap.put(uuid, selected);
                    return;
                }


                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Location clone = selected.getLocation().clone();
                clone.setPitch(selected.getPitch() + moveAmount);
                selected.teleport(clone);
                player.sendRichMessage("<green>Moved pitch by " + moveAmount + " pitch: " + selected.getPitch());
            }
            case "yaw" -> {
                if (e.getClick() == ClickType.MIDDLE) {
                    player.closeInventory();
                    player.sendRichMessage("<green>Type into chat the amount to set the pitch to");
                    yawMap.put(uuid, selected);
                    return;
                }


                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Location clone = selected.getLocation().clone();
                clone.setYaw(selected.getYaw() + moveAmount);
                selected.teleport(clone);
                player.sendRichMessage("<green>Moved yaw by " + moveAmount + " yaw: " + selected.getYaw());
            }


            case "scalex" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyScale(selected, new Vector3f(moveAmount, 0, 0));
                selected.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended X scale by " + moveAmount);
            }
            case "scaley" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyScale(selected, new Vector3f(0, moveAmount, 0));
                selected.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Y scale by " + moveAmount);
            }
            case "scalez" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyScale(selected, new Vector3f(0, 0, moveAmount));
                selected.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Z scale by " + moveAmount);
            }


            case "translationx" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyTranslation(selected, new Vector3f(moveAmount, 0, 0));
                selected.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended X translation by " + moveAmount);
            }
            case "translationy" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyTranslation(selected, new Vector3f(0, moveAmount, 0));
                selected.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Y translation by " + moveAmount);
            }
            case "translationz" -> {
                if (moveAmount == 0) {
                    player.sendRichMessage("<red>You must select a move amount first");
                    return;
                }

                final Transformation newTransform = modifyTranslation(selected, new Vector3f(0, 0, moveAmount));
                selected.setTransformation(newTransform);
                player.sendRichMessage("<green>Extended Z translation by " + moveAmount);
            }


            case "brightness" -> {
                final Display.Brightness brightness = selected.getBrightness();
                int amount = 15;
                if (brightness != null)
                    amount = brightness.getBlockLight();

                if (e.isRightClick())
                    amount--;
                else
                    amount++;

                if (amount > 15)
                    amount = 15;
                else if (amount < 0)
                    amount = 0;

                selected.setBrightness(new Display.Brightness(amount, amount));

                player.sendRichMessage("<green>Modified brightness to " + amount);
            }


            case "billboardfixed" -> {
                selected.setBillboard(Display.Billboard.FIXED);
                player.sendRichMessage("<green>Set Billboard to Fixed");
            }
            case "billboardvertical" -> {
                selected.setBillboard(Display.Billboard.VERTICAL);
                player.sendRichMessage("<green>Set Billboard to Vertical");
            }
            case "billboardhorizontal" -> {
                selected.setBillboard(Display.Billboard.HORIZONTAL);
                player.sendRichMessage("<green>Set Billboard to Horizontal");
            }
            case "billboardcenter" -> {
                selected.setBillboard(Display.Billboard.CENTER);
                player.sendRichMessage("<green>Set Billboard to Center");
            }

            case "delete" -> {
                selected.remove();
                player.sendRichMessage("<green>Display Deleted :(");
            }

            case "glowoverride" -> {
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the R G B for the glow, eg, 255 100 255");
                glowMap.put(uuid, selected);
            }
            case "glow" -> {
                player.closeInventory();
                selected.setGlowing(!selected.isGlowing());
                player.sendRichMessage("<green>Toggled glowing state");
            }


            case "cleartext" -> {
                ((TextDisplay) selected).text(null);
                player.sendRichMessage("<green>Cleared all text");
            }
            case "appendline" -> {
                final TextDisplay textDisplay = (TextDisplay) selected;
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the text to append, MiniMessages supported");
                appendTextMap.put(uuid, textDisplay);
            }

            case "addline" -> {
                final TextDisplay textDisplay = (TextDisplay) selected;
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the text to add to a new line, MiniMessages supported");
                newLineTextMap.put(uuid, textDisplay);
            }

            case "opacity" -> {
                TextDisplay textDisplay = (TextDisplay) selected;
                player.closeInventory();

                player.sendRichMessage("<green>In chat type a number between 0 and 255 for text opacity OR -1 for full");

                textOpacityMap.put(uuid, textDisplay);

            }

            case "background" -> {
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the R G B for the background, eg, 255 100 255");
                backgroundMap.put(uuid, (TextDisplay) selected);
            }

            case "width" -> {
                player.closeInventory();

                player.sendRichMessage("<green>Type in chat the line width, default is 200");
                widthMap.put(uuid, (TextDisplay) selected);
            }

            case "alignleft" -> {
                final TextDisplay textDisplay = (TextDisplay) selected;

                textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
                player.sendRichMessage("<green>Set alignment left");
            }
            case "aligncenter" -> {
                final TextDisplay textDisplay = (TextDisplay) selected;

                textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                player.sendRichMessage("<green>Set alignment center");
            }
            case "alignright" -> {
                final TextDisplay textDisplay = (TextDisplay) selected;

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


    private static float getMoveAmount(Player player, boolean rightClick) {
        final UUID uuid = player.getUniqueId();
        if (!moveAmountMap.containsKey(uuid))
            return 0;


        Float move = moveAmountMap.get(uuid);
        if (rightClick)
            move = move * -1;

        return move;
    }

}
