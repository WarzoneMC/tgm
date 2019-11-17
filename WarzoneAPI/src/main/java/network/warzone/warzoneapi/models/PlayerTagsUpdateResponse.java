package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Created by Jorge on 11/01/2019
 */
@AllArgsConstructor @Data
public class PlayerTagsUpdateResponse {

    private boolean error;
    private String message;
    private List<String> tags;
    private String activeTag;

}
