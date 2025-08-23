package poa.poadisplayeditor.structure;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;


public class Data {

    public static Map<UUID, Location> pos1 = new HashMap<>();
    public static Map<UUID, Location> pos2 = new HashMap<>();





    public static void save(Player player, String id, boolean saveToFile){
        final UUID uuid = player.getUniqueId();
        id = id.toLowerCase();

        if(!pos1.containsKey(uuid)) {
            player.sendRichMessage("<red>Pos1 not set");
            return;
        }
        else if(!pos2.containsKey(uuid)) {
            player.sendRichMessage("<red>Pos2 not set");
            return;
        }

        final List<Display> displays = getDisplaysBetween(pos1.get(uuid), pos2.get(uuid));

        if(displays.isEmpty()){
            player.sendRichMessage("<red>There are no display entities within your positions");
            return;
        }


        final DisplayStructure structure = new DisplayStructure(id, snapshotDisplays(player.getLocation(), displays));

        if(saveToFile)
            structure.save();

        player.sendRichMessage("<green>Saved as " + id);
    }

    // Create snapshots of all display entities relative to an origin
    public static Map<EntitySnapshot, Vector> snapshotDisplays(Location origin, List<Display> displays) {
        Map<EntitySnapshot, Vector> map = new LinkedHashMap<>();
        if (origin == null || origin.getWorld() == null) return map;

        Vector originVec = origin.toVector();
        World world = origin.getWorld();

        for (Display d : displays) {
            if (d == null || !d.isValid() || d.isDead()) continue;
            if (!world.equals(d.getWorld())) continue;

            Vector offset = d.getLocation().toVector().subtract(originVec);
            map.put(d.createSnapshot(), offset); // Paper API
        }
        return map;
    }



    private static List<Display> getDisplaysBetween(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() == null || loc2.getWorld() == null) {
            return Collections.emptyList();
        }

        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return Collections.emptyList();
        }

        World world = loc1.getWorld();

        double minX = Math.min(loc1.getX(), loc2.getX());
        double minY = Math.min(loc1.getY(), loc2.getY());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());
        double maxX = Math.max(loc1.getX(), loc2.getX());
        double maxY = Math.max(loc1.getY(), loc2.getY());
        double maxZ = Math.max(loc1.getZ(), loc2.getZ());

        BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);

        return world.getNearbyEntities(box, entity -> entity instanceof Display)
                .stream()
                .map(entity -> (Display) entity)
                .toList();
    }


}
