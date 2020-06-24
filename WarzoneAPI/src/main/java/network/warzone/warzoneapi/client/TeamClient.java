package network.warzone.warzoneapi.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import network.warzone.warzoneapi.models.*;

import java.util.UUID;

/**
 * Created by luke on 4/27/17.
 */
public interface TeamClient {

    /**
     * Called every second. Keeps the
     * server up-to-date in the database.
     */
    void heartbeat(Heartbeat heartbeat);

    GetPlayerByNameResponse player(String name);

    /**
     * Called when a player logs into the server.
     */
    UserProfile login(PlayerLogin playerLogin);

    /**
     * Called whenever a map is loaded.
     * Returns the map id.
     */
    MapLoadResponse loadmap(Map map);

    void addKill(Death death);

    MatchInProgress loadMatch(MatchLoadRequest matchLoadRequest);

    void finishMatch(MatchFinishPacket matchFinishPacket);

    void destroyWool(DestroyWoolRequest destroyWoolRequest);
    
    RankList retrieveRanks();

    RankUpdateResponse updateRank(String player, RankUpdateRequest.Action action, RankUpdateRequest rankUpdateRequest);

    RankManageResponse manageRank(RankManageRequest.Action action, RankManageRequest rankManageRequest);

    RankManageResponse editRank(RankEditRequest.EditableField field, RankEditRequest rankEditRequest);

    RankManageResponse editPermissions(RankPermissionsUpdateRequest.Action action, RankPermissionsUpdateRequest permissionsUpdateRequest);

    IssuePunishmentResponse issuePunishment(IssuePunishmentRequest issuePunishmentRequest);

    PunishmentsListResponse getPunishments(PunishmentsListRequest punishmentsListRequest);

    RevertPunishmentResponse revertPunishment(String id);

    PlayerInfoResponse getPlayerInfo(PlayerInfoRequest playerInfoRequest);

    PlayerAltsResponse getAlts(String name);

    PlayerTagsUpdateResponse updateTag(String username, String tag, PlayerTagsUpdateRequest.Action action);

    LeaderboardResponse getLeaderboard(LeaderboardCriterion leaderboardCriterion);

    default MojangProfile getMojangProfile(UUID uuid) {
        return getMojangProfile(uuid.toString());
    }

    default MojangProfile getMojangProfile(String username) {
        try {
            HttpResponse<MojangProfile> response = Unirest.get("https://api.ashcon.app/mojang/v2/user/" + username)
                    .asObject(MojangProfile.class);
            if (response.getStatus() != 200) {
                return null;
            }
            return response.getBody();
        } catch (UnirestException e) {
            return null;
        }
    }

}
