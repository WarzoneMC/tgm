package network.warzone.tgm.rank;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Rank;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 10/22/2017.
 */
public class RankManager {

    @Getter private List<Rank> loadedRanks = new ArrayList<>();

    public void retrieveRanks() {
        loadedRanks = TGM.get().getTeamClient().retrieveRanks();
        TGM.get().getLogger().info("Loaded " + loadedRanks.size() + " ranks");
    }

    public List<Rank> getRanks(List<String> stringRanks) {
        List<Rank> ranks = new ArrayList<>();
        for (String stringRank : stringRanks) {
            for (Rank rank : loadedRanks) {
                if (rank.getId().toString().equals(stringRank) && !ranks.contains(rank)) ranks.add(rank);
            }
        }
        return ranks;
    }

    public List<Rank> getRanks(PlayerContext playerContext) {
        return getRanks(playerContext.getUserProfile().getRanks());
    }

}
