package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 03/25/2020
 */
@AllArgsConstructor @Getter
public enum LeaderboardCriterion {
    KILLS("kills") {
        @Override
        public int extract(UserProfile userProfile) {
            return userProfile.getKills();
        }
    }, WINS("wins") {
        @Override
        public int extract(UserProfile userProfile) {
            return userProfile.getWins();
        }
    }, LOSSES("losses") {
        @Override
        public int extract(UserProfile userProfile) {
            return userProfile.getLosses();
        }
    }, XP("XP") {
        @Override
        public int extract(UserProfile userProfile) {
            return userProfile.getXP();
        }
    };

    private String display;

    public abstract int extract(UserProfile userProfile);
}
