package poa.poadisplayeditor.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NearestEntity {

    public static <T extends Entity> T getNearestEntityByType(Location location, Class<T> entityType, double radius, boolean ignorePlayer) {
        double closestDistance = Double.MAX_VALUE;
        T closestEntity = null;

        for (Entity entity : location.getNearbyEntities(radius, radius, radius)) {
            if(entity instanceof Player && ignorePlayer)
                continue;
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

    public static <T extends Entity> T getNearestEntityByType(Location location, Class<T> entityType, double radius){
        return getNearestEntityByType(location, entityType, radius, false);
    }
}
