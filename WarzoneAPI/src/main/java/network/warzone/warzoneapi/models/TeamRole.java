package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luke on 4/27/17.
 */

@AllArgsConstructor
public enum TeamRole {
    DEFAULT(0, "Default"),
    OFFICER(1, "Officer"),
    LEADER(2, "Leader");

    @Getter private int id;
    @Getter private String name;
}
