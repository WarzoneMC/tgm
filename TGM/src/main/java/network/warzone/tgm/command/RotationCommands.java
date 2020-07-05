package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.map.Rotation;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RotationCommands {

    @Command(aliases={"rots", "rotations"}, desc= "View all the rotations.")
    public static void viewRotations(CommandContext context, CommandSender sender) throws CommandException {
        int index = context.argsLength() == 0 ? 1 : context.getInteger(0);
        List<Rotation> rotationLibrary = TGM.get().getMatchManager().getMapRotation().getRotationLibrary();

        int pageSize = 9;

        int pagesRemainder = rotationLibrary.size() % pageSize;
        int pagesDivisible = rotationLibrary.size() / pageSize;
        int pages = pagesDivisible;

        if (pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }

        sender.sendMessage(ChatColor.YELLOW + "Rotations (" + index + "/" + pages + "): ");
        try {
            for (int i = 0; i < pageSize; i++) {
                int position = pageSize * (index - 1) + i;
                Rotation rotation = rotationLibrary.get(position);
                TextComponent message = rotationToTextComponent(position, rotation);
                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
    }


    @Command(aliases={"setrot", "setrotation"}, desc = "Sets the current rotation.")
    @CommandPermissions({"tgm.command.setrot"})
    public static void setRotation(CommandContext context, CommandSender sender) {
        if (context.argsLength() == 0) {
            sender.sendMessage(ChatColor.RED + "No rotation provided.");
            return;
        }

        String rotationName = context.getJoinedStrings(0);

        if (!TGM.get().getMatchManager().getMapRotation().hasRotation(rotationName)) {
            sender.sendMessage(ChatColor.RED + "Invalid rotation. Try /rotations to view all rotations.");
            return;
        }

        TGM.get().getMatchManager().getMapRotation().setRotation(rotationName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aUpdated rotation to &e" + rotationName + "&a."));
    }

    @Command(aliases = {"rot", "rotation"}, desc = "View the maps that are in the rotation.", usage = "[page]")
    public static void rotation(final CommandContext cmd, CommandSender sender) throws CommandException {
        int index = cmd.argsLength() == 0 ? 1 : cmd.getInteger(0);
        List<MapContainer> rotation = TGM.get().getMatchManager().getMapRotation().getMaps();

        int pageSize = 9;

        int pagesRemainder = rotation.size() % pageSize;
        int pagesDivisible = rotation.size() / pageSize;
        int pages = pagesDivisible;

        if (pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }

        String rotationName = TGM.get().getMatchManager().getMapRotation().getRotation().getName();
        sender.sendMessage(ChatColor.YELLOW + rotationName + " Rotation (" + index + "/" + pages + "): ");
        try {
            for (int i = 0; i < pageSize; i++) {
                int position = pageSize * (index - 1) + i;
                MapContainer map = rotation.get(position);
                TextComponent message = CycleCommands.mapToTextComponent(position, map.getMapInfo());
                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    private static TextComponent rotationToTextComponent(int position, Rotation rotation) {
        String rotationName = ChatColor.GOLD + rotation.getName();

        if (rotation.equals(TGM.get().getMatchManager().getMapRotation().getRotation())) {
            rotationName = ChatColor.GREEN + "" + (position + 1) + ". " + rotationName;
        } else {
            rotationName = ChatColor.WHITE + "" + (position + 1) + ". " + rotationName;
        }
        TextComponent message = new TextComponent(rotationName);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setrot " + rotation.getName()));
        ComponentBuilder builder = new ComponentBuilder(ChatColor.GOLD + rotation.getName() + (rotation.isDefault() ? ChatColor.GRAY + " - Default" : "")).append("\n\n")
                .append(ChatColor.GRAY + "Requirements: ").append(ChatColor.YELLOW +
                        (rotation.isDefault() ? rotation.getRequirements().getMin() + "-" + rotation.getRequirements().getMax() + " players" : "N/A")).append("\n")
                .append(ChatColor.GRAY + "Map Count: ").append(ChatColor.YELLOW + String.valueOf(rotation.getMaps().size()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, builder.create()));
        return message;
    }
}
