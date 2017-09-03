package in.einfosolutions.koble.Models;

import java.util.HashMap;

/**
 * Created by joker on 3/9/17.
 */

public class ChatSettings {

    public boolean chattable = true;

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("chattable", chattable);
        return map;
    }

}
