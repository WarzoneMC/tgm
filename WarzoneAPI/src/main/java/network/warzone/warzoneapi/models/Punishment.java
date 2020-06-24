package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;

@Getter
public class Punishment {

    @SerializedName("_id")
    private ObjectId id;

    private ObjectId punisher;
    private ObjectId punished;

    private String type;

    private String ip;
    private boolean ip_ban;

    private long issued;
    private long expires;

    private String reason;

    @Setter private boolean reverted;

    public boolean isActive() {
        if (this.reverted) {
            return false;
        } else {
            if (this.expires == -1) {
                return true;
            } else {
                return this.expires > new Date().getTime();
            }
        }
    }

}
