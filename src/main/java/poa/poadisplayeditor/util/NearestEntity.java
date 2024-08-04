package poa.poadisplayeditor.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NearestEntity {

    public static <T extends Entity> T getNearestEntityByType(Location location, Class<T> entityType, double radius) {
        double closestDistance = Double.MAX_VALUE;
        T closestEntity = null;

        for (Entity entity : location.getNearbyEntities(radius, radius, radius)) {
            if (entityType.isInstance(entity)) {
                double distance = location.distance(entity.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entityType.cast(entity);
                }
            }
        }

        return closestEntity;
    }
}
