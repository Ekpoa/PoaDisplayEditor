package poa.poadisplayeditor.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import poa.poadisplayeditor.PoaDisplayEditor;
import poa.poadisplayeditor.util.holders.GUIHolder;

import java.util.*;

public class ScrollEntity implements Listener {

    // Keep your existing fields:
    private static final Map<UUID, Integer> SCROLL_ACCUM = new HashMap<>();
    private static final Map<UUID, BukkitTask> DEBOUNCE_TASK = new HashMap<>();

    @EventHandler
    public void onPlayerScroll(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        final Entity selectedEntity = InventoryClick.easyMoveMap.get(uuid);
        if (selectedEntity == null || !selectedEntity.isValid() || selectedEntity.isDead()) return;

        // Stop the hotbar changing client-side
        event.setCancelled(true);
        player.getInventory().setHeldItemSlot(event.getPreviousSlot());

        final Float move = InventoryClick.moveAmountMap.get(uuid);
        if (move == null || move == 0f) return;

        // Direction-preserving step count: (no -4..+4 minimalization)
        int steps = directionalSteps(event.getPreviousSlot(), event.getNewSlot());
        if (steps == 0) return;

        // Accumulate all steps for this swipe
        SCROLL_ACCUM.merge(uuid, steps, Integer::sum);

        // Debounce: coalesce rapid events into one movement
        BukkitTask old = DEBOUNCE_TASK.remove(uuid);
        if (old != null) old.cancel();

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                DEBOUNCE_TASK.remove(uuid);

                Integer pending = SCROLL_ACCUM.remove(uuid);
                if (pending == null || pending == 0) return;

                if (!player.isOnline()
                        || !selectedEntity.isValid()
                        || selectedEntity.isDead()
                        || player.getWorld() != selectedEntity.getWorld()
                        || !InventoryClick.easyMoveMap.containsKey(uuid)) {
                    return;
                }

                Location eye = player.getEyeLocation();
                Vector dir = eye.getDirection().normalize();

                double distance = move * pending; // pending may be negative
                Location newLoc = selectedEntity.getLocation().add(dir.multiply(distance));
                selectedEntity.teleport(newLoc);
            }
        }.runTaskLater(PoaDisplayEditor.getINSTANCE(), 2L); // tweak 1–3 ticks if desired

        DEBOUNCE_TASK.put(uuid, task);
    }

    /**
     * Compute signed steps following the *actual scroll direction*.
     * Positive = forward (increasing index, wrapping 8->0 as +1)
     * Negative = backward (decreasing index, wrapping 0->8 as -1)
     */
    private static int directionalSteps(int from, int to) {
        // raw difference in [-8..+8]
        int raw = to - from;

        // resolve wrap so the sign reflects physical direction
        if (raw == 8)  return -1; // 0 <- 8 (backward one)
        if (raw == -8) return  1; // 8 -> 0 (forward one)

        int sign = Integer.signum(raw);
        if (sign == 0) return 0;

        // steps strictly in that direction around the ring
        int forward = (to - from + 9) % 9;    // 0..8 clockwise
        int backward = (from - to + 9) % 9;   // 0..8 counter-clockwise
        return sign > 0 ? forward : -backward;
    }




}
