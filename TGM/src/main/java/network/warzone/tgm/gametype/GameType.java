package network.warzone.tgm.gametype;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public enum GameType {
    TDM("TDM", TDMManifest.class),
    KOTH("King of the Hill", KOTHManifest.class),
    DTM("Destroy the Monument", DTMManifest.class),
    CTW("Capture the Wool", CTWManifest.class),
    Infected("Infection", InfectionManifest.class),
    Blitz("Blitz", BlitzManifest.class);

    private String name;
    private Class manifest;
}
