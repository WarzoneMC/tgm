package com.minehut.teamapi.client.offline;

import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.TeamRole;
import com.minehut.teamapi.models.UserProfile;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by luke on 4/27/17.
 */
public class OfflineClient implements TeamClient {

    @Override
    public UserProfile login(String name, String uuid, String ip) {
        return new UserProfile(name, name.toLowerCase(), uuid, Arrays.asList(ip), new ArrayList<>(), null, TeamRole.DEFAULT, 0, 0, new ArrayList<>());
    }
}
