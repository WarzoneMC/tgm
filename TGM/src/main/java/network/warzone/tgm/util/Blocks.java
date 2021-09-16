package network.warzone.tgm.util;

import org.bukkit.Material;

public class Blocks {

    private static String[] visualChoices = {"WOOL", "CARPET", "TERRACOTTA", "STAINED_GLASS_PANE", "STAINED_GLASS"};

    public static boolean isVisualMaterial(Material material) {
        String name = material.name();
        for(String visualChoice : visualChoices) if(name.contains(visualChoice)) return true;
        return false;
    }

    public static String whichVisualMaterial(Material material) {
        for(String visualChoice : visualChoices) if(material.name().contains(visualChoice)) return visualChoice;
        return "NONE";
    }
}
