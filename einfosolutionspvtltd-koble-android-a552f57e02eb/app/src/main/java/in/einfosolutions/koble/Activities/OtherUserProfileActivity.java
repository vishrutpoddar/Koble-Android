package in.einfosolutions.koble.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.ProfileType;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static android.view.View.GONE;
import static in.einfosolutions.koble.utilities.App.APP_STU;

/**
 * Created by joker on 1/11/17.
 */

public class OtherUserProfileActivity extends BaseActivity {

    public static final String KEY_INPUT_PROFILE = "KEY_INPUT_PROFILE";
    public static final String KEY_SHOW_CHAT_MENU = "KEY_SHOW_CHAT_MENU";
    public static final String KEY_SHOW_APPO_MENU = "KEY_SHOW_APPO_MENU";

    public ProfileModel profileModel;
    private RoundedImageView profileImage;
    private TextView tvName;
    private TextView tvUniv;
    private TextView tvDept;
    private TextView tvEmail;
    private TextView tvDesig;
    private TextView tvOffice;
    private Toolbar toolbar;
    private AppCompatButton bOfficeHours;

    private boolean showAppointMenu = false;
    private boolean showChatMenu = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        profileModel = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_PROFILE), ProfileModel.class);
        showAppointMenu = getIntent().getBooleanExtra(KEY_SHOW_APPO_MENU, false);
        showChatMenu = getIntent().getBooleanExtra(KEY_SHOW_CHAT_MENU, false);

        if (App.currUser.id.equals(profileModel.id))
            showChatMenu = showAppointMenu = false;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Details");

        profileImage = (RoundedImageView) findViewById(R.id.profile_image);
        tvName = (TextView) findViewById(R.id.tvName);
        tvUniv = (TextView) findViewById(R.id.tvUniv);
        tvDept = (TextView) findViewById(R.id.tvDept);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        tvDesig = (TextView) findViewById(R.id.tvDesig);
        tvOffice = (TextView) findViewById(R.id.tvOffice);
        bOfficeHours = (AppCompatButton) findViewById(R.id.bOfficeHours);

        tvName.setText(profileModel.first_name);
        tvUniv.setText(profileModel.university);
        tvDept.setText(profileModel.department);
        tvEmail.setText(profileModel.email);
        tvDesig.setText(profileModel.desig);
        tvOffice.setText(profileModel.office_room);

        Glide.with(this)
                .load(APIManager.PIC_BASE_URL + profileModel.pic)
                .crossFade()
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profileImage);

        if (profileModel.type == ProfileType.stu) {
            tvOffice.setVisibility(GONE);
            tvDesig.setVisibility(GONE);
        }

        if(profileModel.type == ProfileType.prof) {
            bOfficeHours.setVisibility(View.VISIBLE);
            bOfficeHours.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i1 = new Intent(OtherUserProfileActivity.this, OfficeHoursActivity.class);
                    i1.putExtra(OfficeHoursActivity.KEY_INPUT_USER, App.gson.toJson(profileModel));
                    startActivity(i1);
                }
            });
        } else {
            bOfficeHours.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_other_user, menu);

        menu.findItem(R.id.menu_appoint).setVisible(showAppointMenu);
        menu.findItem(R.id.menu_chats).setVisible(showChatMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_appoint:
                if (App.userNotVerified(this)) return true;
                Intent i1 = new Intent(this, MakeAppoActivity.class);
                i1.putExtra(MakeAppoActivity.KEY_INPUT_PROF, App.gson.toJson(profileModel));
                startActivity(i1);
                return true;
            case R.id.menu_chats:
                if (App.userNotVerified(this)) return true;
                Intent i2 = new Intent(this, ChatActivity.class);
                i2.putExtra(ChatActivity.KEY_INPUT_OTHER_USER, App.gson.toJson(profileModel));
                startActivity(i2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
