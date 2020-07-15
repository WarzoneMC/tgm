package network.warzone.tgm.nickname;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class NickedUserProfile extends UserProfile {

    @Getter @Setter
    private boolean frozen;

    private NickedUserProfile(ObjectId id, String name, String nameLower, String uuid, long initialJoinDate, long lastOnlineDate, List<String> ips, List<String> ranks, List<Rank> ranksLoaded, int wins, int losses, int kills, int deaths, int wool_destroys, List<Punishment> punishments, List<String> tags, boolean isNew) {
        super(id, name, nameLower, uuid, initialJoinDate, lastOnlineDate, ips, ranks, ranksLoaded, wins, losses, kills, deaths, wool_destroys, punishments, tags, null, isNew);
        this.frozen = false;
    }

    @Override
    public void addKill() {
        super.addKill();
        if (!frozen) TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addKill();
    }

    @Override
    public void addDeath() {
        super.addDeath();
        if (!frozen) TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addDeath();
    }

    @Override
    public void addLoss() {
        super.addLoss();
        if (!frozen) TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addLoss();
    }

    @Override
    public void addWin() {
        super.addWin();
        if (!frozen) TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addWin();
    }

    @Override
    public void addWoolDestroy() {
        super.addWoolDestroy();
        if (!frozen) TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addWoolDestroy();
    }

    @Override
    public void addPunishment(Punishment punishment) {
        super.addPunishment(punishment);
        if (!frozen) TGM.get().getPlayerManager().getPlayerContext(getUuid()).getUserProfile(true).addPunishment(punishment);
    }

    @Override
    public boolean isStaff() {
        return super.isStaff();
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
                profile.getTags(),
                profile.isNew()
        );
    }
}
