package poa.poadisplayeditor.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import poa.poadisplayeditor.PoaDisplayEditor;
import poa.poadisplayeditor.events.InventoryClick;

public class PreciseMove {


    public static void preciseMove(Player player, Entity entity) {
        if (player == null || entity == null) return;
        if (!player.isOnline()) return;
        if (player.getWorld() != entity.getWorld()) return;

        // Use the current player→entity distance as the lock distance
        final double lockDistance = Math.max(
                0.25,
                player.getEyeLocation().distance(entity.getLocation())
        );

        // Run every tick; stop when either becomes invalid or changes world
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()
                        || entity.isDead()
                        || !entity.isValid()
                        || player.getWorld() != entity.getWorld()
                        || !InventoryClick.precisionMoveMap.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                Location eye = player.getEyeLocation();
                Vector dir = eye.getDirection().normalize();

                double targetDist = lockDistance;

                // If there's a block in the way, place the entity just before it
                RayTraceResult hit = player.getWorld().rayTraceBlocks(
                        eye, dir, lockDistance, FluidCollisionMode.NEVER, true
                );
                if (hit != null) {
                    double hitDist = eye.distance(hit.getHitPosition().toLocation(player.getWorld()));
                    targetDist = Math.max(0.25, hitDist - 0.30); // keep a small gap from the surface
                }

                Location target = eye.clone().add(dir.multiply(targetDist));

                // Face the same way as the player
                target.setYaw(player.getLocation().getYaw());
                target.setPitch(player.getLocation().getPitch());

                // Slight vertical tweak for living entities so they sit "on" the line comfortably
                if (entity instanceof LivingEntity le) {
                    target.subtract(0, le.getHeight() * 0.5, 0);
                }

                // Teleport smoothly; Paper has teleportAsync but plain teleport works everywhere
                entity.teleport(target);
            }
        }.runTaskTimer(PoaDisplayEditor.getINSTANCE(), 0L, 1L);
    }


}
