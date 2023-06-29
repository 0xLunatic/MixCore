package mix.core.EventListener;

import mix.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InteractListener implements Listener {
    private final Main plugin;
    private final Map<Player, Long> cooldownMode = new HashMap<>();
    public InteractListener(Main plugin) {
        this.plugin = plugin;
    }

    ////////// ELEMENTALIST BLADE //////////
    @EventHandler
    public void elementalistMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        ItemStack blade = player.getInventory().getItemInMainHand();
        if (blade.getType() != Material.DIAMOND_SWORD || !blade.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = blade.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasLore()) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.size() <= 6) {
            return;
        }

        if (hasLoreContaining(lore, "Active Element:")) {
            int loreLine = findLoreContaining(lore, "Active Element:");
            String element = getLoreLineValue(lore, loreLine);
            assert element != null;

            if (cooldownMode.containsKey(player)) {
                long currentTime = System.currentTimeMillis();
                long cooldownEnd = cooldownMode.get(player);
                if (currentTime < cooldownEnd) {
                    return;
                }
            }

            switch (element) {
                case "§8Water":
                    lore.set(loreLine, ChatColor.WHITE + "Active Element: " + ChatColor.DARK_GRAY + "Fire");
                    player.sendActionBar(ChatColor.GREEN + "Active Element: " + ChatColor.RED + "Fire");
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 10, 2);
                    break;
                case "§8Fire":
                    lore.set(loreLine, ChatColor.WHITE + "Active Element: " + ChatColor.DARK_GRAY + "Ice");
                    player.sendActionBar(ChatColor.GREEN + "Active Element: " + ChatColor.AQUA + "Ice");
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 10, 2);
                    break;
                case "§8Ice":
                    lore.set(loreLine, ChatColor.WHITE + "Active Element: " + ChatColor.DARK_GRAY + "Water");
                    player.sendActionBar(ChatColor.GREEN + "Active Element: " + ChatColor.BLUE + "Water");
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 10, 2);
                    break;
            }

            itemMeta.setLore(lore);
            blade.setItemMeta(itemMeta);
            player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 50, 5);

            long cooldownEnd = System.currentTimeMillis() + 3000;
            cooldownMode.put(player, cooldownEnd);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cooldownMode.remove(player);
            }, 3000 / 50);

        }
    }

    @EventHandler
    public void elementalistBlade(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        double maxHealth = victim.getMaxHealth();
        double health = victim.getHealth();

        ItemStack blade = victim.getInventory().getItemInMainHand();
        if (blade.getType() != Material.DIAMOND_SWORD || !blade.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = blade.getItemMeta();
        assert itemMeta != null;
        if (!itemMeta.hasLore()) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            return;
        }

        if (hasLoreContaining(lore, "Active Element: Water")) {
            if (health < (0.1 * maxHealth)) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 2));
            }
        }
    }

    private int findLoreContaining(List<String> lore, String searchString) {
        if (lore == null || searchString == null) {
            return -1;
        }
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.contains(searchString)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasLoreContaining(List<String> lore, String searchString) {
        if (lore == null || searchString == null) {
            return false;
        }
        for (String line : lore) {
            if (line.contains(searchString)) {
                return true;
            }
        }
        return false;
    }

    private String getLoreLineValue(List<String> lore, int lineNumber) {
        if (lineNumber <= 0 || lineNumber > Objects.requireNonNull(lore).size()) {
            return null;
        }
        String line = lore.get(lineNumber);
        String[] parts = line.split(": ");
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        return null;
    }
}
