package in.einfosolutions.koble.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.PatternsCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.iid.FirebaseInstanceId;

import in.einfosolutions.koble.Activities.BaseActivity;
import in.einfosolutions.koble.Activities.MainActivity;
import in.einfosolutions.koble.BuildConfig;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.Push.MyFirebaseInstanceIdService;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    TextInputLayout loginEmail;
    TextInputLayout loginPassword;
    Button loginButton;
    View view;

    String tag;
    String email;
    String password;
    private TextView tvForgotPass;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_login, container, false);

        loginEmail = (TextInputLayout) view.findViewById(R.id.login_email);
        loginPassword = (TextInputLayout) view.findViewById(R.id.login_password);
        loginButton = (Button) view.findViewById(R.id.login_button);
        tvForgotPass = (TextView) view.findViewById(R.id.tvForgotPass);

        tag = BuildConfig.FLAVOR.equalsIgnoreCase("professor") ? "login_professor" : "login_student";

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                loginButton.setEnabled(false);

                if (validate()) {

                    loginRequest();
                }
                loginButton.setEnabled(true);
            }
        });

        tvForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .title("Forgot Password")
                        .content("Enter registered email id to receive password")
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input("email", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                forgotPass(input.toString().trim());
                            }
                        }).show();
            }
        });

        return view;
    }

    private void forgotPass(String email) {
        if (email.isEmpty()) {
            return;
        }
        final MaterialDialog progressDialog = ((BaseActivity) getActivity()).progressDialog;
        progressDialog.setContent("Please wait...");
        progressDialog.show();
        new APIManager().service.forgotPassApi("forgot_pass", email).enqueue(new Callback<Response1>() {
            @Override
            public void onResponse(Call<Response1> call, Response<Response1> response) {
                progressDialog.dismiss();
                if (!response.isSuccessful() || response.body() == null) return;
                if (response.body().status.equals("0"))
                    Toast.makeText(getActivity().getApplicationContext(), R.string.pass_sent_to_mail, Toast.LENGTH_SHORT).show();
                if (response.body().status.equals("1"))
                    Toast.makeText(getActivity().getApplicationContext(), R.string.pass_sent_to_mail, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity().getApplicationContext(), R.string.please_try_again, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Response1> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.email_not_exists), Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean validate() {

        email = loginEmail.getEditText().getText().toString();
        password = loginPassword.getEditText().getText().toString();

        if (email.isEmpty()) {
            loginEmail.setError("Email cannot be empty!");
            return false;
        } else if (password.isEmpty()) {

            loginPassword.setError("Password cannot be empty!");
            return false;
        } else if (!(PatternsCompat.EMAIL_ADDRESS.matcher(email).matches())) {
            loginEmail.setError("Invalid email");
            return false;
        } else if (password.length() < 5) {
            loginPassword.setError("Password must be at least 5 characters long");
            return false;
        } else {
            loginEmail.setErrorEnabled(false);
            loginPassword.setErrorEnabled(false);
            return true;
        }
    }

    void loginRequest() {

        final MaterialDialog progressDialog = ((BaseActivity) getActivity()).progressDialog;
        progressDialog.setContent("Authenticating...");
        progressDialog.show();

        new APIManager().service.login(tag, email, password).enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                ProfileModel profileModel = response.body();

                if (profileModel.status.equalsIgnoreCase("0")) {
                    App.currUser = profileModel;
                    App.prefs.putObject(App.KEY_PROFILE, profileModel);
                    profileModel.token = FirebaseInstanceId.getInstance().getToken();
                    App.updateUserIntoFireBaseDB(profileModel);
                    MyFirebaseInstanceIdService.updateTokenToPhpServer();

                    progressDialog.dismiss();
                    loginButton.setEnabled(true);

                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else if (profileModel.status.equalsIgnoreCase("2")) {
                    progressDialog.dismiss();
                    loginButton.setEnabled(true);

                    final Snackbar snackbar = Snackbar.make(view, "Incorrect Email/Password", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                } else {
                    progressDialog.dismiss();
                    loginButton.setEnabled(true);

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
                loginButton.setEnabled(true);

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
