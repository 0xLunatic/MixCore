package mix.core.EventListener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import mix.core.Main;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class WishingWellListener implements Listener {
    private final Main plugin;
    private final Map<Player, Long> cooldownWish = new HashMap<>();
    private final Map<String, Integer> pityMap;
    public WishingWellListener(Main plugin) {
        this.plugin = plugin;
        this.pityMap = new HashMap<>();
    }

    @EventHandler
    public void onWishThrow(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isOnRegion(player, "wishingwell")) {
            if (droppedItem.hasItemMeta()) {
                ItemMeta itemMeta = droppedItem.getItemMeta();
                if (itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals("Token")) {
                    if (!cooldownWish.containsKey(player)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Item item = event.getItemDrop();
                                Location dropLocation = item.getLocation();
                                Material blockTypeBelow = dropLocation.getBlock().getRelative(0, 0, 0).getType();
                                if (blockTypeBelow == Material.WATER) {
                                    item.remove();
                                    plugin.getDataManager().addPity(player.getName(), 1);
                                    player.sendMessage( ChatColor.GREEN + "The well received your coin with pleasure!");
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0, 20);

                        cooldownWish.put(player, System.currentTimeMillis() + 3000);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            cooldownWish.remove(player);
                        }, 3000 / 50);
                    } else {
                        player.sendMessage(ChatColor.RED + "Calm down, don't waste your coin too fast..");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public void wishReward() {

    }

    private boolean isOnRegion(Player player, String regionTarget) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        for (ProtectedRegion region : set) {
            if (region.getId().contains(regionTarget)) {
                return true;
            }
        }
        return false;
    }
}
