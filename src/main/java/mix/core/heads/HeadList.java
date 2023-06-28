package mix.core.heads;

import mix.core.Main;
import org.bukkit.inventory.ItemStack;

public enum HeadList {
    WHEAT("MjY0NTliZTA5OTk4ZTUwYWJkMmNjZjRjZDM4M2U2YjM4YWI1YmM5MDVmYWNiNjZkY2UwZTE0ZTAzOGJhMTk2OCJ9fX0=", "wheat");
    private final ItemStack item;
    private final String idTag;
    private final String url;
    public String prefix = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

    HeadList(String texture, String id) {
        item = Main.createSkull(prefix + texture, id);
        idTag = id;
        url = prefix + texture;
    }

    public String getUrl() {
        return url;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public String getName() {
        return idTag;
    }
}

