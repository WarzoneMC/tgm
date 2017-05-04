package com.minehut.teamapi.client;

import com.minehut.teamapi.models.serverBound.Heartbeat;
import com.minehut.teamapi.models.UserProfile;
import com.minehut.teamapi.models.serverBound.PlayerLogin;

/**
 * Created by luke on 4/27/17.
 */
public interface TeamClient {

    void heartbeat(Heartbeat heartbeat);
    UserProfile login(PlayerLogin playerLogin);

}
