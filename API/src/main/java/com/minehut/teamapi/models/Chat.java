package com.minehut.teamapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Chat {
    @Getter private String user;
    @Getter private String username;
    @Getter private String message;
    @Getter private String team;
    @Getter private int matchTime;
    @Getter private boolean teamChat;
}
