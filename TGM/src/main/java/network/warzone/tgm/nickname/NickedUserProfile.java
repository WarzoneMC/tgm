package network.warzone.tgm.nickname;

import lombok.Setter;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import org.bson.types.ObjectId;

import java.util.List;

public class NickedUserProfile extends UserProfile {

    public NickedUserProfile(ObjectId id, String name, String nameLower, String uuid, long initialJoinDate, long lastOnlineDate, List<String> ips, List<String> ranks, List<Rank> ranksLoaded, int wins, int losses, int kills, int deaths, int wool_destroys, List<Punishment> punishments, boolean isNew) {
        super(id, name, nameLower, uuid, initialJoinDate, lastOnlineDate, ips, ranks, ranksLoaded, wins, losses, kills, deaths, wool_destroys, punishments, isNew);
    }

    public static NickedUserProfile createFromUserProfile(UserProfile profile) {
        return new NickedUserProfile(
                profile.getId(),
                profile.getName(),
                profile.getNameLower(),
                profile.getUuid(),
                profile.getInitialJoinDate(),
                profile.getLastOnlineDate(),
                profile.getIps(),
                profile.getRanks(),
                profile.getRanksLoaded(),
                profile.getWins(),
                profile.getLosses(),
                profile.getKills(),
                profile.getDeaths(),
                profile.getWool_destroys(),
                profile.getPunishments(),
                profile.isNew()
        );
    }
}
