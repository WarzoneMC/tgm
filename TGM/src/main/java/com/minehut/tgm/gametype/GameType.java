package com.minehut.tgm.gametype;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public enum GameType {
    TDM("TDM", TDMManifest.class),
    KOTH("King of the Hill", KOTHManifest.class),
    DTM("Destroy the Monument", DTMManifest.class),
    CTW("Capture the Wool", CTWManifest.class),
    Infected("Infection", InfectionManifest.class),
    Blitz("Blitz", BlitzManifest.class);

    @Getter private String name;
    @Getter private Class manifest;
}
