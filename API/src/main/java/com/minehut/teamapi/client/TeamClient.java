package com.minehut.teamapi.client;

import com.minehut.teamapi.models.UserProfile;

/**
 * Created by luke on 4/27/17.
 */
public interface TeamClient {

    public UserProfile login(String name, String uuid, String ip);
}
