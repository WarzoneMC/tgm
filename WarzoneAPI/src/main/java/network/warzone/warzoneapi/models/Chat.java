package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Chat {
    @Getter private String user;
    @Getter private String username;
    @Getter private String uuid;
    @Getter private String message;
    @Getter private String team;
    @Getter private double matchTime;
    @Getter private boolean teamChat;
}
