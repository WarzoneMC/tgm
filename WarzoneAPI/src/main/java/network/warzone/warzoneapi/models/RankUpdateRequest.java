package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

/**
 * Created by Jorge on 10/22/2017.
 */
@AllArgsConstructor
public class RankUpdateRequest {

    @Getter private ObjectId rankId;
    @Getter private String rankName;

    @AllArgsConstructor
    public static enum Action {
        ADD(),
        REMOVE();
    }

}
