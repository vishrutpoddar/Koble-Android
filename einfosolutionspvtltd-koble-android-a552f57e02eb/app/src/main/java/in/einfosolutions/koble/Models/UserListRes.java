package in.einfosolutions.koble.Models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by joker on 1/3/17.
 */

public class UserListRes {

    public String status = "";

    @SerializedName("array")
    public ArrayList<ProfileModel> userList = new ArrayList<>();

}
