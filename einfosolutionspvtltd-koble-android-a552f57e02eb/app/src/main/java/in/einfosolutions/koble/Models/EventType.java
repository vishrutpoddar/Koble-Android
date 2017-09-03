package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by joker on 1/2/17.
 */

public enum EventType {

    @SerializedName("appo")
    APPO,
    @SerializedName("event")
    EVENT,
    @SerializedName("class")
    CLASS,
    @SerializedName("Office")
    OFFICE,
    @SerializedName("iCal")
    ICAL,
    @SerializedName("google")
    GOOGLE;

    public String string() {
        switch (this) {
            case APPO:
                return "appo";
            case EVENT:
                return "event";
            case CLASS:
                return "class";
            case OFFICE:
                return "Office";
            case ICAL:
                return "iCal";
            case GOOGLE:
                return "google";
            default:
                return "event";
        }

    }

    public String stringVal() {
        switch (this) {
            case APPO:
                return "Appointment";
            case EVENT:
                return "Event";
            case CLASS:
                return "Class";
            case OFFICE:
                return "Office";
            case ICAL:
                return "iCal";
            case GOOGLE:
                return "Google";
            default:
                return "Event";
        }
    }

}
