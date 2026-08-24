package eu.poisonbox.PoisonShulkers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static Main INSTANCE;

    private final HashMap<String, ShulkerRoom> shulkerRooms = new HashMap<>();
    private File storageFile;
    private YamlConfiguration storage;
    private Location freeLocation;
    private Location shulkerBlock;

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(ShulkerRoom.class);

        // Listener registration (was ListenerRegisterer)
        Bukkit.getPluginManager().registerEvents(new ShulkerListener(), this);

        // Command registration (was CommandRegisterer)
        // NOTE: "shulkermanager" and its aliases ("shulker", "shulkerman")
        // must be declared in plugin.yml, since we no longer have the
        // reflection-based CommandMap registration helper.
        PluginCommand command = getCommand("shulkermanager");

        if (command != null) {
            command.setExecutor(new ShulkerCommand());
        } else {
            getLogger().warning(
                    "Command 'shulkermanager' is not defined in plugin.yml"
            );
        }

        // Storage load (was SingleRawStorage)
        this.storageFile = new File(getDataFolder(), "rooms.yml");

        File parent = storageFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            getLogger().severe("Could not create storage directory: " + parent.getAbsolutePath());
            return;
        }

        if (!storageFile.exists()) {
            try {
                if (!storageFile.createNewFile()) {
                    getLogger().severe("Could not create storage file: " + storageFile.getAbsolutePath());
                    return;
                }
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not create storage file: " + storageFile.getAbsolutePath(), ex);
                return;
            }
        }
        this.storage = YamlConfiguration.loadConfiguration(this.storageFile);

        this.shulkerRooms.clear();
        ConfigurationSection section = this.storage.getConfigurationSection("rooms");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                this.shulkerRooms.put(id, section.getSerializable(id, ShulkerRoom.class));
            }
        }
        this.freeLocation = this.storage.getLocation("free-location");
        this.shulkerBlock = this.storage.getLocation("shulker-block");
    }

    @Override
    public void onDisable() {
        // Now save current state
        saveStorage();
    }

    public Location getFreeLocation() {
        return this.freeLocation;
    }

    public Location getShulkerBlock() {
        return this.shulkerBlock;
    }

    public void setFreeLocation(Location freeLocation) {
        this.freeLocation = freeLocation;
    }

    public void setShulkerBlock(Location shulkerBlock) {
        this.shulkerBlock = shulkerBlock;
    }

    public HashMap<String, ShulkerRoom> getShulkerRooms() {
        return this.shulkerRooms;
    }

    public ShulkerRoom getRoom(org.bukkit.entity.Player player) {
        for (ShulkerRoom room : this.shulkerRooms.values()) {
            if (room.getPlayer() == null || !room.getPlayer().equals(player)) continue;
            return room;
        }
        return null;
    }

    public ShulkerRoom getRoom(String id) {
        return this.shulkerRooms.get(id);
    }

    public ShulkerRoom getFreeRoom() {
        for (ShulkerRoom room : this.shulkerRooms.values()) {
            if (!room.isAvailable()) continue;
            return room;
        }
        return null;
    }

    public void saveStorage() {
        for (var entry : this.shulkerRooms.entrySet()) {
            this.storage.set("rooms." + entry.getKey(), entry.getValue());
        }

        this.storage.set("free-location", this.freeLocation);
        this.storage.set("shulker-block", this.shulkerBlock);

        try {
            this.storage.save(this.storageFile);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save storage file: " + this.storageFile.getAbsolutePath(), e);
        }
    }

    public void registerRoom(String id, ShulkerRoom room) {
        this.shulkerRooms.put(id, room);
    }

    public void unregisterRoom(String id) {
        this.shulkerRooms.remove(id);
    }
}