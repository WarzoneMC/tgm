package com.minehut.teamapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public class UserProfile {
    @Getter private String name;
    @Getter private String nameLower;
    @Getter private String uuid;
    @Getter private List<String> ips;
    @Getter private List<ObjectId> tournaments;
    @Getter private ObjectId team;
    @Getter private TeamRole teamRole;
    @Getter private int kills;
    @Getter private int deaths;
    @Getter private List<ObjectId> matches;
}
