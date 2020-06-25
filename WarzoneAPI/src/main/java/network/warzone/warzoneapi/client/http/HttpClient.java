package network.warzone.warzoneapi.client.http;

import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.Getter;
import network.warzone.warzoneapi.client.TeamClient;
import network.warzone.warzoneapi.models.*;
import org.bson.types.ObjectId;

/**
 * Created by luke on 4/27/17.
 */
@Getter
public class HttpClient implements TeamClient {

    private HttpClientConfig config;
    private final Gson gson;

    public HttpClient(HttpClientConfig config) {
        this.config = config;


        GsonBuilder builder = new GsonBuilder();

        // ObjectId
        builder.registerTypeAdapter(ObjectId.class, (JsonDeserializer<ObjectId>) (json, typeOfT, context) -> new ObjectId(json.getAsJsonPrimitive().getAsString()));
        builder.registerTypeAdapter(ObjectId.class, (JsonSerializer<ObjectId>) (src, typeOfT, context) -> new JsonPrimitive(src.toString()));

        builder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);

        this.gson = builder.create();

        //serialize objects using gson
        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String s, Class<T> aClass) {
                try {
                    return gson.fromJson(s, aClass);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object o) {
                try {
                    return gson.toJson(o);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void heartbeat(Heartbeat heartbeat) {
        try {
            Unirest.post(config.getBaseUrl() + "/mc/server/heartbeat")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(heartbeat)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GetPlayerByNameResponse player(String name) {
        try {
            return Unirest.get(config.getBaseUrl() + "/mc/player/" + name)
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .asObject(GetPlayerByNameResponse.class).getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public UserProfile login(PlayerLogin playerLogin) {
        try {
            HttpResponse<UserProfile> userProfileResponse = Unirest.post(config.getBaseUrl() + "/mc/player/login")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<MapLoadResponse> mapLoadResponse = Unirest.post(config.getBaseUrl() + "/mc/map/load")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            Unirest.post(config.getBaseUrl() + "/mc/death/new")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(death)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MatchInProgress loadMatch(MatchLoadRequest matchLoadRequest) {
        try {
            HttpResponse<MatchInProgress> userProfileResponse = Unirest.post(config.getBaseUrl() + "/mc/match/load")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            Unirest.post(config.getBaseUrl() + "/mc/match/finish")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(matchFinishPacket)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroyWool(DestroyWoolRequest destroyWoolRequest) {
        try {
            Unirest.post(config.getBaseUrl() + "/mc/match/destroy_wool")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(destroyWoolRequest)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public LeaderboardResponse getLeaderboard(LeaderboardCriterion leaderboardCriterion) {
        try {
            HttpResponse<LeaderboardResponse> response = Unirest.get(config.getBaseUrl() + "/mc/leaderboard/" + leaderboardCriterion.name().toLowerCase() + "?limit=10")
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .asObject(LeaderboardResponse.class);
            System.out.println(response.getBody());
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return new LeaderboardResponse();
        }
    }

    @Override
    public RankList retrieveRanks() {
        try {
            HttpResponse<RankList> ranksResponse = Unirest.get(config.getBaseUrl() + "/mc/ranks")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<RankUpdateResponse> response = Unirest.post(config.getBaseUrl() + "/mc/player/" + name + "/rank/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<RankManageResponse> response = Unirest.post(config.getBaseUrl() + "/mc/rank/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<RankManageResponse> response = Unirest.post(config.getBaseUrl() + "/mc/rank/set/" + field.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<RankManageResponse> response = Unirest.post(config.getBaseUrl() + "/mc/rank/permissions/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(permissionsUpdateRequest)
                    .asObject(RankManageResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    public IssuePunishmentResponse issuePunishment(IssuePunishmentRequest issuePunishmentRequest) {
        try {
            HttpResponse<IssuePunishmentResponse> response = Unirest.post(config.getBaseUrl() + "/mc/player/issue_punishment")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<PunishmentsListResponse> response = Unirest.post(config.getBaseUrl() + "/mc/player/punishments")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<RevertPunishmentResponse> response = Unirest.post(config.getBaseUrl() + "/mc/player/revert_punishment")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<PlayerInfoResponse> response = Unirest.post(config.getBaseUrl() + "/mc/player/lookup")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<PlayerAltsResponse> response = Unirest.get(config.getBaseUrl() + "/mc/player/alts/" + name)
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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
            HttpResponse<PlayerTagsUpdateResponse> response = Unirest.post(config.getBaseUrl() + "/mc/player/" + username + "/tags/" + action.name().toLowerCase())
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(new PlayerTagsUpdateRequest(tag))
                    .asObject(PlayerTagsUpdateResponse.class);
            return response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

}