package com.minehut.teamapi.client.http;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.Kill;
import com.minehut.teamapi.models.Map;
import com.minehut.teamapi.models.Heartbeat;
import com.minehut.teamapi.models.UserProfile;
import com.minehut.teamapi.models.PlayerLogin;
import lombok.Getter;
import org.bson.types.ObjectId;

/**
 * Created by luke on 4/27/17.
 */
public class HttpClient implements TeamClient {
    @Getter private HttpClientConfig config;
    @Getter private final Gson gson;

    public HttpClient(HttpClientConfig config) {
        this.config = config;
        this.gson = new Gson();

        //serialize objects using gson
        Unirest.setObjectMapper(new ObjectMapper() {

            public <T> T readValue(String s, Class<T> aClass) {
                try{
                    return gson.fromJson(s, aClass);
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object o) {
                try{
                    return gson.toJson(o);
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void heartbeat(Heartbeat heartbeat) {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(config.getBaseUrl() + "/mc/server/heartbeat")
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
    public ObjectId loadmap(Map map) {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(config.getBaseUrl() + "/mc/map/load")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(map)
                    .asJson();
            return new ObjectId((String) jsonResponse.getBody().getObject().get("map"));
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void addKill(Kill kill) {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(config.getBaseUrl() + "/mc/player/kill")
                    .header("x-access-token", config.getAuthToken())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(kill)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
