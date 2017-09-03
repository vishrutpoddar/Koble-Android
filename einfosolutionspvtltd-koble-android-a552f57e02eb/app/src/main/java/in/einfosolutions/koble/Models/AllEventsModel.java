package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AllEventsModel {

    @SerializedName("array")
    public ArrayList<EventModel> events = new ArrayList<>();

    @SerializedName("events_string")
    public String events_string = "";

    @SerializedName("google_string")
    public String google_string = "";
}
