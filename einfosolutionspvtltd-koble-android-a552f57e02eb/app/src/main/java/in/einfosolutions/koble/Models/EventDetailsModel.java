package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class EventDetailsModel {

    @SerializedName("event_slug")
    public String event_slug = "";

    @SerializedName("event_name")
    public String event_name = "";

    @SerializedName("start")
    public String start_date = "";

    public DateTime startDateTime, endDateTime, recurringEnd;

    @SerializedName("end")
    public String end_date = "";

    @SerializedName("descp")
    public String description = "";

    @SerializedName("pic")
    public String pic = "";

    @SerializedName("username")
    public String username = "";

    @SerializedName("start_time")
    public String start_time = "";

    @SerializedName("end_time")
    public String end_time = "";

    @SerializedName("type")
    public EventType event_type;

    @SerializedName("location")
    public String location = "";

    @SerializedName("recurring")
    public boolean recurring;

    @SerializedName("days")
    public String recurringDays = "";

    @SerializedName("notes")
    public String notes = "";

}
