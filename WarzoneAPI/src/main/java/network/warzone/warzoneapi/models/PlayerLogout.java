package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PlayerLogout {
    @Getter private final String name;
    @Getter private final String uuid;
}
