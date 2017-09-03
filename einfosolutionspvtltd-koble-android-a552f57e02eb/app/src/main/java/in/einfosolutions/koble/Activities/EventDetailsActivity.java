package in.einfosolutions.koble.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import in.einfosolutions.koble.Models.AddEventInput;
import in.einfosolutions.koble.Models.AppoStatus;
import in.einfosolutions.koble.Models.EventDetailsModel;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.einfosolutions.koble.Activities.AddEventActivity.KEY_INPUTS;
import static in.einfosolutions.koble.Models.AppoStatus.Accepted;
import static in.einfosolutions.koble.Models.AppoStatus.Declined;
import static in.einfosolutions.koble.Models.AppoStatus.Pending;
import static in.einfosolutions.koble.Models.AppoStatus.ResolveViaChat;
import static in.einfosolutions.koble.Models.AppoStatus.Resolved;
import static in.einfosolutions.koble.utilities.App.APP_PRO;
import static in.einfosolutions.koble.utilities.App.APP_STU;

/**
 * Created by joker on 1/4/17.
 */

public class EventDetailsActivity extends BaseActivity {

    public static final String KEY_INPUT_EVENT = "KEY_INPUT_EVENT";
    public static final DateTimeFormatter dtfDateShow = DateTimeFormat.forPattern("EEEE, dd MMMM yyyy");
    public static final DateTimeFormatter dtfTimeShow = DateTimeFormat.forPattern("hh:mm a");
    static final String deleteTitleRespond = "Respond";
    static final String deleteTitleChangeRespond = "Change Respond";
    static final String deleteTitleDelete = "Delete";
    static final String deleteTitleLeaveClass = "Leave Class";
    static final String deleteTitleDeleteClass = "Delete Class";
    static final String deleteTitleDeleteEvent = "Delete Event";
    static final String deleteTitleCancelAppo = "Cancel Appointment";
    Toolbar toolbar;
    EventModel eventModel;
    EventDetailsModel eventDetails;

    MenuItem menuEdit, menuDelete;
    String menuDeleteTitle = "Delete";
    boolean menuEditVisible = true;
    boolean menuDeleteVisible = true;
    int singleChoiceIndex = -1;
    private TextView tvTitle;
    private TextView tvDetail;
    private TextView tvTime;
    private TextView tvLocation;
    private TextView tvEventTypeTitle;
    private TextView tvEventType;
    private Button bShareCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Event Details");

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDetail = (TextView) findViewById(R.id.tvDetail);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvEventTypeTitle = (TextView) findViewById(R.id.tvEventTypeTitle);
        tvEventType = (TextView) findViewById(R.id.tvEventType);
        bShareCode = (Button) findViewById(R.id.bShareCode);

