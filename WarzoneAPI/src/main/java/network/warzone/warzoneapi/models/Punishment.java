package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.UUID;

@Getter
public class Punishment {

    @SerializedName("_id")
    private ObjectId id;

    private ObjectId punisher;
    private ObjectId punished;

    private String type;

    private long issued;
    private long expires;

    private String reason;

    private boolean reverted;

}
