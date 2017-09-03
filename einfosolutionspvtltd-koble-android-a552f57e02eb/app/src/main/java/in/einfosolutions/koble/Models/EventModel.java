package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class EventModel {

    @SerializedName("event_details")
    public EventDetailsModel eventDetails;

    @SerializedName("tagged_user")
    public ArrayList<ProfileModel> taggedUsers = new ArrayList<>();

    @SerializedName("tagged_by")
    public ArrayList<ProfileModel> taggedBy = new ArrayList<>();

    @SerializedName("status")
    public AppoStatus status = AppoStatus.Pending;

    @SerializedName("class")
    public ClassModel classDetails;
}
