package in.einfosolutions.koble.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import in.einfosolutions.koble.Fragments.AllChatsFragment;
import in.einfosolutions.koble.Fragments.ClassesFragment;
import in.einfosolutions.koble.Fragments.NotificationsFragment;
import in.einfosolutions.koble.Fragments.ProfessorsFragment;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.JoinClassRes;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FloatingActionButton classes = new FloatingActionButton(this);
        classes.setIcon(R.drawable.classes);
        classes.setColorNormalResId(R.color.colorPrimaryDark);
        classes.setColorPressedResId(R.color.colorPrimaryDarkPressed);
        classes.setTitle("Join Class");
        classes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.userNotVerified(MainActivity.this)) {
                } else
                    showAddClassDialog();

                floatingActionsMenu.collapse();
            }
        });
        floatingActionsMenu.addButton(classes);

        FloatingActionButton event = new FloatingActionButton(this);
        event.setIcon(R.drawable.schedule);
        event.setColorNormalResId(R.color.colorPrimaryDark);
        event.setColorPressedResId(R.color.colorPrimaryDarkPressed);
        event.setTitle("Create Event");
        event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.userNotVerified(MainActivity.this)) {
                } else
                    startAddEventAcitvity(EventType.EVENT);

                floatingActionsMenu.collapse();
            }
        });
        floatingActionsMenu.addButton(event);

    }

    @Override
    public void selectTopNavigationItem(MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.schedule:
                fragment = scheduleFragment;
                break;
            case R.id.professors:
                fragment = new ProfessorsFragment();
                break;
            case R.id.classes:
                fragment = new ClassesFragment();
                break;
            case R.id.chats:
                fragment = new AllChatsFragment();
                break;
            case R.id.notifications:
                fragment = new NotificationsFragment();
                break;
            default:
                fragment = scheduleFragment;
        }

        if (fragment == scheduleFragment)
            floatingActionsMenu.setVisibility(View.VISIBLE);
        else
            floatingActionsMenu.setVisibility(View.GONE);

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }

        drawerLayout.closeDrawers();
    }

    void showAddClassDialog() {
        View mView = getLayoutInflater().inflate(R.layout.code_input, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setView(mView);

        final TextInputEditText inputDialogEditText = (TextInputEditText) mView.findViewById(R.id.userInputDialog);
        final TextInputLayout til = (TextInputLayout) mView.findViewById(R.id.til);

        final LinearLayout llInputs = (LinearLayout) mView.findViewById(R.id.llInputs);
        final ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.progressBar);

        final AlertDialog alertDialog = alertDialogBuilderUserInput
                //.setCancelable(false)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {

                            }
                        }).create();

        alertDialog.show();

        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = inputDialogEditText.getText().toString().trim().toLowerCase();

                if (code.length() == 0) {
                    inputDialogEditText.setError("Blank");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                inputDialogEditText.setEnabled(false);

                new APIManager().service.joinClass(
                        "redeem_code",
                        App.currUser.id,
                        code
                ).enqueue(new Callback<JoinClassRes>() {
                    @Override
                    public void onResponse(Call<JoinClassRes> call, Response<JoinClassRes> response) {
                        progressBar.setVisibility(View.GONE);
                        inputDialogEditText.setEnabled(true);
                        JoinClassRes model = response.body();
                        if (model.status.equals("0")) {
                            alertDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Class joined.", Toast.LENGTH_SHORT).show();
                        } else {
                            inputDialogEditText.setError("Incorrect code.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JoinClassRes> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        inputDialogEditText.setEnabled(true);
                        App.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_internet)).show();
                    }
                });
            }
        });

    }

}
