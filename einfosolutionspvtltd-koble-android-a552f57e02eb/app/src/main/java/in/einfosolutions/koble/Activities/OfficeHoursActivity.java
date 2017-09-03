package in.einfosolutions.koble.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import in.einfosolutions.koble.Fragments.OfficeHoursFragment;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

/**
 * Created by joker on 5/13/17.
 */

public class OfficeHoursActivity extends BaseActivity {

    public static final String KEY_INPUT_USER = "KEY_INPUT_USER";
    Toolbar toolbar;
    private OfficeHoursFragment officeHourFrag = new OfficeHoursFragment();
    private ProfileModel profileModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_office_hours);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Office Hours");

        profileModel = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_USER), ProfileModel.class);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        officeHourFrag.inputProfile = profileModel;
        ft.replace(R.id.fragment_container, officeHourFrag, null);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
