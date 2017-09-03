package in.einfosolutions.koble.Models;

import in.einfosolutions.koble.Activities.ChatActivity;

/**
 * Created by joker on 1/23/17.
 */
public class FcmChatProps {

    //public boolean mute = false;
    public ChatActivity.ChatType type = ChatActivity.ChatType.SINGLE;
    public String name = "Group";
    public long new_count = 0;

    public FcmChatProps() {
    }

}
