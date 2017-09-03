package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by joker on 3/9/17.
 */
public enum UserStatus {

    @SerializedName(value = "ONLINE", alternate = {"1"})
    ONLINE,
    @SerializedName(value = "OFFLINE", alternate = {"0"})
    OFFLINE;

    public String string() {
        switch (this) {
            case ONLINE:
                return "ONLINE";
            case OFFLINE:
                return "OFFLINE";
        }
        return "UNKNOWN";
    }

}
