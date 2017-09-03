package in.einfosolutions.koble.Models;

import org.joda.time.DateTime;

/**
 * Created by joker on 1/11/17.
 */
public class EventInfo {

    public String location = "";

    public String pic = "";

    public String type = "";

    public String descp = "";

    public String username = "";

    public String days = "";

    public String event_slug = "";

    public String start = "";

    public String end_time = "";

    public String start_time = "";

    public transient DateTime startDateTime, endDateTime, recurringEnd;

    public String notes = "";

    public String event_name = "";

    public String recurring = "";

    public String end = "";
    
}
