package com.minehut.teamapi.client.offline;

import com.minehut.teamapi.client.TeamClient;
import com.minehut.teamapi.models.Kill;
import com.minehut.teamapi.models.Map;
import com.minehut.teamapi.models.UserProfile;
import com.minehut.teamapi.models.Heartbeat;
import com.minehut.teamapi.models.PlayerLogin;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by luke on 4/27/17.
 */
public class OfflineClient implements TeamClient {

    @Override
    public void heartbeat(Heartbeat heartbeat) {

    }

    @Override
    public UserProfile login(PlayerLogin playerLogin) {
        return new UserProfile(new ObjectId().toString(), playerLogin.getName(), playerLogin.getName().toLowerCase(),
                playerLogin.getUuid(), new Date().getTime(), new Date().getTime(), Arrays.asList(playerLogin.getIp()), new ArrayList<>(),
                0, 0, new ArrayList<>());
    }

    @Override
    public ObjectId loadmap(Map map) {
        return null;
    }

    @Override
    public void addKill(Kill kill) {

    }
}
