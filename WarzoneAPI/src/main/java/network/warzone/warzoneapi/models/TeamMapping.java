package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TeamMapping {
    @Getter private String team; //id
    @Getter private String player; //id
}
