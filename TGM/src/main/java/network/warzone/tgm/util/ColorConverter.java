package network.warzone.tgm.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;

public class ColorConverter {

    private static Material[] orderedWool = {Material.WHITE_WOOL, Material.LIGHT_BLUE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL, Material.RED_WOOL, Material.LIGHT_GRAY_WOOL, Material.GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL, Material.BLACK_WOOL};
    private static Material[] orderedCarpet = {Material.WHITE_CARPET, Material.LIGHT_BLUE_CARPET, Material.ORANGE_CARPET, Material.MAGENTA_CARPET, Material.YELLOW_CARPET, Material.LIME_CARPET, Material.RED_CARPET, Material.LIGHT_GRAY_CARPET, Material.GRAY_CARPET, Material.CYAN_CARPET, Material.PURPLE_CARPET, Material.BLUE_CARPET, Material.GREEN_CARPET, Material.BLACK_CARPET};
    private static Material[] orderedTerracotta = {Material.WHITE_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.MAGENTA_TERRACOTTA, Material.YELLOW_TERRACOTTA, Material.LIME_TERRACOTTA, Material.RED_TERRACOTTA, Material.LIGHT_GRAY_TERRACOTTA, Material.GRAY_TERRACOTTA, Material.CYAN_TERRACOTTA, Material.PURPLE_TERRACOTTA, Material.BLUE_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.BLACK_TERRACOTTA};
    private static Material[] orderedStainedGlass = {Material.WHITE_STAINED_GLASS, Material.LIGHT_BLUE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.CYAN_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.BLACK_STAINED_GLASS};
    private static Material[] orderedStainedGlassPane = {Material.WHITE_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.CYAN_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE};

    public static String filterString(String text) {
        return ChatColor.translateAlternateColorCodes('&' , text);
    }

    public static Color getColor(ChatColor color) {
        switch (color) {
            case RED:
                return Color.RED;
            case BLUE:
                return Color.BLUE;
            case LIGHT_PURPLE:
                return Color.FUCHSIA;
            case GREEN:
                return Color.LIME;
            case DARK_GREEN:
                return Color.GREEN;
            case YELLOW:
                return Color.YELLOW;
            case GOLD:
                return Color.ORANGE;
            case AQUA:
                return Color.AQUA;
            case DARK_AQUA:
                return Color.TEAL;
            case DARK_PURPLE:
                return Color.PURPLE;
            default:
                return getColorAll(color);
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
            default:
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
            case GREEN:
                return ChatColor.DARK_GREEN;
            case RED:
                return ChatColor.DARK_RED;
            case BLACK:
                return ChatColor.BLACK;
            case BROWN:
            case ORANGE:
                return ChatColor.GOLD;
            default:
                return ChatColor.WHITE;
        }
    }

    public static Material convertChatColorToColoredBlock(Material oldMaterial, ChatColor chatColor) {
        int targetIndex;
        switch (chatColor) {
            case AQUA:
                targetIndex = 1;
                break;
            case GOLD:
                targetIndex = 2;
                break;
            case LIGHT_PURPLE:
                targetIndex = 3;
                break;
            case YELLOW:
                targetIndex = 4;
                break;
            case GREEN:
                targetIndex = 5;
                break;
            case DARK_RED:
            case RED:
                targetIndex = 6;
                break;
            case GRAY:
                targetIndex = 7;
                break;
            case DARK_GRAY:
                targetIndex = 8;
                break;
            case DARK_AQUA:
                targetIndex = 9;
                break;
            case DARK_PURPLE:
                targetIndex = 10;
                break;
            case DARK_BLUE:
            case BLUE:
                targetIndex = 11;
                break;
            case DARK_GREEN:
                targetIndex = 12;
                break;
            case BLACK:
                targetIndex = 13;
                break;
            case WHITE:
            default:
                targetIndex = 0;
                break;
        }
        switch (Blocks.whichVisualMaterial(oldMaterial)) {
            case "WOOL":
                return orderedWool[targetIndex];
            case "CARPET":
                return orderedCarpet[targetIndex];
            case "STAINED_GLASS_PANE":
                return orderedStainedGlassPane[targetIndex];
            case "STAINED_GLASS":
                return orderedStainedGlass[targetIndex];
            default:
                return orderedTerracotta[targetIndex];
        }
    }

    public static DyeColor convertChatColorToDyeColor(ChatColor chatColor) {
        switch (chatColor) {
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
            case GRAY:
                return DyeColor.LIGHT_GRAY;
            case DARK_GRAY:
                return DyeColor.GRAY;
            case DARK_AQUA:
                return DyeColor.CYAN;
            case DARK_PURPLE:
                return DyeColor.PURPLE;
            case DARK_GREEN:
                return DyeColor.GREEN;
            case BLACK:
                return DyeColor.BLACK;
            case DARK_BLUE:
            case BLUE:
                return DyeColor.BLUE;
            case RED:
            case DARK_RED:
                return DyeColor.RED;
            default:
                return DyeColor.WHITE;
        }
    }

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
