package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;

/**
 * Created by Jorge on 11/01/2019
 */

@AllArgsConstructor
public class PlayerTagsUpdateRequest {
    private String tag;

    public enum Action {
        ADD, REMOVE, SET
    }
}
