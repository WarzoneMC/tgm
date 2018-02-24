package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by Jorge on 2/23/2018.
 */
@AllArgsConstructor
public class RankPermissionsUpdateRequest {
    private String name;
    private List<String> permissions;

    public static enum Action {
        ADD(),
        REMOVE();
    }
}
