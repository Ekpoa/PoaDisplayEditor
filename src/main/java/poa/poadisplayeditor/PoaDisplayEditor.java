package poa.poadisplayeditor;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import poa.poadisplayeditor.events.*;

public final class PoaDisplayEditor extends JavaPlugin {

    @Getter
    public static PoaDisplayEditor INSTANCE;

    @Getter
    public static Material item;


    @Override
    public void onEnable() {
        INSTANCE = this;

        saveDefaultConfig();

        item = Material.valueOf(getConfig().getString("Item").toUpperCase());


        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Click(), this);
        pm.registerEvents(new InventoryClick(), this);
        pm.registerEvents(new ClickBlockItem(), this);
        pm.registerEvents(new Chat(), this);
        pm.registerEvents(new LoadEntity(), this);
        pm.registerEvents(new EntityClick(), this);
        pm.registerEvents(new ScrollEntity(), this);
        pm.registerEvents(new InvOpen(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
