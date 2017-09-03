package in.einfosolutions.koble.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import eu.inloop.localmessagemanager.LocalMessage;
import eu.inloop.localmessagemanager.LocalMessageCallback;
import eu.inloop.localmessagemanager.LocalMessageManager;
import in.einfosolutions.koble.Activities.ProfileEditActivity;
import in.einfosolutions.koble.Models.ChatSettings;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.Activities.ChatActivity.CHAT_SETTINGS;

/**
 * Created by joker on 1/4/17.
 */

public class ProfileFragment extends Fragment implements LocalMessageCallback {

    SwitchCompat switchComapt;
    private RoundedImageView profileImage;
    private EditText etName;
    private EditText etUniv;
    private EditText etDept;
    private EditText etEmail;
    private EditText etDesig;
    private EditText etOffice;
    private DatabaseReference fireElseChatSettings;
    private View vOnlineOffline;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = (RoundedImageView) view.findViewById(R.id.profile_image);
        etName = (EditText) view.findViewById(R.id.etName);
        etUniv = (EditText) view.findViewById(R.id.etUniv);
        etDept = (EditText) view.findViewById(R.id.etDept);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etDesig = (EditText) view.findViewById(R.id.etDesig);
        etOffice = (EditText) view.findViewById(R.id.etOffice);
        switchComapt = (SwitchCompat) view.findViewById(R.id.switchCompat);
        vOnlineOffline = view.findViewById(R.id.vOnlineOffline);


        if (App.APP_STU) {
            etOffice.setVisibility(View.GONE);
            etDesig.setVisibility(View.GONE);
            vOnlineOffline.setVisibility(View.GONE);
        }

        if (App.APP_PRO) {
            fireElseChatSettings = FirebaseDatabase.getInstance().getReference(CHAT_SETTINGS).child(App.currUser.id);
            // getting online / offline status
            fireElseChatSettings.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatSettings chatSettings = dataSnapshot.getValue(ChatSettings.class);
                    if (chatSettings == null) chatSettings = new ChatSettings();
                    switchComapt.setChecked(chatSettings.chattable);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            switchComapt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    ChatSettings chatSettings = new ChatSettings();
                    chatSettings.chattable = b;
                    fireElseChatSettings.updateChildren(chatSettings.toMap());

                }
            });
        }

        return view;
    }

    void updateViews() {

        ProfileModel profileModel = App.currUser;

        etName.setText(profileModel.first_name);
        etUniv.setText(profileModel.university);
        etDept.setText(profileModel.department);
        etEmail.setText(profileModel.email);
        etDesig.setText(profileModel.desig);
        etOffice.setText(profileModel.office_room);

        Glide.with(this)
                .load(APIManager.PIC_BASE_URL + profileModel.pic)
                .crossFade()
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(profileImage);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit:
                Intent i = new Intent(getActivity(), ProfileEditActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");
        LocalMessageManager.getInstance().addListener(this);
        updateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalMessageManager.getInstance().removeListener(this);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if(localMessage.getId() == App.OBSERVE_USER_LIST_REFRESH) {
            updateViews();
        }
    }
}
