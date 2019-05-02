package network.warzone.tgm.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public class ColorConverter {

    public static String filterString(String text) {
        return ChatColor.translateAlternateColorCodes('&' , text);
    }

    public static Color getColor(ChatColor color) {
        if (color == ChatColor.RED) {
            return Color.RED;
        } else if (color == ChatColor.BLUE) {
            return Color.BLUE;
        } else if (color == ChatColor.LIGHT_PURPLE) {
            return Color.FUCHSIA;
        } else if (color == ChatColor.GREEN) {
            return Color.LIME;
        } else if (color == ChatColor.DARK_GREEN) {
            return Color.GREEN;
        } else if (color == ChatColor.YELLOW) {
            return Color.YELLOW;
        } else if (color == ChatColor.GOLD) {
            return Color.ORANGE;
        } else if (color == ChatColor.AQUA) {
            return Color.AQUA;
        } else if (color == ChatColor.DARK_PURPLE) {
            return Color.PURPLE;
        } else if (color == ChatColor.DARK_AQUA) {
            return Color.TEAL;
        } else {
            return Color.WHITE;
        }
    }

    public static Color getColorAll(ChatColor color) {
        switch (color) {
            case BLACK:
                return Color.fromRGB(0, 0, 0);
            case DARK_BLUE:
                return Color.fromRGB(0, 0, 170);
            case DARK_GREEN:
                return Color.fromRGB(0, 170, 0);
            case DARK_AQUA:
                return Color.fromRGB(0, 170, 170);
            case DARK_RED:
                return Color.fromRGB(170, 0, 0);
            case DARK_PURPLE:
                return Color.fromRGB(170, 0, 170);
            case GOLD:
                return Color.fromRGB(255, 170, 0);
            case GRAY:
                return Color.fromRGB(170, 170, 170);
            case DARK_GRAY:
                return Color.fromRGB(85, 85, 85);
            case BLUE:
                return Color.fromRGB(85, 85, 255);
            case GREEN:
                return Color.fromRGB(85, 255, 85);
            case AQUA:
                return Color.fromRGB(85, 255, 255);
            case RED:
                return Color.fromRGB(255, 85, 85);
            case LIGHT_PURPLE:
                return Color.fromRGB(255, 85, 255);
            case YELLOW:
                return Color.fromRGB(255, 255, 85);
            case WHITE:
                return Color.fromRGB(255, 255, 255);
        }

        return Color.WHITE;
    }

    public static ChatColor getChatColor(Color color) {
        if (color == Color.RED) {
            return ChatColor.RED;
        } else if (color == Color.BLUE) {
            return ChatColor.BLUE;
        } else if (color == Color.PURPLE) {
            return ChatColor.DARK_PURPLE;
        } else if (color == Color.GREEN) {
            return ChatColor.GREEN;
        } else if (color == Color.YELLOW) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.WHITE;
        }
    }

    public static ChatColor convertDyeColorToChatColor(DyeColor dye) {
        switch (dye) {
            case WHITE:
                return ChatColor.WHITE;
            case ORANGE:
                return ChatColor.GOLD;
            case MAGENTA:
                return ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE:
                return ChatColor.BLUE;
            case YELLOW:
                return ChatColor.YELLOW;
            case LIME:
                return ChatColor.GREEN;
            case PINK:
                return ChatColor.RED;
            case GRAY:
                return ChatColor.DARK_GRAY;
            case LIGHT_GRAY:
                return ChatColor.GRAY;
            case CYAN:
                return ChatColor.DARK_AQUA;
            case PURPLE:
                return ChatColor.DARK_PURPLE;
            case BLUE:
                return ChatColor.DARK_BLUE;
            case BROWN:
                return ChatColor.GOLD;
            case GREEN:
                return ChatColor.DARK_GREEN;
            case RED:
                return ChatColor.DARK_RED;
            case BLACK:
                return ChatColor.BLACK;
        }

        return ChatColor.WHITE;
    }

    public static DyeColor convertChatColorToDyeColor(ChatColor chatColor) {
        switch (chatColor) {
            case WHITE:
                return DyeColor.WHITE;
            case AQUA:
                return DyeColor.LIGHT_BLUE;
            case GOLD:
                return DyeColor.ORANGE;
            case LIGHT_PURPLE:
                return DyeColor.MAGENTA;
            case YELLOW:
                return DyeColor.YELLOW;
            case GREEN:
                return DyeColor.LIME;
            case RED:
                return DyeColor.RED;
            case GRAY:
                return DyeColor.LIGHT_GRAY;
            case DARK_GRAY:
                return DyeColor.GRAY;
            case DARK_AQUA:
                return DyeColor.CYAN;
            case DARK_PURPLE:
                return DyeColor.PURPLE;
            case DARK_BLUE:
                return DyeColor.BLUE;
            case BLUE:
                return DyeColor.BLUE;
            case DARK_GREEN:
                return DyeColor.GREEN;
            case DARK_RED:
                return DyeColor.RED;
            case BLACK:
                return DyeColor.BLACK;
        }

        return DyeColor.WHITE;
    }


}
