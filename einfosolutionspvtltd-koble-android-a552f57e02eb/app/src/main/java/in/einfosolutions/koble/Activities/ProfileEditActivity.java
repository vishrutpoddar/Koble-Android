package in.einfosolutions.koble.Activities;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.StackingBehavior;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.File;
import java.util.HashMap;

import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by joker on 5/15/17.
 */

public class ProfileEditActivity extends BaseActivity {

    public final static int PIC_CHANGE = 2, PIC_REMOVE = 1, PIC_NONE = 0;
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    public ProfileModel profileModel = App.currUser;
    int picOperation = PIC_NONE;
    private RoundedImageView ivLogo;
    private TextInputLayout tilName;
    private EditText etName;
    private TextInputLayout tilUniv;
    private EditText etUniv;
    private TextInputLayout tilDept;
    private TextInputLayout tilDesig;
    private EditText etDept;
    private EditText etEmail;
    private EditText etDesig;
    private TextInputLayout tilOffice;
    private EditText etOffice;
    private Toolbar toolbar;
    private ImageView bToolbarRight;
    private ImageView bToolbarLeft;
    private TextView tvToolbarTitle;
    private File imageFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_profile);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        bToolbarLeft = (ImageView) findViewById(R.id.bToolbarLeft);
        tvToolbarTitle = (TextView) findViewById(R.id.tvToolbarTitle);
        bToolbarRight = (ImageView) findViewById(R.id.bToolbarRight);
        ivLogo = (RoundedImageView) findViewById(R.id.profile_image);
        tilName = (TextInputLayout) findViewById(R.id.tilName);
        etName = (EditText) findViewById(R.id.etName);
        tilUniv = (TextInputLayout) findViewById(R.id.tilUniv);
        etUniv = (EditText) findViewById(R.id.etUniv);
        tilDesig = (TextInputLayout) findViewById(R.id.tilDesig);
        tilDept = (TextInputLayout) findViewById(R.id.tilDept);
        etDept = (EditText) findViewById(R.id.etDept);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etDesig = (EditText) findViewById(R.id.etDesig);
        tilOffice = (TextInputLayout) findViewById(R.id.tilOffice);
        etOffice = (EditText) findViewById(R.id.etOffice);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        enableEditing(true);

        tvToolbarTitle.setText("Edit");
        bToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileApiCall();
            }
        });

        if (App.APP_STU) {
            etOffice.setVisibility(View.GONE);
            etDesig.setVisibility(View.GONE);
        }

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(ProfileEditActivity.this)
                        .title("Change Photo")
                        .items("Remove", "Change")
                        .stackingBehavior(StackingBehavior.ALWAYS)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                if (position == 0) {
                                    ivLogo.setImageResource(R.drawable.ic_user_placeholder);
                                    picOperation = PIC_REMOVE;
                                } else {
                                    startPickImage();
                                }
                            }
                        })
                        .negativeText("Cancel")
                        .show();
            }
        });

        updateViews();
    }

    void startPickImage() {

        PickSetup picSetup = new PickSetup()
                .setMaxSize(500)
                .setTitle("Capture profile picture")
                .setButtonOrientation(LinearLayoutCompat.HORIZONTAL)
                .setSystemDialog(true);

        PickImageDialog.build(picSetup).setOnPickResult(new IPickResult() {
            @Override
            public void onPickResult(PickResult r) {
                if (r.getError() == null) {
                    File fromFile = new File(r.getPath());
                    //Log.e(TAG, "initial size : w:h " + r.getBitmap().getWidth() + ":" + r.getBitmap().getHeight() + ", size:" + fromFile.length() / 1024 + "kb");

                    imageFile = fromFile;
                    if (fromFile.length() / 1024 < 50) {
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), r.getBitmap());
                        ivLogo.setImageDrawable(drawable);
                        picOperation = PIC_CHANGE;
                    } else {
                        imageFile = App.scaleImageFileAndReduceSize(imageFile);
                        if (imageFile == null) {
                            Toast.makeText(App.ctx, App.ctx.getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                            updateViews();
                        } else {
                            BitmapDrawable drawable = new BitmapDrawable(getResources(), r.getBitmap());
                            ivLogo.setImageDrawable(drawable);
                            picOperation = PIC_CHANGE;
                        }
                    }
                    //Log.e(TAG, "final size : w:h " + r.getBitmap().getWidth() + ":" + r.getBitmap().getHeight() + ", size:" + imageFile.length() / 1024 + "kb");
                } else {
                    //Log.e(TAG, r.getError().getMessage());
                }
            }
        }).show(this);
    }

    void updateUserDetailsApiCall() {
        new APIManager().service.updateUserDetails("update_user",
                App.currUser.id, App.currUser.first_name,
                App.currUser.university, App.currUser.department,
                "pic_raw", App.currUser.office_room, App.currUser.desig).enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    App.currUser = response.body();
                    App.prefs.putObject(App.KEY_PROFILE, App.currUser);
                }
            }
            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {

            }
        });
    }

    void updateViews() {

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
                .into(ivLogo);

    }

    void updateProfileApiCall() {

        String name = etName.getText().toString().trim();
        //String email = etEmail.getText().toString();
        String university = etUniv.getText().toString();
        String department = etDept.getText().toString();
        String designation = etDesig.getText().toString();
        String officeRoom = etOffice.getText().toString();

        if (name.isEmpty()) {
            tilName.setError("Name cannot be empty!");
            return;
        } else if (department.isEmpty()) {
            tilDept.setError("Department cannot be empty!");
            return;
        } else if (App.APP_PRO && designation.isEmpty()) {
            tilDesig.setError("Designation cannot be empty!");
            return;
        } else if (App.APP_PRO && officeRoom.isEmpty()) {
            tilOffice.setError("Office cannot be empty!");
            return;
        }

        tilName.setError("");
        tilDept.setError("");
        tilDesig.setError("");
        tilOffice.setError("");

        progressDialog.setContent("Updating details...");
        progressDialog.show();

        new APIManager().service.updateUserDetails("update_user",
                App.currUser.id, name, university, department, "pic_raw", officeRoom, designation).enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().status.equals("0")) {
                    App.currUser = response.body();
                    App.prefs.putObject(App.KEY_PROFILE, App.currUser);
                    changeOrRemoveProfilePicApiCall();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT);
            }
        });
    }

    private void changeOrRemoveProfilePicApiCall() {
        if (picOperation == PIC_CHANGE) {
            HashMap<String, RequestBody> map = new HashMap<>();
            map.put("tag", RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), "update_profile_image"));
            map.put("userid", RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), App.currUser.id));

            MultipartBody.Part imageBody = App.prepareFilePart("profile_photo", imageFile);
            progressDialog.setContent("Uploading new photo...");
            progressDialog.show();
            new APIManager().service.changeProfilePic(map, imageBody).enqueue(new Callback<Response1>() {
                @Override
                public void onResponse(Call<Response1> call, Response<Response1> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        updateUserDetailsApiCall();
                        Toast.makeText(getApplicationContext(), getString(R.string.profile_saved), Toast.LENGTH_SHORT);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT);
                    }
                }

                @Override
                public void onFailure(Call<Response1> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(App.ctx, App.ctx.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (picOperation == PIC_REMOVE) {
            progressDialog.setContent("Removing photo...");
            progressDialog.show();
            new APIManager().service.removeProfilePic("delete_img", App.currUser.id).enqueue(new Callback<Response1>() {
                @Override
                public void onResponse(Call<Response1> call, Response<Response1> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        updateUserDetailsApiCall();
                        Toast.makeText(getApplicationContext(), getString(R.string.profile_saved), Toast.LENGTH_SHORT);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT);
                    }
                }

                @Override
                public void onFailure(Call<Response1> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(App.ctx, App.ctx.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            });
        } else { // PIC no change
            updateUserDetailsApiCall();
            Toast.makeText(getApplicationContext(), getString(R.string.profile_saved), Toast.LENGTH_SHORT);
            finish();
        }
    }

    void enableEditing(final boolean enable) {
        etName.setEnabled(enable);
        //etEmail.setEnabled(enable);
        etDesig.setEnabled(enable);
        //etUniv.setEnabled(enable);
        etDept.setEnabled(enable);
        etOffice.setEnabled(enable);
    }

}
