package network.warzone.tgm.nickname;

import network.warzone.tgm.TGM;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import org.bson.types.ObjectId;

import java.util.List;

public class NickedUserProfile extends UserProfile {

    private NickedUserProfile(ObjectId id, String name, String nameLower, String uuid, long initialJoinDate, long lastOnlineDate, List<String> ips, List<String> ranks, List<Rank> ranksLoaded, int wins, int losses, int kills, int deaths, int wool_destroys, List<Punishment> punishments, boolean isNew) {
        super(id, name, nameLower, uuid, initialJoinDate, lastOnlineDate, ips, ranks, ranksLoaded, wins, losses, kills, deaths, wool_destroys, punishments, isNew);
    }

    @Override
    public void addKill() {
        super.addKill();
        TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addKill();
    }

    @Override
    public void addDeath() {
        super.addDeath();
        TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addDeath();
    }

    @Override
    public void addLoss() {
        super.addLoss();
        TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addLoss();
    }

    @Override
    public void addWin() {
        super.addWin();
        TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addWin();
    }

    @Override
    public void addWoolDestroy() {
        super.addWoolDestroy();
        TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addWoolDestroy();
    }

    @Override
    public void addPunishment(Punishment punishment) {
        super.addPunishment(punishment);
        TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addPunishment(punishment);
    }

    @Override
    public boolean isStaff() {
        return TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).isStaff();
    }

    static NickedUserProfile createFromUserProfile(UserProfile profile) {
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
