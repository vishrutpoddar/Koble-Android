package in.einfosolutions.koble.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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
import in.einfosolutions.koble.Activities.BaseActivity;
import in.einfosolutions.koble.Activities.BaseMainActivity;
import in.einfosolutions.koble.Activities.ProfileEditActivity;
import in.einfosolutions.koble.Models.ChatSettings;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.einfosolutions.koble.Activities.ChatActivity.CHAT_SETTINGS;

/**
 * Created by joker on 1/4/17.
 */

public class FeedbackFragment extends Fragment {

    private TextInputLayout loginEmail;
    private AppCompatButton loginButton;
    private TextInputEditText etFeedback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        etFeedback = (TextInputEditText) view.findViewById(R.id.etFeedback);
        loginEmail = (TextInputLayout) view.findViewById(R.id.login_email);
        loginButton = (AppCompatButton) view.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitFeedback();
            }
        });

        return view;
    }

    void submitFeedback() {
        String text = etFeedback.getText().toString().trim();
        if(text.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "Enter your feedback!", Toast.LENGTH_SHORT).show();
            return;
        }

        final MaterialDialog progressDialog = ((BaseActivity) getActivity()).progressDialog;
        progressDialog.setContent("Submitting your feedback...");
        progressDialog.show();

        new APIManager().service.submitFeedback("feedback", App.currUser.id).enqueue(new Callback<Response1>() {
            @Override
            public void onResponse(Call<Response1> call, Response<Response1> response) {
                progressDialog.dismiss();
                if(response.isSuccessful()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Feedback Submitted", Toast.LENGTH_SHORT).show();
                    etFeedback.setText("");
                    ((BaseMainActivity) getActivity()).selectTopNavigationItem(((BaseMainActivity) getActivity()).navigationViewTop.getMenu().findItem(R.id.schedule));
                }
            }

            @Override
            public void onFailure(Call<Response1> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
