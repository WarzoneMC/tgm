package network.warzone.warzoneapi.models;

import lombok.Getter;

/**
 * Created by Jorge on 2/22/2018.
 */
@Getter
public class RankUpdateResponse {

    private String message;
    private boolean error;
    private Rank rank;

}
