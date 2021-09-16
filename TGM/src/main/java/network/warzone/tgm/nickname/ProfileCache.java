package network.warzone.tgm.nickname;

import lombok.Getter;
import network.warzone.warzoneapi.models.MojangProfile;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Jorge on 10/03/2019
 */
public class ProfileCache extends CopyOnWriteArrayList<MojangProfile> {

    @Getter private static ProfileCache instance = new ProfileCache();

    @Override
    public boolean add(MojangProfile mojangProfile) {
        if (mojangProfile == null) return false;
        if (!contains(mojangProfile.getUuid())) super.add(mojangProfile);
        return true;
    }

    public boolean contains(String name) {
        for (MojangProfile profile : this) {
            if (profile.getUsername().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public boolean contains(UUID uuid) {
        for (MojangProfile profile : this) {
            if (profile.getUuid().equals(uuid)) return true;
        }
        return false;
    }

    public MojangProfile get(String name) {
        for (MojangProfile profile : this) {
            if (profile.getUsername().equalsIgnoreCase(name)) return profile;
        }
        return null;
    }

    public MojangProfile get(UUID uuid) {
        for (MojangProfile profile : this) {
            if (profile.getUuid().equals(uuid)) return profile;
        }
        return null;
    }
}
