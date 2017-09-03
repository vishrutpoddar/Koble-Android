package in.einfosolutions.koble.Push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import in.einfosolutions.koble.Models.Unused;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by joker on 1/24/17.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "FCM Token: " + token);

        updateTokenToPhpServer();
    }

    public static void updateTokenToPhpServer() {

        if( App.currUser != null) {
            App.currUser.token = FirebaseInstanceId.getInstance().getToken();
            App.updateUserIntoFireBaseDB(App.currUser);
            App.prefs.putObject(App.KEY_PROFILE, App.currUser);

            new APIManager().service.updateDeviceToken("update_token", App.currUser.id, App.currUser.token).enqueue(new Callback<Unused>() {
                @Override
                public void onResponse(Call<Unused> call, Response<Unused> response) {

                }

                @Override
                public void onFailure(Call<Unused> call, Throwable t) {

                }
            });
        }
    }

}
