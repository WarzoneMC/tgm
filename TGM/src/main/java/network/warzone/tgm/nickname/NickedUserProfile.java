package network.warzone.tgm.nickname;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.TGM;
import network.warzone.warzoneapi.models.Punishment;
import network.warzone.warzoneapi.models.UserProfile;

public class NickedUserProfile extends UserProfile {

    @Getter @Setter
    private boolean frozen;

    public NickedUserProfile(UserProfile profile) {
        super(profile.getId(),
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
                null,
                profile.isNew());
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
}
