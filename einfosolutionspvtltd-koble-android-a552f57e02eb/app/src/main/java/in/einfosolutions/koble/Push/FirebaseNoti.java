package in.einfosolutions.koble.Push;

/**
 * Created by joker on 1/24/17.
 */

/**
 * Notification body for sendting to server
 */
public class FirebaseNoti {

    public String to = "";
    public String priority = "High";
    public Data data;
    public Noti notification;

    public FirebaseNoti(String toToken, String title, String msg, String type, String id) {
        this.to = toToken;
        notification = new Noti(msg, title);
        data = new Data(type, id);
    }

}
