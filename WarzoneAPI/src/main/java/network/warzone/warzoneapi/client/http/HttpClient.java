package network.warzone.warzoneapi.client.http;

import com.google.gson.*;
import kong.unirest.*;
import lombok.Getter;
import network.warzone.warzoneapi.client.TeamClient;
import network.warzone.warzoneapi.models.*;
import org.bson.types.ObjectId;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class HttpClient extends TeamClient {

    private HttpClientConfig config;

    public HttpClient(HttpClientConfig config) {
        super();
        this.config = config;
    }

    @Override
    public void heartbeat(Heartbeat heartbeat) {
        try {
            unirest.post(config.getBaseUrl() + "/mc/server/heartbeat")
                    .header("x-access-token", config.getAuthToken())
                    .body(heartbeat)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GetPlayerByNameResponse player(String name) {
        try {
            return unirest.get(config.getBaseUrl() + "/mc/player/" + name)
                    .asObject(GetPlayerByNameResponse.class).getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserProfile login(PlayerLogin playerLogin) {
        try {
            HttpResponse<UserProfile> userProfileResponse = unirest.post(config.getBaseUrl() + "/mc/player/login")
                    .header("x-access-token", config.getAuthToken())
                    .body(playerLogin)
                    .asObject(UserProfile.class);
            return userProfileResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MapLoadResponse loadmap(Map map) {
        try {
            HttpResponse<MapLoadResponse> mapLoadResponse = unirest.post(config.getBaseUrl() + "/mc/map/load")
                    .header("x-access-token", config.getAuthToken())
                    .body(map)
                    .asObject(MapLoadResponse.class);
            return mapLoadResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addKill(Death death) {
        try {
            unirest.post(config.getBaseUrl() + "/mc/death/new")
                    .header("x-access-token", config.getAuthToken())
                    .body(death)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MatchInProgress loadMatch(MatchLoadRequest matchLoadRequest) {
        try {
            HttpResponse<MatchInProgress> userProfileResponse = unirest.post(config.getBaseUrl() + "/mc/match/load")
                    .header("x-access-token", config.getAuthToken())
                    .body(matchLoadRequest)
                    .asObject(MatchInProgress.class);
            return userProfileResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void finishMatch(MatchFinishPacket matchFinishPacket) {
        try {
            unirest.post(config.getBaseUrl() + "/mc/match/finish")
                    .header("x-access-token", config.getAuthToken())
                    .body(matchFinishPacket)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroyWool(DestroyWoolRequest destroyWoolRequest) {
        try {
            unirest.post(config.getBaseUrl() + "/mc/match/destroy_wool")
                    .header("x-access-token", config.getAuthToken())
                    .body(destroyWoolRequest)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LeaderboardResponse getLeaderboard(LeaderboardCriterion leaderboardCriterion) {
        try {
            HttpResponse<LeaderboardResponse> response = unirest.get(config.getBaseUrl() + "/mc/leaderboard/" + leaderboardCriterion.name().toLowerCase() + "?limit=10")
                    .header("x-access-token", config.getAuthToken())
                    .asObject(LeaderboardResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return new LeaderboardResponse();
        }
    }

    @Override
    public RankList retrieveRanks() {
        try {
            HttpResponse<RankList> ranksResponse = unirest.get(config.getBaseUrl() + "/mc/ranks")
                    .asObject(RankList.class);
            return ranksResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return new RankList();
        }
    }

    @Override
    public RankUpdateResponse updateRank(String name, RankUpdateRequest.Action action, RankUpdateRequest rankUpdateRequest) {
        try {
            HttpResponse<RankUpdateResponse> response = unirest.post(config.getBaseUrl() + "/mc/player/" + name + "/rank/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .body(rankUpdateRequest)
                    .asObject(RankUpdateResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public RankManageResponse manageRank(RankManageRequest.Action action, RankManageRequest rankManageRequest) {
        try {
            HttpResponse<RankManageResponse> response = unirest.post(config.getBaseUrl() + "/mc/rank/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .body(rankManageRequest)
                    .asObject(RankManageResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public RankManageResponse editRank(RankEditRequest.EditableField field, RankEditRequest rankEditRequest) {
        try {
            HttpResponse<RankManageResponse> response = unirest.post(config.getBaseUrl() + "/mc/rank/set/" + field.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .body(rankEditRequest)
                    .asObject(RankManageResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public RankManageResponse editPermissions(RankPermissionsUpdateRequest.Action action, RankPermissionsUpdateRequest permissionsUpdateRequest) {
        try {
            HttpResponse<RankManageResponse> response = unirest.post(config.getBaseUrl() + "/mc/rank/permissions/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .body(permissionsUpdateRequest)
                    .asObject(RankManageResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createReport(ReportCreateRequest reportCreateRequest) {
        try {
            unirest.post(config.getBaseUrl() + "/mc/report/create").header("x-access-token", config.getAuthToken()).body(reportCreateRequest).asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public IssuePunishmentResponse issuePunishment(IssuePunishmentRequest issuePunishmentRequest) {
        try {
            HttpResponse<IssuePunishmentResponse> response = unirest.post(config.getBaseUrl() + "/mc/player/issue_punishment")
                    .header("x-access-token", config.getAuthToken())
                    .body(issuePunishmentRequest)
                    .asObject(IssuePunishmentResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PunishmentsListResponse getPunishments(PunishmentsListRequest punishmentsListRequest) {
        try {
            HttpResponse<PunishmentsListResponse> response = unirest.post(config.getBaseUrl() + "/mc/player/punishments")
                    .header("x-access-token", config.getAuthToken())
                    .body(punishmentsListRequest)
                    .asObject(PunishmentsListResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public RevertPunishmentResponse revertPunishment(String id) {
        try {
            HttpResponse<RevertPunishmentResponse> response = unirest.post(config.getBaseUrl() + "/mc/player/revert_punishment")
                    .header("x-access-token", config.getAuthToken())
                    .body(new RevertPunishmentRequest(new ObjectId(id)))
                    .asObject(RevertPunishmentResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ObjectID: " + id);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PlayerInfoResponse getPlayerInfo(PlayerInfoRequest playerInfoRequest) {
        try {
            HttpResponse<PlayerInfoResponse> response = unirest.post(config.getBaseUrl() + "/mc/player/lookup")
                    .header("x-access-token", config.getAuthToken())
                    .body(playerInfoRequest)
                    .asObject(PlayerInfoResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PlayerAltsResponse getAlts(String name) {
        try {
            HttpResponse<PlayerAltsResponse> response = unirest.get(config.getBaseUrl() + "/mc/player/alts/" + name)
                    .header("x-access-token", config.getAuthToken())
                    .asObject(PlayerAltsResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PlayerTagsUpdateResponse updateTag(String username, String tag, PlayerTagsUpdateRequest.Action action) {
        try {
            HttpResponse<PlayerTagsUpdateResponse> response = unirest.post(config.getBaseUrl() + "/mc/player/" + username + "/tags/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .body(new PlayerTagsUpdateRequest(tag))
                    .asObject(PlayerTagsUpdateResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }
}
