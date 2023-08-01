package mix.core.Data;

import mix.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DataManager {

    private final Main plugin;
    private final File userFile;
    private final FileConfiguration userData;
    private final HashMap<String, Config> configs = new HashMap<>();
    private final Map<String, Integer> pityMap;

    public DataManager(Main plugin) {
        this.plugin = plugin;
        this.userFile = new File(plugin.getDataFolder(), "userdata.yml");
        this.userData = YamlConfiguration.loadConfiguration(userFile);
        this.pityMap = new HashMap<>();
        saveDefaultUserData();
    }

    private void saveDefaultUserData() {
        if (!userFile.exists()) {
            plugin.saveResource("userdata.yml", false);
        }
    }

    public Config getConfig(String name) {
        if (!configs.containsKey(name))
            configs.put(name, new Config(name));

        return configs.get(name);
    }

    /**
     * Save the config by the name(Don't forget the .yml)
     *
     */
    public Config saveConfig(String name) {
        return getConfig(name).save();
    }

    /**
     * Reload the config by the name(Don't forget the .yml)
     *
     */
    public Config reloadConfig(String name) {
        return getConfig(name).reload();
    }

    public class Config {

        private final String name;
        private File file;
        private YamlConfiguration config;
        public Config(String name) {
            this.name = name;
        }

        /**
         * Saves the config as long as the config isn't empty
         *
         */
        public Config save() {
            if ((this.config == null) || (this.file == null))
                return this;
            try
            {
                if (Objects.requireNonNull(config.getConfigurationSection("")).getKeys(true).size() != 0)
                    config.save(this.file);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            return this;
        }

        /**
         * Gets the config as a YamlConfiguration
         *
         */
        public YamlConfiguration get() {
            if (this.config == null)
                reload();

            return this.config;
        }

        /**
         * Saves the default config(Will overwrite anything in the current config's file)
         * <p>
         * Don't forget to reload after!
         *
         */
        public Config saveDefaultConfig() {
            file = new File(plugin.getDataFolder(), this.name);
            if (!file.exists()) {
                try (InputStream inputStream = plugin.getResource(this.name)) {
                    if (inputStream != null) {
                        if (!file.getParentFile().exists())
                            file.getParentFile().mkdirs();

                        try (OutputStream outputStream = new FileOutputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                        }
                    } else {
                        plugin.getLogger().warning("Failed to save default configuration file: " + this.name);
                    }
                } catch (IOException ex) {
                    plugin.getLogger().warning("Failed to save default configuration file: " + this.name);
                    ex.printStackTrace();
                }
            }

            return this;
        }

        /**
         * Reloads the config
         * *
         *
         */
        public Config reload() {
            if (file == null)
                this.file = new File(plugin.getDataFolder(), this.name);

            this.config = YamlConfiguration.loadConfiguration(file);

            try (Reader defConfigstream = new InputStreamReader(plugin.getResource(this.name), StandardCharsets.UTF_8)) {
                if (defConfigstream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigstream);
                    this.config.setDefaults(defConfig);
                } else {
                    plugin.getLogger().warning("Failed to load default configuration file: " + this.name);
                }
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigstream);
                this.config.setDefaults(defConfig);
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to load default configuration file: " + this.name);
                ex.printStackTrace();
            }
            return this;
        }

        /**
         * Copies the config from the resources to the config's default settings.
         * <p>
         * Force = true ----> Will add any new values from the default file
         * <p>
         * Force = false ---> Will NOT add new values from the default file
         *
         */
        public Config copyDefaults(boolean force) {
            get().options().copyDefaults(force);
            return this;
        }

        /**
         * An easy way to set a value into the config
         *
         */
        public Config set(String key, Object value) {
            get().set(key, value);
            return this;
        }

        /**
         * An easy way to get a value from the config
         *
         */
        public Object get(String key) {
            return get().get(key);
        }
    }

    public int getPity(String playerName) {
        return this.pityMap.getOrDefault(playerName, 0);
    }

    public int getTotalPity() {
        int totalPity = 0;
        for (int amount : this.pityMap.values()) {
            totalPity += amount;
        }
        return totalPity;
    }

    public void setPity(String playerName, int pity) {
        this.pityMap.put(playerName, pity);
        userData.set("players." + playerName + ".pity", pity);
        Bukkit.getLogger().info("[WishingWell] " + ChatColor.GOLD + "Succesfully set " + ChatColor.AQUA + playerName + "'s " + ChatColor.GOLD + "total pity to " + ChatColor.RESET + pity);
        saveUserData();
    }

    public void addPity(String playerName, int pity) {
        int currentPity = pityMap.getOrDefault(playerName, 0);
        int newPity = currentPity + pity;
        this.pityMap.put(playerName, newPity);
        Bukkit.getLogger().info( "[WishingWell] " + ChatColor.GOLD + "Added " + ChatColor.RESET + pity + ChatColor.GOLD + " Pity to " + ChatColor.AQUA + playerName + ChatColor.GOLD + ", total pity " + ChatColor.RESET + newPity);
    }

    public void saveUserData() {
        ConfigurationSection playerSection = userData.createSection("Pity");
        for (Map.Entry<String, Integer> entry : this.pityMap.entrySet()) {
            String playerName = entry.getKey();
            int pity = entry.getValue();
            playerSection.set(playerName + ".amount", pity);
        }

        try {
            userData.save(userFile);
            Bukkit.getLogger().info("[WishingWell] Succesfully saved userdata!");
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getLogger().info("[WishingWell] " + ChatColor.RED + "There's a problem saving userdata!");
        }
    }

    public void loadUserData() {
        try {
            Map<String, Integer> pityMap = new HashMap<>();
            ConfigurationSection playerSection = userData.getConfigurationSection("Pity");
            if (playerSection != null) {
                for (String playerName : playerSection.getKeys(false)) {
                    int pity = playerSection.getInt(playerName + ".amount", 0);
                    pityMap.put(playerName, pity);
                }
            }
            this.pityMap.clear();
            this.pityMap.putAll(pityMap);
            Bukkit.getLogger().info("[WishingWell] Userdata successfully mapped!");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().info("[WishingWell] " + ChatColor.RED + "There's a problem mapping userdata!");
        }
    }

    public Map<String, Integer> getAllPityData() {
        Map<String, Integer> pityData = new HashMap<>();
        ConfigurationSection playersSection = userData.getConfigurationSection("Pity");
        if (playersSection != null) {
            for (String playerName : playersSection.getKeys(false)) {
                int pity = playersSection.getInt(playerName + ".amount", 0);
                pityData.put(playerName, pity);
            }
        }
        return pityData;
    }
}