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

        final double lockDistance = Math.max(
                0.25,
                player.getEyeLocation().distance(entity.getLocation())
        );

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

                RayTraceResult hit = player.getWorld().rayTraceBlocks(
                        eye, dir, lockDistance, FluidCollisionMode.NEVER, true
                );
                if (hit != null) {
                    double hitDist = eye.distance(hit.getHitPosition().toLocation(player.getWorld()));
                    targetDist = Math.max(0.25, hitDist - 0.30);
                }

                // Preserve entity's current orientation
                float eYaw = entity.getLocation().getYaw();
                float ePitch = entity.getLocation().getPitch();

                Location target = eye.clone().add(dir.multiply(targetDist));
                target.setYaw(eYaw);
                target.setPitch(ePitch);

                if (entity instanceof LivingEntity le) {
                    target.subtract(0, le.getHeight() * 0.5, 0);
                }

                entity.teleport(target);
            }
        }.runTaskTimer(PoaDisplayEditor.getINSTANCE(), 0L, 1L);
    }



}
