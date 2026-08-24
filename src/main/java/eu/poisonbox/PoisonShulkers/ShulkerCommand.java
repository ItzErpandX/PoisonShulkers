package eu.poisonbox.PoisonShulkers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ShulkerCommand implements TabExecutor {
    @Override
    public boolean onCommand(
            @NonNull CommandSender commandSender,
            @NonNull Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {
        ShulkerRoom room;

        if (!(commandSender instanceof Player player)) {
            sendError(commandSender, "This command can only be used by players");
            return true;
        }

        if (args.length < 1) {
            sendError(commandSender, "Incorrect usage: /" + label + " help");
            return true;
        }

        if (args[0].equalsIgnoreCase("setfreeloc")) {
            Main.INSTANCE.setFreeLocation(player.getLocation());
            sendSuccess(commandSender, "Set free loc to your current position");
            return true;
        }

        if (args[0].equalsIgnoreCase("save")) {
            Main.INSTANCE.saveStorage();
            sendSuccess(commandSender, "Shulker rooms saved successfully");
            return true;
        }

        if (args[0].equalsIgnoreCase("setblock")) {
            Block block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
            if (block == null) {
                sendError(commandSender, "You must be looking at a block to use this command");
                return true;
            }

            Main.INSTANCE.setShulkerBlock(block.getLocation());
            sendSuccess(commandSender, "Set shulker block to the block you're looking at");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                sendError(commandSender, "Incorrect usage: /" + label + " create <id>");
                return true;
            }

            if (Main.INSTANCE.getRoom(args[1]) != null) {
                sendError(commandSender, "A room with that id already exists");
                return true;
            }

            Main.INSTANCE.registerRoom(
                    args[1],
                    new ShulkerRoom(
                            new Location(player.getWorld(), 0.0, 0.0, 0.0),
                            new Location(player.getWorld(), 0.0, 0.0, 0.0)
                    )
            );
            Main.INSTANCE.saveStorage();
            sendSuccess(commandSender, "Created a room with id " + args[1].toUpperCase());
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                sendError(commandSender, "Incorrect usage: /" + label + " delete <id>");
                return true;
            }

            if (Main.INSTANCE.getRoom(args[1]) == null) {
                sendError(commandSender, "No room with that id exists");
                return true;
            }

            Main.INSTANCE.unregisterRoom(args[1]);
            Main.INSTANCE.saveStorage();
            sendSuccess(commandSender, "Deleted the room with id " + args[1].toUpperCase());
            return true;
        }

        if (args[0].equalsIgnoreCase("setbutton")) {
            if (args.length < 2) {
                sendError(commandSender, "Incorrect usage: /" + label + " setbutton <id>");
                return true;
            }

            room = Main.INSTANCE.getRoom(args[1]);
            if (room == null) {
                sendError(commandSender, "No room with that id exists");
                return true;
            }

            Block block = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
            if (block == null) {
                sendError(commandSender, "You must be looking at a block to use this command");
                return true;
            }

            room.setButtonLocation(block.getLocation());
            sendSuccess(commandSender, "Set button location of " + args[1].toUpperCase()
                    + " to the block you're looking at");
            return true;
        }

        if (args[0].equalsIgnoreCase("setlocation")) {
            if (args.length < 2) {
                sendError(commandSender, "Incorrect usage: /" + label + " setlocation <id>");
                return true;
            }

            room = Main.INSTANCE.getRoom(args[1]);
            if (room == null) {
                sendError(commandSender, "No room with that id exists");
                return true;
            }

            room.setTeleportLocation(player.getLocation());
            Main.INSTANCE.saveStorage();
            sendSuccess(commandSender, "Set location of " + args[1].toUpperCase()
                    + " to your current position");
            return true;
        }

        if (args[0].equalsIgnoreCase("tp")) {
            if (args.length < 2) {
                sendError(commandSender, "Incorrect usage: /" + label + " tp <id>");
                return true;
            }

            room = Main.INSTANCE.getRoom(args[1]);
            if (room == null) {
                sendError(commandSender, "No room with that id exists");
                return true;
            }

            player.teleport(room.getTeleportLocation());
            sendSuccess(commandSender, "Teleported you to " + args[1].toUpperCase());
            return true;
        }

        sendHelp(commandSender);
        return true;
    }

    @Override
    public List<String> onTabComplete(
            @NonNull CommandSender sender,
            @NonNull Command command,
            @NonNull String label,
            String @NonNull [] args
    ) {
        if (args.length == 1) {
            return Stream.of(
                            "setfreeloc",
                            "save",
                            "setblock",
                            "create",
                            "delete",
                            "setbutton",
                            "setlocation",
                            "tp"
                    ).filter(argument -> argument.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2
                && Arrays.asList("delete", "setbutton", "setlocation", "tp")
                .contains(args[0].toLowerCase())) {
            return Main.INSTANCE.getShulkerRooms().keySet().stream()
                    .filter(roomId -> roomId.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private static void sendError(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    private static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("Shulker room help:\n", NamedTextColor.GREEN)
                .append(Component.text(" /shulker setfreeloc\n"))
                .append(Component.text(" /shulker setblock\n"))
                .append(Component.text(" /shulker create <id>\n"))
                .append(Component.text(" /shulker delete <id>\n"))
                .append(Component.text(" /shulker setbutton <id>\n"))
                .append(Component.text(" /shulker setlocation <id>\n"))
                .append(Component.text(" /shulker tp <id>")));
    }
}