package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PlayerLogin {
    @Getter private final String name;
    @Getter private final String uuid;
    @Getter private final String ip;
}
