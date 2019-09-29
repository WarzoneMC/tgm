package network.warzone.warzoneapi.client;

import network.warzone.warzoneapi.models.*;

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

    void purchaseClass(ClassPurchaseRequest request);
    
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

    KillsLeaderboardResponse getKillsLeaderboard();

}
