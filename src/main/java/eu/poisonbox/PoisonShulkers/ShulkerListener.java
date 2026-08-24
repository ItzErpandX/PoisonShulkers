package eu.poisonbox.PoisonShulkers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ShulkerListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || event.getClickedBlock() == null) {
            return;
        }

        if (Main.INSTANCE.getShulkerBlock() == null) {
            return;
        }

        ShulkerRoom room = Main.INSTANCE.getRoom(event.getPlayer());
        if (Main.INSTANCE.getShulkerBlock().equals(event.getClickedBlock().getLocation())) {
            event.setCancelled(true);

            if (room != null) {
                return;
            }

            ShulkerRoom freeRoom = Main.INSTANCE.getFreeRoom();
            if (freeRoom == null) {
                event.getPlayer().sendMessage(Component.text(
                        "There are currently no shulker rooms available, please try again later",
                        NamedTextColor.RED
                ));
                return;
            }

            freeRoom.join(event.getPlayer());
        } else if (room != null
                && room.getButtonLocation() != null
                && room.getButtonLocation().equals(event.getClickedBlock().getLocation())) {
            room.free();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        ShulkerRoom room = Main.INSTANCE.getRoom(event.getPlayer());
        if (room == null) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text(
                "You may not use commands while in a shulker room",
                NamedTextColor.RED
        ));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ShulkerRoom room = Main.INSTANCE.getRoom(event.getPlayer());
        if (room != null) {
            room.free();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ShulkerRoom room = Main.INSTANCE.getRoom(event.getPlayer());
        if (room == null) {
            return;
        }

        room.registerBlock(event.getBlock().getLocation());
    }
}