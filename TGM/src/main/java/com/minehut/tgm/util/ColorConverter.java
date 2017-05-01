package com.minehut.tgm.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public class ColorConverter {

    public static Color getColor(ChatColor color) {
        if (color == ChatColor.RED) {
            return Color.RED;
        } else if (color == ChatColor.BLUE) {
            return Color.BLUE;
        } else if (color == ChatColor.LIGHT_PURPLE) {
            return Color.FUCHSIA;
        } else if (color == ChatColor.GREEN) {
            return Color.GREEN;
        } else if (color == ChatColor.YELLOW) {
            return Color.YELLOW;
        } else {
            return Color.WHITE;
        }
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
            case SILVER:
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
                return DyeColor.SILVER;
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
