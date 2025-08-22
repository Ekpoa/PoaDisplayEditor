package poa.poadisplayeditor.structure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import poa.poadisplayeditor.structure.Data;
import poa.poadisplayeditor.structure.DisplayStructure;
import poa.poalib.tabcomplete.EasyTabComplete;

import java.util.List;

public class DisplayStructureCMD implements CommandExecutor, TabCompleter {



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player))
            return false;

        if(args.length == 0){
            player.sendRichMessage("<red>/displaystructure <pos1/pos2/save/paste>");
            return false;
        }

        switch (args[0].toLowerCase()){
            case "pos1" -> {
                Data.pos1.put(player.getUniqueId(), player.getLocation());
                player.sendRichMessage("<green>Pos1 set at your location");
            }
            case "pos2" -> {
                Data.pos2.put(player.getUniqueId(), player.getLocation());
                player.sendRichMessage("<green>Pos2 set at your location");
            }

            case "save" -> {
                if(args.length == 1){
                    player.sendRichMessage("<red>/displaystructure save <id>");
                    return false;
                }

                Data.save(player, args[1], true);
                player.sendRichMessage("<green>Saved as " + args[1]);
            }
            case "paste" -> {
                if(args.length == 1){
                    player.sendRichMessage("<red>/displaystructure save <id>");
                    return false;
                }
                final boolean b = DisplayStructure.spawnStructure(args[1], player.getLocation());

                if(!b){
                    player.sendRichMessage("<red>Failed to paste. Incorrect id?");
                    return false;
                }
                player.sendRichMessage("<green>Pasted structure");
            }

        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return EasyTabComplete.correctTabComplete(args[0], "pos1", "pos2", "save", "paste");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("paste")){
            return EasyTabComplete.correctTabComplete(args[1], DisplayStructure.allIds);
        }

        return List.of();
    }
}
