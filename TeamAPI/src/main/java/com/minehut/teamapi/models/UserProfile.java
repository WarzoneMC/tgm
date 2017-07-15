package com.minehut.teamapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public class UserProfile {
    @SerializedName("_id")
    @Getter private ObjectId id;

    @Getter private String name;
    @Getter private String nameLower;
    @Getter private String uuid;
    @Getter private long initialJoinDate;
    @Getter private long lastOnlineDate;

    @Getter private List<String> ips;
    @Getter private List<String> ranks;
    @Getter private int kills = 0;
    @Getter private int deaths = 0;
    @Getter private List<String> matches;
}
