package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Created by Jorge on 09/08/2019
 */
@Getter @AllArgsConstructor
public class Author {

    public Author(UUID uuid) {
        this(uuid, null, null);
    }

    public Author(String username) {
        this(null, username, null);
    }

    private final UUID uuid;
    private final String username; // Fallback
    @Setter private String displayUsername;
}
