package in.einfosolutions.koble.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;

import in.einfosolutions.koble.Models.AddEventInput;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.ResModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by joker on 12/30/16.
 */

public class AddEventActivity extends BaseActivity {

    public static final String KEY_INPUTS = "add_event_inputs";

    static int animationDuration = 300;

    SimpleDateFormat dtfShowDate = new SimpleDateFormat("dd MMM yyyy");
    SimpleDateFormat dtfShowTime = new SimpleDateFormat("hh:mm a");

    TextInputEditText etTitle;
    TextInputEditText etDesc;
    View bStart;
    TextView tvLableStart;
    TextView tvStartDate, tvStartTime;
    TextView tvLableEnd, tvLabelRecurring;
    TextView tvEndDate, tvEndTime;
    SwitchCompat sRecurring;
    View bRecurringDate;
    View bRecurringSwitch, bRecurringDays;
    TextView tvRecurring;
    TextInputEditText etOffice;
    Toolbar toolbar;
    TextView toolbarTitle;
    TextView tvLabelRecurringDays;
    ImageView bToolbarLeft;
    TextView bToolbarRight;
    TextView tvDay1, tvDay2, tvDay3, tvDay4, tvDay5, tvDay6, tvDay7;

    String taggedUsers = "[]";

    AddEventInput inputs;

    Date startMinDate, endMinDate, reqMinDate;

