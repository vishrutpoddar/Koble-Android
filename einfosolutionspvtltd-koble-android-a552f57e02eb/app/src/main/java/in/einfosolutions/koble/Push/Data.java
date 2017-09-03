package in.einfosolutions.koble.Push;

import android.os.Bundle;

import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by joker on 1/24/17.
 */
public class Data {

    public String chat_type = "Type", sender_id = "id";

    public Data(String chat_type, String sender_id) {
        this.chat_type = chat_type;
        this.sender_id = sender_id;
    }

    public Data(Bundle extras) {
        try {
            chat_type = extras.getString("chat_type");
            sender_id = extras.getString("sender_id");
        } catch (Exception e) {
            FirebaseCrash.report(e);
        }
    }
}
