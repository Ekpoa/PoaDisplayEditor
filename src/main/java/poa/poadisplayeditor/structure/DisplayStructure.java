package poa.poadisplayeditor.structure;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.util.Vector;
import poa.poadisplayeditor.PoaDisplayEditor;
import poa.poalib.yml.PoaYaml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

public class DisplayStructure {

    public static Map<String, DisplayStructure> dataMap = new HashMap<>();

    private static final File file = new File(PoaDisplayEditor.getINSTANCE().getDataFolder(), "structures.yml");
    private static final PoaYaml yml = PoaYaml.loadFromFile(file, true);

    public static List<String> allIds = new ArrayList<>();

    static {
        if(yml.isConfigurationSection("Structures")){
            allIds = yml.getConfigurationSection("Structures").getKeys(false).stream().toList();
        }
    }


    String id;
    @Getter
    Map<EntitySnapshot, Vector> offsetMap;


    public DisplayStructure(String id, Map<EntitySnapshot, Vector> offsetMap){
        this.id = id.toLowerCase();
        this.offsetMap = offsetMap;
        dataMap.put(this.id, this);
    }


    public void save(){
        final String path = "Structures." + this.id + ".";

        int i = 0;
        for (Map.Entry<EntitySnapshot, Vector> entry : offsetMap.entrySet()) {
            final EntitySnapshot display = entry.getKey();
            final Vector offset = entry.getValue();
            final double x = offset.getX();
            final double y = offset.getY();
            final double z = offset.getZ();

            final String displayString = display.getAsString();
            String encoded = Base64.getEncoder().encodeToString(displayString.getBytes(StandardCharsets.UTF_8));

            yml.set(path + i + ".entity", encoded);
            yml.set(path + i + ".x", x);
            yml.set(path + i + ".y", y);
            yml.set(path + i + ".z", z);

            i++;
        }
        yml.saveAsync(file);
    }


    public static boolean spawnStructure(String id, Location location){
        id = id.toLowerCase();

        DisplayStructure structure;
        if(dataMap.containsKey(id))
            structure = dataMap.get(id);
        else
            structure = loadStructure(id);

        if(structure == null) {
            PoaDisplayEditor.getINSTANCE().getLogger().log(Level.WARNING, "structure not found with id " + id);
            return false;
        }

        spawnFromSnapshots(location, structure.getOffsetMap());
        return true;
    }

    private static DisplayStructure loadStructure(String id){
        id = id.toLowerCase();

        if(!yml.isConfigurationSection("Structures." + id))
            return null;

        Map<EntitySnapshot, Vector> map = new HashMap<>();

        for (String displayID : yml.getConfigurationSection("Structures." + id).getKeys(false)) {
            String path = "Structures." + id + "." + displayID;
            final String encoded = yml.getString(path + ".entity");
            final String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            final EntitySnapshot snapshot = Bukkit.getEntityFactory().createEntitySnapshot(decoded);

            final Vector offset = new Vector(yml.getDouble(path + ".x"), yml.getDouble(path + ".y"), yml.getDouble(path + ".z"));

            map.put(snapshot, offset);
        }

        return new DisplayStructure(id, map);
    }


    private static List<Display> spawnFromSnapshots(Location base, Map<EntitySnapshot, Vector> blueprint) {
        List<Display> spawned = new ArrayList<>();
        if (base == null || base.getWorld() == null || blueprint == null) return spawned;

        World w = base.getWorld();

        for (Map.Entry<EntitySnapshot, Vector> entry : blueprint.entrySet()) {
            Location at = base.clone().add(entry.getValue());
            final EntitySnapshot key = entry.getKey();
            final Entity ent = key.createEntity(at);
            if (ent instanceof Display display) {
                spawned.add(display);
            }
        }
        return spawned;
    }




}