    View.OnClickListener daysViewListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            TextView tv = (TextView) view;
            if (tv.getHint() == null) {
                tv.setHint("");
                tv.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setHint(null);
                tv.setTextColor(Color.DKGRAY);
                tv.setTypeface(null, Typeface.NORMAL);
            }
        }
    };

    private TextInputLayout tilOffice;
    private boolean editing = false;

    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 0, 1);
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(a);
        a.setDuration(animationDuration);
        set.setDuration(animationDuration);

        v.startAnimation(set);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 1, 0);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(a);
        set.setDuration(animationDuration);

        v.startAnimation(set);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bToolbarLeft = (ImageView) findViewById(R.id.bToolbarLeft);
        bToolbarRight = (TextView) findViewById(R.id.bToolbarRight);
        toolbarTitle = (TextView) findViewById(R.id.tvToolbarTitle);
        etTitle = (TextInputEditText) findViewById(R.id.etTitle);
        etDesc = (TextInputEditText) findViewById(R.id.etDesc);

        bStart = findViewById(R.id.bStart);
        tvLableStart = (TextView) findViewById(R.id.tvLableStart);
        tvStartDate = (TextView) findViewById(R.id.tvStartDate);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);

        tvLableEnd = (TextView) findViewById(R.id.tvLabelEnd);
        tvEndDate = (TextView) findViewById(R.id.tvEndDate);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);

        bRecurringDate = findViewById(R.id.bRecurring);
        bRecurringSwitch = findViewById(R.id.bRecurringSwitch);
        bRecurringDays = findViewById(R.id.bRecurringDays);
        sRecurring = (SwitchCompat) findViewById(R.id.sRecurring);
        tvRecurring = (TextView) findViewById(R.id.tvRecurring);
        tvLabelRecurringDays = (TextView) findViewById(R.id.tvLabelRecurringDays);
        tvLabelRecurring = (TextView) findViewById(R.id.tvLabelRecurring);
        etOffice = (TextInputEditText) findViewById(R.id.etOffice);
        tilOffice = (TextInputLayout) findViewById(R.id.tilOffice);

        if (App.APP_STU) {
            tilOffice.setHint("Location");
            tilOffice.getEditText().setHint("");
        }

        tvDay1 = (TextView) findViewById(R.id.tvDay1);
        tvDay2 = (TextView) findViewById(R.id.tvDay2);
        tvDay3 = (TextView) findViewById(R.id.tvDay3);
        tvDay4 = (TextView) findViewById(R.id.tvDay4);
        tvDay5 = (TextView) findViewById(R.id.tvDay5);
        tvDay6 = (TextView) findViewById(R.id.tvDay6);
        tvDay7 = (TextView) findViewById(R.id.tvDay7);

        tvDay1.setOnClickListener(daysViewListener);
        tvDay2.setOnClickListener(daysViewListener);
        tvDay3.setOnClickListener(daysViewListener);
        tvDay4.setOnClickListener(daysViewListener);
        tvDay5.setOnClickListener(daysViewListener);
        tvDay6.setOnClickListener(daysViewListener);
        tvDay7.setOnClickListener(daysViewListener);

        sRecurring.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    expand(bRecurringDays);
                } else {
                    collapse(bRecurringDays);
                }
            }
        });

        bToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        bToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEventAPICall();
            }
        });

        inputs = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUTS), AddEventInput.class);
        editing = inputs.eventModel != null ? true : false;

        endMinDate = inputs.startDateTime;
        reqMinDate = inputs.endDateTime;

        tvStartDate.setText(dtfShowDate.format(inputs.startDateTime));
        tvEndDate.setText(dtfShowDate.format(inputs.endDateTime));
        tvStartTime.setText(dtfShowTime.format(inputs.startDateTime));
        tvEndTime.setText(dtfShowTime.format(inputs.endDateTime));
        tvRecurring.setText(dtfShowDate.format(inputs.recurringDate));

        if (editing) {
            etTitle.setText(inputs.eventModel.eventDetails.event_name);
            etDesc.setText(inputs.eventModel.eventDetails.description);
            etOffice.setText(inputs.eventModel.eventDetails.location);
            sRecurring.setChecked(inputs.eventModel.eventDetails.recurring);

            if (inputs.eventModel.eventDetails.recurring) {
                for (int i = 1; i <= 7; i++) {
                    if (inputs.eventModel.eventDetails.recurringDays.contains(String.valueOf(i)))
                        turnOnRecurringDay(i);
                }
            }
        }

        int week = new DateTime(inputs.startDateTime).getDayOfWeek();
        switch (week) {
            case 1:
                tvDay2.setHint("");
                tvDay2.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay2.setTypeface(null, Typeface.BOLD);
                break;
            case 2:
                tvDay3.setHint("");
                tvDay3.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay3.setTypeface(null, Typeface.BOLD);
                break;
            case 3:
                tvDay4.setHint("");
                tvDay4.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay4.setTypeface(null, Typeface.BOLD);
                break;
            case 4:
                tvDay5.setHint("");
                tvDay5.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay5.setTypeface(null, Typeface.BOLD);
                break;
            case 5:
                tvDay6.setHint("");
                tvDay6.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay6.setTypeface(null, Typeface.BOLD);
                break;
            case 6:
                tvDay7.setHint("");
                tvDay7.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay7.setTypeface(null, Typeface.BOLD);
                break;
            case 7:
                tvDay1.setHint("");
                tvDay1.setTextColor(ContextCompat.getColor(AddEventActivity.this, R.color.colorPrimaryDark));
                tvDay1.setTypeface(null, Typeface.BOLD);
                break;
            default:
                break;
        }

        if (inputs.eventType == EventType.CLASS) {
            tvLabelRecurringDays.setText("Class Days");
        } else {
            tvLabelRecurringDays.setText("Repeat");
        }

        switch (inputs.eventType) {
            case EVENT:
                toolbarTitle.setText("New Event");
                break;
            case OFFICE:
                toolbarTitle.setText("Add Office");
                break;
            case APPO:
                toolbarTitle.setText("Appointment");
                break;
            case CLASS:
                toolbarTitle.setText("New Class");
                break;
        }

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showDatePicker(inputs.startDateTime, null, null, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        inputs.startDateTime = new DateTime(inputs.startDateTime)
                                .withYear(datePicker.getYear())
                                .withMonthOfYear(datePicker.getMonth() + 1)
                                .withDayOfMonth(datePicker.getDayOfMonth()).toDate();
                        endMinDate = inputs.startDateTime;
                        tvStartDate.setText(dtfShowDate.format(inputs.startDateTime));

                        if (inputs.eventType == EventType.EVENT && inputs.startDateTime.compareTo(inputs.endDateTime) > 0) {
                            inputs.endDateTime = new DateTime(inputs.endDateTime)
                                    .withYear(datePicker.getYear())
                                    .withMonthOfYear(datePicker.getMonth() + 1)
                                    .withDayOfMonth(datePicker.getDayOfMonth()).toDate();
                            tvEndDate.setText(dtfShowDate.format(inputs.endDateTime));
                        } else {
                            inputs.endDateTime = new DateTime(inputs.endDateTime)
                                    .withYear(datePicker.getYear())
                                    .withMonthOfYear(datePicker.getMonth() + 1)
                                    .withDayOfMonth(datePicker.getDayOfMonth()).toDate();
                            tvEndDate.setText(dtfShowDate.format(inputs.endDateTime));
                        }

                        DateTime endDate = new DateTime(inputs.endDateTime).plusWeeks(1);
                        inputs.recurringDate = new DateTime(inputs.recurringDate)
                                .withYear(endDate.getYear())
                                .withMonthOfYear(endDate.getMonthOfYear())
                                .withDayOfMonth(endDate.getDayOfMonth()).toDate();
                        tvRecurring.setText(dtfShowDate.format(inputs.recurringDate));
                    }
                });
            }
        });
        tvStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(inputs.startDateTime, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        inputs.startDateTime = new DateTime(inputs.startDateTime).withHourOfDay(i).withMinuteOfHour(i1).toDate();
                        tvStartTime.setText(dtfShowTime.format(inputs.startDateTime));
                        validateStartAndEndDate();
                    }
                });
            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showDatePicker(inputs.endDateTime, endMinDate, null, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        inputs.endDateTime = new DateTime(inputs.endDateTime)
                                .withYear(datePicker.getYear())
                                .withMonthOfYear(datePicker.getMonth() + 1)
                                .withDayOfMonth(datePicker.getDayOfMonth()).toDate();

                        tvEndDate.setText(dtfShowDate.format(inputs.endDateTime));

                        reqMinDate = inputs.endDateTime;
                        if (inputs.endDateTime.compareTo(inputs.recurringDate) > 0) {
                            inputs.recurringDate = new DateTime(inputs.recurringDate)
                                    .withYear(datePicker.getYear())
                                    .withMonthOfYear(datePicker.getMonth() + 1)
                                    .withDayOfMonth(datePicker.getDayOfMonth()).toDate();
                            tvRecurring.setText(dtfShowDate.format(inputs.recurringDate));
                        }
                    }
                });
            }
        });
        tvEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(inputs.endDateTime, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        inputs.endDateTime = new DateTime(inputs.endDateTime).withHourOfDay(i).withMinuteOfHour(i1).toDate();
                        tvEndTime.setText(dtfShowTime.format(inputs.endDateTime));
                        validateStartAndEndDate();
                    }
                });
            }
        });
        bRecurringDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showDatePicker(inputs.recurringDate, reqMinDate, null, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        inputs.recurringDate = new DateTime(inputs.recurringDate)
                                .withYear(datePicker.getYear())
                                .withMonthOfYear(datePicker.getMonth() + 1)
                                .withDayOfMonth(datePicker.getDayOfMonth()).toDate();

                        tvRecurring.setText(dtfShowDate.format(inputs.recurringDate));
                    }
                });
            }
        });

        if (inputs.eventType == EventType.CLASS || inputs.eventType == EventType.OFFICE) {
            tvEndDate.setEnabled(false);
            tvEndDate.setTextColor(Color.GRAY);
        }

        if (inputs.eventType == EventType.APPO) {
            bRecurringSwitch.setVisibility(View.GONE);

            etOffice.setText(inputs.professorModel.office_room);
            etOffice.setEnabled(false);

            tvStartTime.setEnabled(false);
            tvStartDate.setEnabled(false);

            tvEndTime.setEnabled(false);
            tvEndDate.setEnabled(false);

            int colorGray = ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray);

            tvStartDate.setTextColor(colorGray);
            tvStartTime.setTextColor(colorGray);
            tvEndDate.setTextColor(colorGray);
            tvEndTime.setTextColor(colorGray);

            JSONArray array = new JSONArray();
            array.put(Integer.parseInt(inputs.professorModel.id));
            taggedUsers = array.toString();
        }

    }

    void showTimePicker(Date init, TimePickerDialog.OnTimeSetListener listener) {
        DateTime in = new DateTime(init);
        TimePickerDialog timePicker = new TimePickerDialog(this, listener, in.getHourOfDay(), in.getMinuteOfHour(), false);
        timePicker.show();
    }

    // creating datetime pickers
    void showDatePicker(Date init, Date min, Date max, DatePickerDialog.OnDateSetListener listener) {

        DateTime in = new DateTime(init);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                listener,
                in.getYear(),
                in.getMonthOfYear() - 1,
                in.getDayOfMonth());

        if (min != null)
            datePickerDialog.getDatePicker().setMinDate(min.getTime());
        if (max != null)
            datePickerDialog.getDatePicker().setMaxDate(max.getTime());

        datePickerDialog.show();

        /*SlideDateTimePicker.Builder pickerStart = new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {
                        tvStart.setText(sdfShow.format(date));
                        inputs.startDateTime = date;

                        Date dateAdd1hour = new DateTime(inputs.startDateTime).plusHours(1).toDate();
                        tvEnd.setText(sdfShow.format(dateAdd1hour));
                        inputs.endDateTime = dateAdd1hour;

                        if (inputs.eventType == EventType.CLASS) {

                        } else {

                        }
                    }
                })
                .setInitialDate(inputs.startDateTime)
                .setType(SlideDateTimePicker.TYPE_BOTH);

        if (inputs.eventType == EventType.CLASS) {
            pickerStart.setInitialDate(new Date());
            DateTime dateTime = new DateTime(new Date());
            pickerStart.setMaxDate(dateTime.plusMinutes((24 * 60) - (dateTime.getMinuteOfDay())).toDate());
        }
        pickerStart.build().show();*/
    }

    String createRecurringDays() {
        JSONArray array = new JSONArray();
        if (tvDay1.getHint() != null) array.put(1);
        if (tvDay2.getHint() != null) array.put(2);
        if (tvDay3.getHint() != null) array.put(3);
        if (tvDay4.getHint() != null) array.put(4);
        if (tvDay5.getHint() != null) array.put(5);
        if (tvDay6.getHint() != null) array.put(6);
        if (tvDay7.getHint() != null) array.put(7);
        return array.toString();
    }

    void turnOnRecurringDay(int weekNumber) {
        switch (weekNumber) {
            case 1:
                daysViewListener.onClick(tvDay1);
                break;
            case 2:
                daysViewListener.onClick(tvDay2);
                break;
            case 3:
                daysViewListener.onClick(tvDay3);
                break;
            case 4:
                daysViewListener.onClick(tvDay4);
                break;
            case 5:
                daysViewListener.onClick(tvDay5);
                break;
            case 6:
                daysViewListener.onClick(tvDay6);
                break;
            case 7:
                daysViewListener.onClick(tvDay7);
                break;
        }
    }

    void addEventAPICall() {

        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        String eventName = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String officeLocation = etOffice.getText().toString().trim();
        String recurring = (sRecurring.isChecked()) ? "true" : "false";
        String recurringDays = createRecurringDays();

        String tag_users = taggedUsers;

        final String eventType = inputs.eventType.string();
        String tag = "", eventSlug = "";

        if (editing) {
            tag = "edit_event";
            eventSlug = inputs.eventModel.eventDetails.event_slug;
        } else {
            tag = "add_event";
        }

        if (eventName.length() == 0) {
            Toast.makeText(getApplicationContext(), "Enter event name", Toast.LENGTH_SHORT).show();
            return;
        }

        String endDate;
        if (sRecurring.isChecked())
            endDate = sdfDate.format(inputs.recurringDate);
        else
            endDate = sdfDate.format(inputs.endDateTime);

        if (!validateStartAndEndDate()) {
            return;
        }
        progressDialog.setContent("Creating...");
        progressDialog.show();

        new APIManager().service.addOrUpdateEvent(
                tag,
                eventSlug,
                App.currUser.id,
                eventName,
                desc,
                officeLocation,
                sdfTime.format(inputs.startDateTime),
                sdfTime.format(inputs.endDateTime),

                sdfDate.format(inputs.startDateTime),
                endDate,

                tag_users,
                eventType,
                "",
                recurring,
                recurringDays
        ).enqueue(new Callback<ResModel>() {
            @Override
            public void onResponse(Call<ResModel> call, Response<ResModel> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body().status.equals("0")) {
                    final ResModel model = response.body();
                    String msg;

                    if (editing) {
                        if (inputs.eventType == EventType.APPO)
                            msg = "Appointment request updated!";
                        else if (inputs.eventType == EventType.CLASS)
                            msg = "Class updated!\nCode: " + model.class_code.toUpperCase();
                        else if (inputs.eventType == EventType.OFFICE)
                            msg = "Office Hour updated!";
                        else
                            msg = "Event updated!";
                    } else {
                        if (inputs.eventType == EventType.APPO)
                            msg = "Appointment request sent!";
                        else if (inputs.eventType == EventType.CLASS)
                            msg = "Class created!\nCode: " + model.class_code.toUpperCase();
                        else if (inputs.eventType == EventType.OFFICE)
                            msg = "Office Hour created!";
                        else
                            msg = "Event created!";
                    }

                    if (inputs.eventType == EventType.CLASS)
                        new AlertDialog.Builder(AddEventActivity.this)
                                .setMessage(msg)
                                .setTitle(editing ? "Class Updated" : "Class Created")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("code", model.class_code.toUpperCase()));
                                        dialogInterface.dismiss();
                                        Toast.makeText(getApplicationContext(), "Code coppied to clipboard.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    else
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                    if (inputs.eventType == EventType.APPO) {
                        Intent i = new Intent(AddEventActivity.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else
                        finish();

                } else {
                    App.showSnackBar(findViewById(android.R.id.content), getString(R.string.something_wrong)).show();
                }
            }

            @Override
            public void onFailure(Call<ResModel> call, Throwable t) {
                progressDialog.dismiss();
                App.showSnackBar(findViewById(android.R.id.content), getString(R.string.no_internet)).show();
            }
        });
    }

    boolean validateStartAndEndDate() {
        if (inputs.startDateTime.compareTo(inputs.endDateTime) > 0) {
            App.showSnackBar(findViewById(android.R.id.content), "End time can not be before start.").show();
            tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return false;
        } else {
            tvStartTime.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            return true;
        }
    }

}


