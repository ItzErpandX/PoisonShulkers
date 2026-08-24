package eu.poisonbox.PoisonShulkers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ShulkerRoom implements ConfigurationSerializable {
    private final List<Location> blocks;
    private Location teleportLocation;
    private Location buttonLocation;
    private boolean available;
    private Player player;

    public ShulkerRoom(Location teleportLocation, Location buttonLocation) {
        this.teleportLocation = teleportLocation;
        this.buttonLocation = buttonLocation;
        this.available = true;
        this.blocks = new ArrayList<>();
    }

    public void setTeleportLocation(Location location) {
        this.teleportLocation = location;
    }

    public void setButtonLocation(Location location) {
        this.buttonLocation = location;
    }

    public Location getButtonLocation() {
        return this.buttonLocation;
    }

    public Location getTeleportLocation() {
        return this.teleportLocation;
    }

    public void free() {
        if (this.player != null) {
            Location freeLocation = Main.INSTANCE.getFreeLocation();
            if (freeLocation != null) {
                this.player.teleport(freeLocation);
            }
        }

        this.available = true;
        this.player = null;

        for (Location location : this.blocks) {
            World world = location.getWorld();
            if (world != null) {
                world.getBlockAt(location).setType(Material.AIR);
            }
        }

        this.blocks.clear();
    }

    public void join(Player player) {
        if (player == null || this.teleportLocation == null) {
            return;
        }

        player.teleport(this.teleportLocation);
        this.player = player;
        this.available = false;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void registerBlock(Location location) {
        if (location != null && !this.blocks.contains(location)) {
            this.blocks.add(location);
        }
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("teleport-location", this.teleportLocation);
        map.put("button-location", this.buttonLocation);
        map.put("blocks", new ArrayList<>(this.blocks));
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    public ShulkerRoom(Map<String, Object> map) {
        this.teleportLocation = (Location)map.get("teleport-location");
        this.buttonLocation = (Location)map.get("button-location");
        this.blocks = new ArrayList((List)map.get("blocks"));
        this.available = true;
    }
}