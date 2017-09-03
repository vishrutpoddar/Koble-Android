package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by joker on 1/24/17.
 */
public enum  NotiType {

    @SerializedName("SINGLE")
    SINGLE,
    @SerializedName("GROUP")
    GROUP;

    public String string() {
        switch (this) {
            case SINGLE:
                return "SINGLE";
            case GROUP:
                return "GROUP";
            default:
                return "SINGLE";
        }

    }

}
