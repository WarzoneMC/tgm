package network.warzone.tgm.util;

import com.google.common.collect.Sets;
import org.bukkit.Material;

import java.util.Set;

public class Blocks {
    public static boolean isVisualMaterial(Material material) { //TODO Temp 1.13 fix
        String name = material.name();
        return name.contains("WOOL") || name.contains("CARPET") || name.contains("TERRACOTTA") || name.contains("STAINED_GLASS") || name.contains("STAINED_GLASS_PANE");
    }
}
