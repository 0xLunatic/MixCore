package mix.core.Command;

import mix.core.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Pity implements CommandExecutor {

    private final Main plugin;

    public Pity(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        if (!player.hasPermission("op")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        String modifier = strings[0].toLowerCase();
        int amount;

        if (strings.length >= 2) {
            Player targetPlayer = commandSender.getServer().getPlayer(strings[1]);
            switch (modifier) {
                case "set":
                    try {
                        amount = Integer.parseInt(strings[2]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(ChatColor.RED + "Invalid amount. Please provide a valid integer value.");
                        return true;
                    }

                    if (amount < 0) {
                        commandSender.sendMessage(ChatColor.RED + "Pity amount can't be negative!");
                    }

                    if (commandSender.getServer().getPlayer(strings[1]) == null) {
                        commandSender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    assert targetPlayer != null;
                    plugin.getDataManager().setPity(targetPlayer.getName(), amount);
                    commandSender.sendMessage(ChatColor.GREEN + "Successfully set " + targetPlayer.getName() + "'s pity to " + amount);

                    return true;

                case "add":
                    try {
                        amount = Integer.parseInt(strings[2]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(ChatColor.RED + "Invalid amount. Please provide a valid integer value.");
                        return true;
                    }

                    if (amount < 0) {
                        commandSender.sendMessage(ChatColor.RED + "Pity amount can't be negative!");
                    }

                    if (targetPlayer == null) {
                        commandSender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    plugin.getDataManager().addPity(targetPlayer.getName(), amount);
                    commandSender.sendMessage(ChatColor.GREEN + "Successfully add " + amount + " pity to " + targetPlayer.getName());

                    return true;

                case "check":
                    int pity = plugin.getDataManager().getPity(targetPlayer.getName());
                    commandSender.sendMessage(String.valueOf(pity));

                    return true;
            }
        } else {
            switch (modifier) {
                case "save":
                    plugin.getDataManager().saveUserData();
                    commandSender.sendMessage("Successfully save userdata!");

                    return true;

                case "reload":
                    plugin.getDataManager().loadUserData();
                    commandSender.sendMessage("Successfully reload userdata!");

                    return true;
            }
        }
    return false;
    }
}
