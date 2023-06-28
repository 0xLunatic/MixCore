package mix.core.EventListener;

import mix.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Random;

public class WheatListener implements Listener {
    private Main plugin;
    private Random random = new Random(600);

    public WheatListener(Main plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void enchantedWheatEvent(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Check if the block broken is wheat in the "spawn" world
        if (block.getType() == Material.WHEAT && block.getWorld().getName().equals("spawn")) {

            // Check if it's the last growth stage of the wheat
            if (block.getBlockData() instanceof org.bukkit.block.data.Ageable) {
                org.bukkit.block.data.Ageable ageable = (org.bukkit.block.data.Ageable) block.getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {

                    // Random chance to execute the command
                    if (random.nextDouble() <= 0.05) { // 5% chance
                        String playerName = event.getPlayer().getName();
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                "mi give MATERIAL ENCHANTED_WHEAT " + playerName + " 1");
                    }
                }
            }
        }
    }
}
