package in.einfosolutions.koble.Models;

import java.util.Date;

/**
 * Created by joker on 1/2/17.
 */

public class AddEventInput {

    // input
    public EventType eventType = EventType.EVENT;

    // selected dates // input startDateTime only
    public Date startDateTime, endDateTime, recurringDate;

    public ProfileModel professorModel;

    public EventModel eventModel;
}
