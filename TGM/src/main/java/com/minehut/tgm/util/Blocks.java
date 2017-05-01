package com.minehut.tgm.util;

import com.google.common.collect.Sets;
import org.bukkit.Material;

import java.util.Set;

public class Blocks {
    public static boolean isVisualMaterial(Material material) {
        Set<Material> visuals = Sets.newHashSet();
        visuals.add(Material.WOOL);
        visuals.add(Material.CARPET);
        visuals.add(Material.STAINED_CLAY);
        visuals.add(Material.STAINED_GLASS);
        visuals.add(Material.STAINED_GLASS_PANE);

        return visuals.contains(material);
    }
}
