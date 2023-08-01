package mix.core.Command;

import mix.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class TpsBar implements CommandExecutor {

    private final Main plugin;
    private final BossBar tpsBar;
    private boolean tpsMonitoring;
    private static final Map<Player, BossBar> playerBossBars = new HashMap<>();
    public TpsBar(Main plugin) {
        this.plugin = plugin;
        this.tpsBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.tpsMonitoring = false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (player.hasPermission("tpsbar")) {
                toggleTpsMonitor(player);
            } else {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "This command can only be executed by players!");
        }
        return true;
    }

    private void toggleTpsMonitor(Player player) {
        tpsMonitoring = !tpsMonitoring;

        if (tpsMonitoring) {
            startTPSMonitor(player);
            player.sendMessage(ChatColor.GREEN + "TPS Monitor has been enabled!");
        } else {
            stopTPSMonitor(player);
            player.sendMessage(ChatColor.RED + "TPS Monitor has been disabled!");
        }
    }
    private void startTPSMonitor(Player player) {
        tpsBar.addPlayer(player);
        updateTpsBar(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateTpsBar(player);
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
    }

    private void stopTPSMonitor(Player player) {
        tpsBar.removePlayer(player);
    }

    private void updateTpsBar(Player player) {
        DecimalFormat df = new DecimalFormat("#.##");
        String tps = df.format(Bukkit.getServer().getTPS()[0]);
        int tpsPercentage = (int) Math.min(Double.parseDouble(tps), 20.0) * 5;

        BarColor barColor;
        ChatColor textColor;
        if (tpsPercentage >= 75) {
            barColor = BarColor.GREEN;
            textColor = ChatColor.GREEN;
        } else if (tpsPercentage >= 50) {
            barColor = BarColor.YELLOW;
            textColor = ChatColor.YELLOW;
        } else {
            barColor = BarColor.RED;
            textColor = ChatColor.RED;
        }


        tpsBar.setTitle(ChatColor.GRAY + "TPS: " + textColor + tps);
        tpsBar.setProgress(tpsPercentage / 100.0);
        tpsBar.setColor(barColor);
    }
}
