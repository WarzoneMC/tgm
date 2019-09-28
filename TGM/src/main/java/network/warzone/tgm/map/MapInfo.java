package network.warzone.tgm.map;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.gametype.GameType;
import network.warzone.warzoneapi.models.Author;

import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public class MapInfo {
    private String name;
    private String version;
    private List<Author> authors;
    private GameType gametype;
    private List<ParsedTeam> teams;
    private boolean usingLegacyKits;
    private JsonObject jsonObject;
}
