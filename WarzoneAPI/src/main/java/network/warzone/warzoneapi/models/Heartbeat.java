package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@AllArgsConstructor
public class Heartbeat {
    @Getter private final String name;
    @Getter private final String id;
    @Getter private List<String> players;
    @Getter private int playerCount;
    @Getter private int spectatorCount;
    @Getter private int maxPlayers;
    @Getter private String map;
    @Getter private String gametype;

    @Getter private long startedAt;

    @SerializedName("_id")
    @Getter private ObjectId serverId;
}
