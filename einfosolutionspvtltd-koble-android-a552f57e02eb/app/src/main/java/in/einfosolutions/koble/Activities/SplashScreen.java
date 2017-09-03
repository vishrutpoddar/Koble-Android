package in.einfosolutions.koble.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.danlew.android.joda.JodaTimeAndroid;

import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Push.Data;
import in.einfosolutions.koble.Push.MyFirebaseInstanceIdService;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.Activities.BaseMainActivity.INPUT_RECEIVED_NOTI;
import static in.einfosolutions.koble.utilities.App.USER_DETAILS;

public class SplashScreen extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        JodaTimeAndroid.init(this);

        App.currUser = (ProfileModel) App.prefs.getObject(App.KEY_PROFILE, ProfileModel.class);

        // reload users data if changes happened
        if (App.currUser != null) {
            MyFirebaseInstanceIdService.updateTokenToPhpServer();
            DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(USER_DETAILS).child(App.currUser.id);
            fcDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final ProfileModel profileModel = dataSnapshot.getValue(ProfileModel.class);
                    App.currUser = profileModel;
                    App.prefs.putObject(App.KEY_PROFILE, profileModel);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (App.currUser != null) {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    // getting received notification data
                    Bundle extras = getIntent().getExtras();
                    if (extras != null && extras.getString("sender_id") != null) {
                        Data receividNoti = new Data(extras);
                        intent.putExtra(INPUT_RECEIVED_NOTI, App.gson.toJson(receividNoti));
                        getIntent().replaceExtras(new Bundle());
                    }

                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }
}
