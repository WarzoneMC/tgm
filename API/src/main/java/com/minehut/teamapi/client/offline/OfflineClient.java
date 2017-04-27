package com.minehut.teamapi.client.offline;

import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.TeamRole;
import com.minehut.teamapi.models.UserProfile;

import java.util.ArrayList;

/**
 * Created by luke on 4/27/17.
 */
public class OfflineClient implements TeamClient {

    @Override
    public UserProfile login(String name, String uuid) {
        return new UserProfile(name, name.toLowerCase(), uuid, new ArrayList<>(), null, TeamRole.DEFAULT, 0, 0, new ArrayList<>());
    }
}
