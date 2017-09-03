package in.einfosolutions.koble.Models;

/**
 * Created by joker on 1/21/17.
 */

public class MsgGroup {

    public String senderId;
    public String message;
    public long timestamp;
    public MsgStatus status = MsgStatus.UNREAD;

    public MsgGroup() {
    }

    public MsgGroup(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }
}