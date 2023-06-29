package mix.core;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import mix.core.EventListener.InteractListener;
import mix.core.EventListener.WheatListener;
import mix.core.heads.HeadList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("=======================================");
        System.out.println("Survival Mix Core by Mornov Enabled!");
        System.out.println("=======================================");
        getServer().getPluginManager().registerEvents(new WheatListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);

        Bukkit.getScheduler().runTask(this, () -> {
            World world = Bukkit.getWorld("spawn"); // Replace "s3" with the actual name of your world
            spawnWheatOrb(world, -49, 180, -36);
            spawnWheatOrb(world, -30, 179, -78);

        });
        Bukkit.getScheduler().runTask(this, () -> {
            World world = Bukkit.getWorld("spawn"); // Replace "s3" with the actual name of your world
            spawnWheatOrb(world, -27, 179, -64);
        });
        Bukkit.getScheduler().runTask(this, () -> {
            World world = Bukkit.getWorld("spawn"); // Replace "s3" with the actual name of your world
            spawnWheatOrb(world, 38, 179, -192);
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        removeCustomNameArmorStands();
        clearCacheFile();
    }

    public void spawnWheatOrb(World world, double x, double y, double z) {
        if (world != null) {
            Location spawnLocation = new Location(world, x, y, z);
            ArmorStand orb = world.spawn(spawnLocation, ArmorStand.class);
            orb.setHelmet(Main.getHead("wheat"));
            orb.setVisible(false);
            orb.setInvulnerable(true);
            orb.setCustomName("Wheat_Orb");
            orb.setGravity(false);
            orb.setMarker(true);
            moveUpOrb(orb);
            cacheCustomName(orb.getCustomName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!orb.isDead()) {
                        Location orbLocation = orb.getLocation();
                        Location farmlandLocation = findFarmlandLocation(orbLocation, 25);
                        if (farmlandLocation != null) {
                            placeWheatOnFarmland(farmlandLocation);
                            createLineParticle(orbLocation, farmlandLocation);
                        }
                    }else{
                        cancel();
                    }
                }
            }.runTaskTimer(this, 40, 40);

        } else {
            System.out.println(ChatColor.RED + "The specified world does not exist.");
        }
    }
    private void moveUpOrb(ArmorStand orb) {
        new BukkitRunnable() {
            private boolean goingUp = true;
            Location loc = orb.getLocation();
            private final int maximumHeight = loc.getBlockY() + 2;
            private final int minimumHeight = loc.getBlockY();

            @Override
            public void run() {
                if (orb.isDead()) {
                    orb.remove();
                    cancel();
                    return;
                }
                if (goingUp) {
                    if (orb.getLocation().getY() > maximumHeight) {
                        goingUp = false;
                    } else {
                        loc.setYaw(loc.getYaw() + (float) 20);
                        orb.teleport(loc.add(0, 0.07, 0));

                    }
                } else {
                    if (orb.getLocation().getY() < minimumHeight) {
                        goingUp = true;
                    } else {
                        loc.setYaw(loc.getYaw() + (float) 20);
                        orb.teleport(loc.add(0, -0.07, 0));
                    }
                }
            }
        }.runTaskTimer(this, 1, 1);
    }
    private Location findFarmlandLocation(Location centerLocation, int radius) {
        World world = centerLocation.getWorld();
        int centerX = centerLocation.getBlockX();
        int centerY = centerLocation.getBlockY();
        int centerZ = centerLocation.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.FARMLAND) {
                        Block blockAbove = world.getBlockAt(x, y + 1, z);
                        if (!isBlockAboveValid(blockAbove)) {
                            continue; // Skip this farmland if the block above is not valid
                        }
                        return block.getLocation();
                    }
                }
            }
        }

        return null;
    }

    private boolean isBlockAboveValid(Block blockAbove) {
        Material typeAbove = blockAbove.getType();
        // Add any additional checks here for blocks that are not valid above farmland
        return typeAbove.isAir() || typeAbove == Material.WATER || typeAbove == Material.LAVA;
    }

    private void placeWheatOnFarmland(Location location) {
        Block block = location.add(0, 1, 0).getBlock();
        block.setType(Material.WHEAT);

        BlockData blockData = block.getBlockData();
        if (blockData instanceof Ageable) {
            Ageable ageable = (Ageable) blockData;
            ageable.setAge(ageable.getMaximumAge());
            block.setBlockData(ageable);
        }
    }

    private void createLineParticle(Location startLocation, Location endLocation) {
        World world = startLocation.getWorld();
        int particleCount = 20; // Number of particles to spawn along the line
        double distance = startLocation.distance(endLocation);
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();
        double interval = distance / particleCount;
        double offsetX = direction.getX() * interval;
        double offsetY = direction.getY() * interval;
        double offsetZ = direction.getZ() * interval;

        for (int i = 0; i < particleCount; i++) {
            Location particleLocation = startLocation.clone().add(offsetX * i, offsetY * i, offsetZ * i);
            world.spawnParticle(Particle.CLOUD, particleLocation, 1, 0, 0, 0, 0);
        }
    }


    public void cacheCustomName(String customName) {
        try {
            File folder = new File("plugins/ArmorStandCache");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(folder, "armorStandCache.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(customName);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void removeCustomNameArmorStands() {
        List<String> customNames = readCustomNamesFromFile();
        if (customNames.isEmpty()) {
            return; // No custom names found in the file
        }

        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (armorStand.getCustomName() != null && customNames.contains(armorStand.getCustomName())) {
                    armorStand.remove();
                }
                if (armorStand.getCustomName() != null && armorStand.getCustomName().contains("Wheat_Orb")){
                    armorStand.remove();
                }
            }
        }
    }

    private List<String> readCustomNamesFromFile() {
        List<String> customNames = new ArrayList<>();
        File file = new File("plugins/ArmorStandCache/armorStandCache.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                customNames.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return customNames;
    }

    private void clearCacheFile() {
        try {
            File file = new File("plugins/ArmorStandCache/armorStandCache.txt");
            if (file.exists()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write("");
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack createSkull(String url, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
        if (url.isEmpty()) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));

        try {
            assert headMeta != null;
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
            error.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }
    public static ItemStack getHead(String name) {
        for (HeadList head : HeadList.values()) {
            if (head.getName().equals(name)) {
                return head.getItemStack();
            }
        }
        return null;

    }
}


