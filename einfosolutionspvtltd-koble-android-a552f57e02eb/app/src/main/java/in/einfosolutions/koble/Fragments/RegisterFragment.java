package in.einfosolutions.koble.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import in.einfosolutions.koble.Activities.BaseActivity;
import in.einfosolutions.koble.Activities.MainActivity;
import in.einfosolutions.koble.BuildConfig;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.Push.MyFirebaseInstanceIdService;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private static final String[] universities = new String[]{"Emory University", "Babson College"};

    TextInputLayout registerName;
    TextInputLayout registerEmail;
    TextInputLayout registerPassword;
    TextInputLayout registerPasswordRepeat;
    TextInputLayout registerDepartment;
    TextInputLayout registerDesignation;
    TextInputLayout registerOfficeRoom;
    MaterialBetterSpinner universitySpinner;
    Button registerButton;
    View view;

    String tag;
    String name;
    String email;
    String password;
    String university;
    String department;
    String designation;
    String officeRoom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_register, container, false);

        registerName = (TextInputLayout) view.findViewById(R.id.register_name);
        registerEmail = (TextInputLayout) view.findViewById(R.id.register_email);
        registerPassword = (TextInputLayout) view.findViewById(R.id.register_password);
        registerPasswordRepeat = (TextInputLayout) view.findViewById(R.id.register_password_repeat);
        universitySpinner = (MaterialBetterSpinner) view.findViewById(R.id.university_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, universities);
        universitySpinner.setAdapter(adapter);
        registerDepartment = (TextInputLayout) view.findViewById(R.id.register_department);
        registerDesignation = (TextInputLayout) view.findViewById(R.id.register_designation);
        registerOfficeRoom = (TextInputLayout) view.findViewById(R.id.register_office_room);
        registerButton = (Button) view.findViewById(R.id.register_button);

        tag = BuildConfig.FLAVOR.equalsIgnoreCase("professor") ? "register_professor" : "register_student";

        if (App.APP_STU) {
            registerDesignation.setVisibility(View.GONE);
            registerOfficeRoom.setVisibility(View.GONE);
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                registerButton.setEnabled(false);

                if (validate()) {
                    registerRequest();
                }

                registerButton.setEnabled(true);
            }
        });

        return view;
    }

    boolean validate() {

        name = registerName.getEditText().getText().toString();
        email = registerEmail.getEditText().getText().toString();
        password = registerPassword.getEditText().getText().toString();
        String repeatPassword = registerPasswordRepeat.getEditText().getText().toString();
        university = universitySpinner.getText().toString();
        department = registerDepartment.getEditText().getText().toString();
        designation = registerDesignation.getEditText().getText().toString();
        officeRoom = registerOfficeRoom.getEditText().getText().toString();

        if (name.isEmpty()) {
            registerName.setError("Name cannot be empty!");
            return false;
        } else if (email.isEmpty()) {
            registerEmail.setError("Email cannot be empty!");
            return false;
        } else if (password.isEmpty()) {
            registerPassword.setError("Password cannot be empty!");
            return false;
        } else if (password.length() < 6) {
            registerPassword.setError("Password must be at least 6 characters long");
            return false;
        } else if (repeatPassword.isEmpty()) {
            registerPasswordRepeat.setError("Please repeat password!");
            return false;
        } else if (!password.equalsIgnoreCase(repeatPassword)) {
            registerPasswordRepeat.setError("Passwords do not match!");
            return false;
        } else if (university.equals("")) {
            universitySpinner.setError("Select University");
            return false;
        } else {
            registerName.setErrorEnabled(false);
            registerEmail.setErrorEnabled(false);
            registerPassword.setErrorEnabled(false);
            registerPasswordRepeat.setErrorEnabled(false);
            registerDepartment.setErrorEnabled(false);
            return true;
        }
    }

    void registerRequest() {

        final MaterialDialog progressDialog = ((BaseActivity) getActivity()).progressDialog;
        progressDialog.setContent("Registering...");
        progressDialog.show();

        new APIManager().service.register(tag, name, email, password, university, department, officeRoom, designation, FirebaseInstanceId.getInstance().getToken()).enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                ProfileModel profileModel = response.body();

                if (profileModel.status.equalsIgnoreCase("0")) {
                    App.currUser = profileModel;
                    App.prefs.putObject(App.KEY_PROFILE, profileModel);
                    App.updateUserIntoFireBaseDB(profileModel);
                    MyFirebaseInstanceIdService.updateTokenToPhpServer();

                    progressDialog.dismiss();
                    registerButton.setEnabled(true);

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else if (profileModel.status.equalsIgnoreCase("2")) {
                    progressDialog.dismiss();
                    registerButton.setEnabled(true);

                    registerEmail.setError("Email already exists!");
                } else {
                    progressDialog.dismiss();
                    registerButton.setEnabled(true);

                    final Snackbar snackbar = Snackbar.make(view, "Something went wrong.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                progressDialog.dismiss();
                registerButton.setEnabled(true);

                final Snackbar snackbar = Snackbar.make(view, "No Internet", Snackbar.LENGTH_LONG);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }
}
