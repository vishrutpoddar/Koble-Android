package in.einfosolutions.koble.Models;

/**
 * Created by joker on 1/21/17.
 */

public class MsgOne {

    public String senderId;
    public String receiverId;
    public String message;
    public long timestamp;
    public MsgStatus status = MsgStatus.UNREAD;

    public MsgOne() {
    }

    public MsgOne(String senderId, String receiverId, String message, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
    }
}