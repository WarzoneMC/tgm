package com.minehut.tgm.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;

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


}
