package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by joker on 5/11/17.
 */

public enum AppoStatus {

    @SerializedName(value = "Accepted")Accepted, @SerializedName("Declined")Declined, @SerializedName(value = "Pending", alternate = "")Pending, @SerializedName("")Resolved, @SerializedName("Resolve Via Chat")ResolveViaChat;

    public String stringVal() {
        switch (this) {
            case Accepted:
                return "Accepted";
            case Declined:
                return "Declined";
            case Pending:
                return "Pending";
            case Resolved:
                return "Resolved";
            case ResolveViaChat:
                return "Resolve Via Chat";
            default:
                return "Pending";
        }
    }

}
