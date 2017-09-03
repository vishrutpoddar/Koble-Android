package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by joker on 1/30/17.
 */
public enum MsgStatus {
    @SerializedName("READ")READ,
    @SerializedName("UNREAD")UNREAD;
    public String string () {
        switch (this) {
            case READ: return "READ";
            case UNREAD: return "UNREAD";
        }
        return "UNKNOWN";
    }
}
