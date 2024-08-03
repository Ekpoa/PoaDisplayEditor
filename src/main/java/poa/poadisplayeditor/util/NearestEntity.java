package poa.poadisplayeditor.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NearestEntity {

    public static <T extends Entity> T getNearestEntityByType(Player player, Class<T> entityType, double radius) {
        Location playerLocation = player.getLocation();
        double closestDistance = Double.MAX_VALUE;
        T closestEntity = null;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entityType.isInstance(entity)) {
                double distance = playerLocation.distance(entity.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entityType.cast(entity);
                }
            }
        }

        return closestEntity;
    }
}
