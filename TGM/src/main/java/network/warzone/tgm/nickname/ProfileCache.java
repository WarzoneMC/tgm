package network.warzone.tgm.nickname;

import lombok.Getter;
import network.warzone.warzoneapi.models.MojangProfile;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Jorge on 10/03/2019
 */
public class ProfileCache extends ArrayList<MojangProfile> {

    @Getter private static ProfileCache instance = new ProfileCache();

    private ProfileCache() {
    }

    @Override
    public boolean add(MojangProfile mojangProfile) {
        while (this.size() >= 20) this.remove(0);
        return super.add(mojangProfile);
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
