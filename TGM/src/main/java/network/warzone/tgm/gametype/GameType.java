package network.warzone.tgm.gametype;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public enum GameType {

    TDM("Team Death Match", TDMManifest.class),
    KOTH("King of the Hill", KOTHManifest.class),
    DTM("Destroy the Monument", DTMManifest.class),
    CTW("Capture the Wool", CTWManifest.class),
    INFECTION("Infection", InfectionManifest.class),
    BLITZ("Blitz", BlitzManifest.class),
    FFA("Free for All", FFAManifest.class);

    private String name;
    private Class manifest;

}