        eventModel = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_EVENT), EventModel.class);
        eventDetails = eventModel.eventDetails;

    }

    @Override
    protected void onResume() {
        super.onResume();
        configureViews();
    }

    // Hide & display views according to Student & Professor app
    void configureViews() {
        tvTitle.setText(eventDetails.event_name);
        tvDetail.setText(eventDetails.description);
        String time = dtfDateShow.print(eventDetails.startDateTime) +
                "\n" + dtfTimeShow.print(eventDetails.startDateTime) + " to " + dtfTimeShow.print(eventDetails.endDateTime);
        tvTime.setText(time);
        tvLocation.setText(eventDetails.location);

        String typeTitle = "";
        String type = "";

        bShareCode.setVisibility(View.GONE);

        switch (eventDetails.event_type) {
            case CLASS:
                typeTitle = "Class Code";
                getSupportActionBar().setTitle("Class Details");
                type = eventModel.classDetails.code.toUpperCase();
                bShareCode.setVisibility(View.VISIBLE);
                if (APP_STU) {
                    menuEditVisible = false;
                    menuDeleteTitle = deleteTitleLeaveClass;
                }
                break;
            case APPO:
                getSupportActionBar().setTitle("Appointment");
                tvDetail.setText(eventModel.taggedBy.get(0).first_name + "\n" + eventDetails.description);
                typeTitle = "Appointment Status";
                type = eventModel.status.toString();

                if (eventModel.status == AppoStatus.ResolveViaChat) {
                    tvEventType.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                    tvEventType.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startResolveViaChat();
                        }
                    });
                }
                if (APP_STU) {
                    if (eventModel.status == Declined || eventModel.status == Resolved) {
                        menuDeleteVisible = false;
                    } else {
                        menuDeleteTitle = deleteTitleCancelAppo;
                    }
                } else {
                    if (eventModel.status == Pending) {
                        menuDeleteTitle = deleteTitleRespond;
                    } else if (eventModel.status == Accepted) {
                        menuDeleteTitle = deleteTitleChangeRespond;
                    } else if (eventModel.status == Declined) {
                        menuDeleteTitle = deleteTitleDelete;
                    } else {
                        menuDeleteTitle = deleteTitleCancelAppo;
                    }
                }
                menuEditVisible = false;
                break;
            case EVENT:
                getSupportActionBar().setTitle("Event Details");
                typeTitle = "Type";
                type = eventDetails.event_type.stringVal();
                if (!eventModel.eventDetails.username.equals(App.currUser.id)) {
                    menuEditVisible = false;
                }
                break;
            case OFFICE:
                getSupportActionBar().setTitle("Office Hour");
                typeTitle = "Type";
                type = eventDetails.event_type.stringVal();
                if (APP_STU) {
                    menuEditVisible = false;
                    menuDeleteVisible = false;
                }
                break;
            case ICAL:
                getSupportActionBar().setTitle("Event Details");
                typeTitle = "Type";
                type = eventDetails.event_type.stringVal();
                menuEditVisible = false;
                break;
            case GOOGLE:
                getSupportActionBar().setTitle("Event Details");
                typeTitle = "Type";
                type = eventDetails.event_type.stringVal();
                menuEditVisible = false;
                break;
        }

        tvEventTypeTitle.setText(typeTitle);
        tvEventType.setText(type);

        //invalidateOptionsMenu();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_event_details, menu);
        menuEdit = menu.findItem(R.id.menu_item_edit);
        menuDelete = menu.findItem(R.id.menu_item_del);

        menuEdit.setVisible(menuEditVisible);
        menuDelete.setVisible(menuDeleteVisible);
        menuDelete.setTitle(menuDeleteTitle);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_item_edit:
                finish();

                Intent i = new Intent(this, AddEventActivity.class);
                AddEventInput input = new AddEventInput();

                input.startDateTime = eventDetails.startDateTime.toDate();
                input.endDateTime = eventDetails.endDateTime.toDate();
                input.recurringDate = eventDetails.recurringEnd.toDate();
                input.eventType = eventDetails.event_type;
                input.eventModel = eventModel;

                i.putExtra(KEY_INPUTS, App.gson.toJson(input));
                startActivity(i);
                return true;
            case R.id.menu_item_del:
                deleteButtonTap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void deleteButtonTap() {
        if (eventDetails.event_type == EventType.APPO && APP_PRO) {
            if (eventModel.status == Pending) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(menuDelete.getTitle())
                        .items("Accept", "Decline", "Resolve Via Chat")
                        .alwaysCallSingleChoiceCallback()
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                singleChoiceIndex = which;
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                return true;
                            }
                        })
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (singleChoiceIndex == 0) { // accept
                                    respondToAppointment(eventDetails.event_slug,
                                            AppoStatus.Accepted, App.currUser.id, eventDetails.username);
                                } else if (singleChoiceIndex == 1) { // decline
                                    new MaterialDialog.Builder(EventDetailsActivity.this)
                                            .title("Warning")
                                            .content("Are you sure?")
                                            .negativeText("Cancel")
                                            .positiveText("Decline")
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    respondToAppointment(eventDetails.event_slug,
                                                            AppoStatus.Declined, App.currUser.id, eventDetails.username);
                                                }
                                            }).show();
                                } else if (singleChoiceIndex == 2) { // Resolve Via Chat
                                    respondToAppointment(eventDetails.event_slug,
                                            AppoStatus.ResolveViaChat, App.currUser.id, eventDetails.username);
                                } else {

                                }
                            }
                        })
                        .negativeText("Cancel")
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                singleChoiceIndex = -1;
                            }
                        }).build();
                dialog.show();
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            } else if (eventModel.status == Accepted) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(menuDelete.getTitle())
                        .items(deleteTitleCancelAppo, "Resolve Via Chat")
                        .alwaysCallSingleChoiceCallback()
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                singleChoiceIndex = which;
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                return true;
                            }
                        })
                        .positiveText("OK")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (singleChoiceIndex == 0) { // Cancel Appo / Delete
                                    respondToAppointment(eventDetails.event_slug,
                                            AppoStatus.Declined, App.currUser.id, eventDetails.username);
                                } else if (singleChoiceIndex == 1) { // Resolve via chat
                                    respondToAppointment(eventDetails.event_slug,
                                            AppoStatus.ResolveViaChat, App.currUser.id, eventDetails.username);
                                }
                            }
                        })
                        .negativeText("Cancel")
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                singleChoiceIndex = -1;
                            }
                        }).build();
                dialog.show();
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
            } else {
                deleteStart();
            }
        } else {
            deleteStart();
        }
    }

    void deleteStart() {
        if (eventDetails.event_type == EventType.ICAL || eventDetails.event_type == EventType.GOOGLE) {
            new AlertDialog.Builder(this).setMessage("Cannot delete " + eventDetails.event_type.stringVal() + "events!").show();
            return;
        }
        new MaterialDialog.Builder(this)
                .title("Warning")
                .content("Sure you want to delete?")
                .negativeText("Cancel")
                .positiveText("Delete")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteApiCall();
                    }
                }).show();
    }

    void deleteApiCall() {
        String createdById = eventDetails.username;
        final String type = eventDetails.event_type.stringVal();
        progressDialog.setContent("Deleting...");
        progressDialog.show();
        Callback<Response1> handler = new Callback<Response1>() {
            @Override
            public void onResponse(Call<Response1> call, Response<Response1> response) {
                progressDialog.hide();
                if (!response.isSuccessful()) return;
                if (response.body().status.equals("0"))
                    Toast.makeText(getApplicationContext(), type + " Deleted!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Response1> call, Throwable t) {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        };

        if (APP_STU && eventDetails.event_type == EventType.CLASS) {
            new APIManager().service.deleteUserFromClass("delete_user_from_class",
                    eventModel.classDetails.code,
                    createdById,
                    App.currUser.id).enqueue(handler);
        } else {
            new APIManager().service.deleteEvent("delete_event", createdById, App.currUser.id).enqueue(handler);
        }

    }

    void respondToAppointment(String eventSlug, final AppoStatus answer, String tagged, String taggedBy) {
        progressDialog.setContent("Responding...");
        progressDialog.show();
        new APIManager().service.updateNotificationStatus(
                "update_notification",
                eventSlug,
                answer.toString(),
                tagged,
                taggedBy,
                "").enqueue(new Callback<Response1>() {
            @Override
            public void onResponse(Call<Response1> call, Response<Response1> response) {
                if (!response.isSuccessful()) return;
                progressDialog.hide();
                if (response.body().status.equals("0")) {
                    if (answer == ResolveViaChat) {
                        startResolveViaChat();
                    } else {
                        new MaterialDialog.Builder(EventDetailsActivity.this)
                                .content("Request " + answer.toString())
                                .positiveText("OK").show();
                    }
                    eventModel.status = answer;
                    configureViews();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Response1> call, Throwable t) {
                progressDialog.hide();
                Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void startResolveViaChat() {
        Intent i = new Intent(this, ChatActivity.class);
        ProfileModel otherUser = APP_STU ? eventModel.taggedUsers.get(0) : eventModel.taggedBy.get(0);
        i.putExtra(ChatActivity.KEY_INPUT_OTHER_USER, App.gson.toJson(otherUser));
        startActivity(i);
    }

}
