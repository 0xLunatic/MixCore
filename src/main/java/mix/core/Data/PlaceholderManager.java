package mix.core.Data;

import com.google.common.base.Joiner;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import mix.core.EventListener.WishingWellListener;
import mix.core.Main;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion{
    private final Main plugin;
    private final WishingWellListener wishingWellListener;
    public PlaceholderManager(Main plugin, WishingWellListener wishingWellListener) {
        this.plugin = plugin;
        this.wishingWellListener = wishingWellListener;
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getDescription().getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return Joiner.on(", ").join(this.plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {

        if (params.equalsIgnoreCase("pity")) {
            String pity = String.valueOf(plugin.getDataManager().getTotalPity());
            return pity;
        }
        if (params.equalsIgnoreCase("amount")) {
            String totalPity = String.valueOf(plugin.getDataManager().getPity(player.getName()));
            return totalPity;
        }
        if (params.equalsIgnoreCase("reward")) {
            int totalPity = plugin.getDataManager().getTotalPity();
            if (totalPity >= 50) {
                if (plugin.getDataManager().getPity(player.getName()) > 0){
                    return "Claim your wish";
                }
                return "You can't claim anything with no wish!";
            } else {
                return "Pity " + totalPity + " of 50";
            }
        }

        return null;
    }
}
