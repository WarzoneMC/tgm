package network.warzone.tgm.map;

import com.google.gson.JsonObject;
import network.warzone.tgm.modules.gametypes.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public class MapInfo {
    private String name;
    private String version;
    private List<String> authors;
    private GameType gametype;
    private List<ParsedTeam> teams;
    private JsonObject jsonObject;
}
