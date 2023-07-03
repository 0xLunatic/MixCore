package mix.core.EventListener;

import mix.core.Main;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InteractListener implements Listener {
    private final Main plugin;
    private final Map<Player, Long> cooldownMode = new HashMap<>();
    private final Map<Player, Long> cooldownElement = new HashMap<>();
    public InteractListener(Main plugin) {
        this.plugin = plugin;
    }

    ////////// ELEMENTALIST BLADE //////////
    @EventHandler
    public void elementalistMode(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData data = PlayerData.get(player);
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
            long cooldownEnd = System.currentTimeMillis() + 3000;

            String cost = getLoreLineValue(lore, findLoreContaining(lore, "Mana Cost:"));
            double mana = data.getMana();
            int loreLine = findLoreContaining(lore, "Active Element:");
            String element = getLoreLineValue(lore, loreLine);
            assert element != null;

            if (cooldownMode.containsKey(player)) {
                if (System.currentTimeMillis() < cooldownMode.get(player)) {
                    return;
                }
            }
            assert cost != null;
            if (mana < Double.parseDouble(cost.replaceAll("§3", ""))) {
                player.playSound(player, Sound.BLOCK_BEACON_DEACTIVATE, 50, 5);
                player.sendMessage(ChatColor.RED + "You don't have enough mana to cast this skill!");

                cooldownMode.put(player, cooldownEnd);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    cooldownMode.remove(player);
                }, 3000 / 50);
                return;
            }

            switch (element) {
                case "§8Water":
                    lore.set(loreLine, ChatColor.WHITE + "Active Element: " + ChatColor.DARK_GRAY + "Fire");
                    player.sendMessage(ChatColor.GREEN + "Active Element: " + ChatColor.RED + "Fire");
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 10, 2);
                    break;
                case "§8Fire":
                    lore.set(loreLine, ChatColor.WHITE + "Active Element: " + ChatColor.DARK_GRAY + "Ice");
                    player.sendMessage(ChatColor.GREEN + "Active Element: " + ChatColor.AQUA + "Ice");
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 10, 2);
                    break;
                case "§8Ice":
                    lore.set(loreLine, ChatColor.WHITE + "Active Element: " + ChatColor.DARK_GRAY + "Water");
                    player.sendMessage(ChatColor.GREEN + "Active Element: " + ChatColor.BLUE + "Water");
                    player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 10, 2);
                    break;
            }

            itemMeta.setLore(lore);
            blade.setItemMeta(itemMeta);
            player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 50, 5);
            data.setMana(data.getMana() - 10);

            cooldownMode.put(player, cooldownEnd);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cooldownMode.remove(player);
            }, 3000 / 50);
        }
    }

    @EventHandler
    public void elementalistWater(EntityDamageByEntityEvent event) {
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

        if (hasLoreContaining(lore, "§fActive Element: §8Water")) {
            if (health <= (0.1 * maxHealth)) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 2));
            }
        }
    }

    @EventHandler
    public void elementalistFire(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        LivingEntity target = (LivingEntity) event.getEntity();
        Player player = (Player) event.getDamager();
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
        if (lore == null) {
            return;
        }

        if (hasLoreContaining(lore, "§fActive Element: §8Fire")) {
            if (cooldownElement.containsKey(player)) {
                long cooldownEnd = cooldownElement.get(player);
                if (System.currentTimeMillis() < cooldownEnd) {
                    return;
                }
            }
            player.sendMessage(ChatColor.WHITE + "You hit " + ChatColor.AQUA + target.getName() + ChatColor.WHITE + " with Fire Elementalist");
            target.setFireTicks(60);
            new BukkitRunnable() {
                int ticksRemaining = 6; // 0.5 seconds = 6 ticks (20 ticks per second)
                @Override
                public void run() {
                    if (ticksRemaining <= 0 || target.isDead()) {
                        cancel();
                        return;
                    }
                    target.damage(2);
                    ticksRemaining--;
                }
            }.runTaskTimer(plugin, 0, 10); // 10 ticks = 0.5 seconds (20 ticks per second)
            long cooldownEnd = System.currentTimeMillis() + 10000;
            cooldownElement.put(player, cooldownEnd);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage(ChatColor.WHITE + "Fire Elementalist is ready to be used!");
                cooldownElement.remove(player);
            }, 10000 / 50);
        }
    }

    @EventHandler
    public void elementalistIce(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();
        Player player = (Player) event.getDamager();
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
        if (lore == null) {
            return;
        }

        if (hasLoreContaining(lore, "§fActive Element: §8Ice")) {
            if (Math.random() <= 0.1) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 4));
                victim.setFreezeTicks(140);
                player.sendMessage(ChatColor.WHITE + "You hit " + ChatColor.AQUA + victim.getName() + ChatColor.WHITE + " with Ice Elementalist");
                if (victim instanceof Player) {
                    victim.sendMessage(ChatColor.WHITE + "You slowed down by " + ChatColor.AQUA + player.getName() + ChatColor.WHITE + " using Ice Elementalist");
                }
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
