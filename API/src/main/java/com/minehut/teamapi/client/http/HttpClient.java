package com.minehut.teamapi.client.http;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by luke on 4/27/17.
 */
public class HttpClient implements TeamClient {
    @Getter private HttpClientConfig config;

    public HttpClient(HttpClientConfig config) {
        this.config = config;

        //serialize objects using gson
        Unirest.setObjectMapper(new ObjectMapper() {
            private Gson gson = new Gson();

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
    public UserProfile login(String name, String uuid, String ip) {
        try {
            HttpResponse<UserProfile> userProfileResponse = Unirest.post(config.getBaseUrl() + "/player/login")
                    .queryString("name", name)
                    .queryString("uuid", uuid)
                    .queryString("ip", ip)
                    .asObject(UserProfile.class);
            return userProfileResponse.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }
}
