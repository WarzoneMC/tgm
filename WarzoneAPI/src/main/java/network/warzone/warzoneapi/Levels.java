package network.warzone.warzoneapi;

import network.warzone.warzoneapi.models.UserProfile;

/**
 * Created by Jorge on 9/29/2017.
 */
public class Levels {

    /**
    *
    * Returns the player level progress as a percentage, between 0.00 (inclusive) and 1.00 (exclusive).
    *
    * @param profile target player profile
    *
    */
    public static double getLevelProgress(UserProfile profile) {
        return ((0.6 * Math.sqrt(profile.getXP())) + 1) - profile.getLevel();
    }

    /**
     *
     * Returns the amount of XP the player is missing to level up.
     *
     * @param profile target player profile
     *
     */
    public static int getXPRequiredForNextLevel(UserProfile profile) {
        return getTotalXPRequiredForNextLevel(profile) - profile.getXP();
    }

    /**
     *
     * Returns the amount of total XP needed to reach the next level, not including the current player's XP.
     *
     * @param profile target player profile
     *
     */
    public static int getTotalXPRequiredForNextLevel(UserProfile profile) {
        return getXPRequiredForLevel(profile.getLevel() + 1);
    }

    /**
     *
     * Returns the total XP needed to get to the specified level.
     *
     * @param l target level
     *
     */
    public static int getXPRequiredForLevel(int l) {
        return (int) Math.round(Math.pow((l - 1)/0.6, 2) + 0.49);
    }

    public static int calculateLevel(int XP) {
        return (int) (0.6 * Math.sqrt(XP)) + 1;
    }
}
