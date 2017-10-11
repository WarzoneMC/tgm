package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class MatchFinishPacket {
    @Getter private String id; //id
    @Getter private String map; //id
    @Getter private long startedDate;
    @Getter private long finishedDate;
    @Getter private List<Chat> chat;
    @Getter private List<String> winners; //id
    @Getter private List<String> losers; //id
    @Getter private String winningTeam; //map defined team name
    @Getter private List<TeamMapping> teamMappings;
}
