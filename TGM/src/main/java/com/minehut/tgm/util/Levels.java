package com.minehut.tgm.util;

import com.minehut.teamapi.models.UserProfile;
import com.minehut.tgm.TGM;
import com.minehut.tgm.user.PlayerContext;
import org.bukkit.entity.Player;

/**
 * Created by Jorge on 9/29/2017.
 */
public class Levels {

    /**
    *
    * Returns the player level progress in a percentage.
    *
    * @param player target player
    *
    */
    public static double getLevelProgress(Player player){
        UserProfile profile = TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile();
        return (((0.6 * Math.sqrt(profile.getXP())) + 1) - profile.getLevel()) * 100;
    }

    /**
     *
     * Returns the amount of XP the player is missing to level up.
     *
     * @param player target player
     *
     */
    public static int getXPRequiredForNextLevel(Player player){
        return getTotalXPRequiredForNextLevel(player) - TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().getXP();
    }

    /**
     *
     * Returns the amount of total XP needed to reach the next level, not including the current player's XP.
     *
     * @param player target player
     *
     */
    public static int getTotalXPRequiredForNextLevel(Player player){
        return getXPRequiredForLevel(TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().getLevel() + 1);
    }

    /**
     *
     * Returns the total XP needed to get to the specified level.
     *
     * @param level target level
     *
     */
    public static int getXPRequiredForLevel(int l){
        return (int) Math.round(Math.pow((l - 1)/0.6, 2) + 0.49);
    }
}
