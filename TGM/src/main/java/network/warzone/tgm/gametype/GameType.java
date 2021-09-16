package network.warzone.tgm.gametype;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public enum GameType {
    TDM("Team Deathmatch", TDMManifest.class, "Fight alongside your team to kill as many enemies as possible while preventing the other team to achieve the same."),
    KOTH("King of the Hill", KOTHManifest.class, "Capture hills and defend them from the other teams to gain points for your own. The first team to complete the points goal wins."),
    CP("Capture Points", CPManifest.class, "Capture the points in the correct order to win."),
    DTM("Destroy the Monument", DTMManifest.class, "Destroy the enemy's monument located on their side of the map. Make sure your monument is being defended from intruders."),
    CTW("Capture the Wool", CTWManifest.class, "Capture the other team's wool from their base and return it to your base's podium. Don't forget to defend your team's wool from your enemies."),
    Infected("Infection", InfectionManifest.class, "Try not to get killed by infected. Humans win by default after the timer ends."),
    Blitz("Blitz", BlitzManifest.class, "You have a limited amount of lives before you are eliminated, fight and kill the other team without getting eliminated."),
    FFA("Free for All", FFAManifest.class, "Every player for themselves, kill the most players and and be the first one to reach the goal."),
    CTF("Capture the Flag", CTFManifest.class, "Capture the enemy's flag from their base and return the flag back to your base before they can steal yours."),
    KOTF("King of the Flag", KOTFManifest.class, "Gain points for your team by capturing the flag. Win by having the most points when the timer ends.");

    private String name;
    private Class manifest;
    private String objective;
}
