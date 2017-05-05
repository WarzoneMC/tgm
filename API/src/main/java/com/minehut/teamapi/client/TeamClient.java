package com.minehut.teamapi.client;

import com.minehut.teamapi.models.Kill;
import com.minehut.teamapi.models.Map;
import com.minehut.teamapi.models.serverBound.Heartbeat;
import com.minehut.teamapi.models.UserProfile;
import com.minehut.teamapi.models.serverBound.PlayerLogin;
import org.bson.types.ObjectId;

/**
 * Created by luke on 4/27/17.
 */
public interface TeamClient {

    /**
     * Called every second. Keeps the
     * server up-to-date in the database.
     */
    void heartbeat(Heartbeat heartbeat);

    /**
     * Called when a player logs into the server.
     */
    UserProfile login(PlayerLogin playerLogin);

    /**
     * Called whenever a map is loaded.
     */
    ObjectId loadmap(Map map);

    void addKill(Kill kill);

}
