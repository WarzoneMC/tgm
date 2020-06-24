package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Team {
    @Getter private String id;
    @Getter private String name;
    @Getter private String color;
    @Getter private int min;
    @Getter private int max;
}
