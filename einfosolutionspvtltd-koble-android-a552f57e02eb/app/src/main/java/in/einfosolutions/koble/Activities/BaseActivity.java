package in.einfosolutions.koble.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by joker on 1/24/17.
 */

public class BaseActivity extends AppCompatActivity {

    public static boolean isInForegroundMode = false;
    public MaterialDialog progressDialog;

    @Override
    protected void onResume() {
        super.onResume();
        isInForegroundMode = true;
        progressDialog = new MaterialDialog.Builder(this)
                .content("Loading...")
                .progress(true, 0).build();

        /*progressDialog.setIndeterminate(true);
        progressDialog.getWindow().setGravity(Gravity.CENTER);
        progressDialog.setMessage("Loading...");*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForegroundMode = false;
    }


    // for keyboard listener

}
