package com.minehut.teamapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.text.NumberFormat;
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
    @Getter private int wins = 0;
    @Getter private int losses = 0;
    @Getter private int kills = 0;
    @Getter private int deaths = 0;
    @Getter private int wool_destroys = 0;
    @Getter private List<String> matches;

    public void addWin() {
        wins++;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addLoss() {
        losses++;
    }

    public void addWoolDestroy() {
        wool_destroys++;
    }

    public int getXP() {
        return (getWins() * 10) + (getLosses() * 5) + (getWool_destroys() * 7) + getKills();
    }

    public int getLevel() {
        return (int) getLevelRaw();
    }

    public double getLevelRaw() {
        return (0.6 * Math.sqrt(getXP())) + 1;
    }

    public String getKDR() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        if (getDeaths() == 0) return nf.format((double) getKills());
        return nf.format((double) getKills()/getDeaths());
    }

    public String getWLR() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        if (getLosses() == 0) return nf.format((double) getWins());
        return nf.format((double) getWins()/getLosses());
    }
}
