package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by joker on 5/10/17.
 */

public class NotiModel {

    public String id = "";

    public EventType type = EventType.EVENT;

    public String title = "";

    public String message = "";

    public String user_id = "";

    public String date = "";

    public String event_slug = "";


    public static class NotiRes {
        @SerializedName("array")
        public ArrayList<NotiModel> notiList = new ArrayList<>();
    }

}
