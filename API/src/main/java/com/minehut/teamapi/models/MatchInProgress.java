package com.minehut.teamapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MatchInProgress {

    @SerializedName("_id")
    @Getter private String id;

    @Getter private String map; //id
}