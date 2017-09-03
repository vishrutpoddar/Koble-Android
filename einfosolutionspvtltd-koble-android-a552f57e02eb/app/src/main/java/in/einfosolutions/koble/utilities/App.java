package in.einfosolutions.koble.utilities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import eu.inloop.localmessagemanager.LocalMessageManager;
import in.einfosolutions.koble.Activities.ChatActivity;
import in.einfosolutions.koble.BuildConfig;
import in.einfosolutions.koble.Models.AllEventsModel;
import in.einfosolutions.koble.Models.AppoStatus;
import in.einfosolutions.koble.Models.Class2;
import in.einfosolutions.koble.Models.ClassResStu;
import in.einfosolutions.koble.Models.EventDetailsModel;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Models.FCNotiModel;
import in.einfosolutions.koble.Models.FcmChatProps;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.UserListRes;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.einfosolutions.koble.Activities.ChatActivity.ALL_CHATS;

/**
 * Created by joker on 12/29/16.
 */

public class App extends Application {

    public static final String KEY_PROFILE = "KEY_PROFILE";
    public static final DateTimeFormatter dtfDateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter dtfDate = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final String USER_DETAILS = "USER_DETAILS";
    public static final String GROUP_DETAILS = "GROUP_DETAILS";
    public static final int OBSERVE_USER_LIST_REFRESH = 99;
    public static final int OBSERVE_EVENTS_REFRESH = 98;
    public static final int OBSERVE_CURR_USER_REFRESH = 97;
    public static final int OBSERVE_NO_INTERNET = 100;
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String NOTIFICATION_QUEUE = "NOTIFICATION_QUEUE";
    public static Context ctx;
    public static AwesomePref prefs;
    public static ProfileModel currUser = null;
    public static Gson gson;
    public static int COLOR_RIPPLE;
    public static boolean APP_STU = false;
    public static boolean APP_PRO = false;

    public static Snackbar showSnackBar(View view, String msg) {
        final Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        return snackbar;
    }

    /**
     * Creates extra models for recurring events.
     *
     * @param allEventsModel
     * @param date           input date with calenders selected month and year
     */
    public static void processAllEvents(AllEventsModel allEventsModel, final CalendarDay date, ArrayList<CalendarDay> decorationDaysList, boolean shouldProcessRecurring) {

        if (allEventsModel.events == null) {
            allEventsModel.events = new ArrayList<>();
            return;
        }

        ArrayList<EventModel> eventsToRemove = new ArrayList<>();
        ArrayList<EventModel> eventsToAdd = new ArrayList<>();

        for (EventModel mainEventModel : allEventsModel.events) {

            if (mainEventModel.status == null) mainEventModel.status = AppoStatus.Pending;
            EventDetailsModel eventDetail = mainEventModel.eventDetails;

            eventDetail.startDateTime = dtfDateTime.parseDateTime(eventDetail.start_date + " " + eventDetail.start_time);
            eventDetail.endDateTime = dtfDateTime.parseDateTime(eventDetail.start_date + " " + eventDetail.end_time);
            eventDetail.recurringEnd = dtfDate.parseDateTime(eventDetail.end_date);

            if (eventDetail.startDateTime == null || eventDetail.endDateTime == null) {
                Log.e("wrong", App.gson.toJson(eventDetail));
                allEventsModel.events.remove(mainEventModel);
                continue;
            }

            DateTime starting = new DateTime(date.getDate()).withDayOfMonth(1);

            if (eventDetail.recurring && shouldProcessRecurring) {

                for (int j = 0; j < 32; j++) {
                    DateTime day = starting.plusDays(j);
                    //Log.e("check", "day=" + day.toString() + " start" + eventDetail.startDateTime);
                    if (day.isBefore(eventDetail.startDateTime.withTime(0, 0, 0, 0)))
                        continue;
                    if (day.isAfter(eventDetail.recurringEnd.withTime(0, 0, 0, 0)))
                        break;

                    int dayNum = (day.getDayOfWeek() == 7) ? 1 : day.getDayOfWeek() + 1;


                    if (eventDetail.recurringDays.contains(String.valueOf(dayNum))) {
                        EventModel eventModel = gson.fromJson(gson.toJson(mainEventModel), EventModel.class);

                        eventModel.eventDetails.startDateTime = eventModel.eventDetails.startDateTime.withDate(day.getYear(), day.getMonthOfYear(), day.getDayOfMonth());
                        eventModel.eventDetails.endDateTime = eventModel.eventDetails.endDateTime.withDate(day.getYear(), day.getMonthOfYear(), day.getDayOfMonth());

                        eventsToAdd.add(eventModel);
                        if (decorationDaysList != null)
                            decorationDaysList.add(CalendarDay.from(day.toDate()));

                        /*Log.e(eventDetail.event_name, ", start=" + dtfDateTime.print(eventModel.eventDetails.startDateTime)
                                + ", end=" + dtfDateTime.print(eventModel.eventDetails.endDateTime)
                                + ", req=" + dtfDate.print(eventModel.eventDetails.recurringEnd));
                        }*/
                    }
                }

                eventsToRemove.add(mainEventModel);

            } else {
                if (decorationDaysList != null)
                    decorationDaysList.add(CalendarDay.from(eventDetail.startDateTime.toDate()));
            }
        }

        for (EventModel each : eventsToRemove) {
            allEventsModel.events.remove(each);
        }
        for (EventModel each : eventsToAdd) {
            allEventsModel.events.add(each);
        }
    }

    public static Drawable getTintedDrawable(int drawableId, int colorId) {
        Drawable forTinting = DrawableCompat.wrap(ContextCompat.getDrawable(ctx, drawableId));
        int color = ContextCompat.getColor(ctx, colorId);
        DrawableCompat.setTint(forTinting.mutate(), color);
        DrawableCompat.setTintMode(forTinting, PorterDuff.Mode.SRC_IN);
        return forTinting;
    }

    public static int dp2px(int dp) {
        Resources r = ctx.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int) px;
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void updateUserIntoFireBaseDB(ProfileModel profileModel) {
        DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(USER_DETAILS).child(profileModel.id);
        fcDBRef.keepSynced(true);
        fcDBRef.setValue(profileModel);
    }

    public static void updateGroupIntoFireBaseDB(Class2 class2) {
        DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(GROUP_DETAILS).child(class2.group_code);
        fcDBRef.keepSynced(true);
        fcDBRef.setValue(class2);
    }

    public static void sendNotificationToUser(FCNotiModel model) {
        DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(NOTIFICATION_QUEUE);
        fcDBRef.push().setValue(model);
    }

    public static boolean userNotVerified(Activity activity) {
        if (!App.currUser.user_verified) {
            if (activity != null) {
                new AlertDialog.Builder(activity)
                        .setMessage(App.ctx.getString(R.string.email_not_verified))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        }
        return !App.currUser.user_verified;
    }

    public static void refreshUserList() {
        if (App.currUser == null) {
            return;
        }
        String tag = "";
        if (APP_STU)
            tag = "get_all_profs_new";
        else
            tag = "get_all_studs_new";

        new APIManager().service.getUserList(tag, App.currUser.id).enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(final Call<UserListRes> call, final Response<UserListRes> response) {
                if (response.isSuccessful() && response.body() != null) {

                    deleteStudentOrTeacherFromFireBaseChatList(response.body());
                    LocalMessageManager.getInstance().send(OBSERVE_USER_LIST_REFRESH, response.body());


                } else {
                    LocalMessageManager.getInstance().send(OBSERVE_USER_LIST_REFRESH, null);
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                LocalMessageManager.getInstance().send(OBSERVE_NO_INTERNET);
            }
        });

    }

    static void deleteStudentOrTeacherFromFireBaseChatList(final UserListRes userList) {
        // delete student or teacher from list, if not in ones current list
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(ALL_CHATS);
                final DatabaseReference fcChatList = fcDBRef.child(App.currUser.id);
                fcChatList.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String key = child.getKey();
                            FcmChatProps chatProps = child.getValue(FcmChatProps.class);
                            if (chatProps.type == ChatActivity.ChatType.GROUP) continue;

                            boolean exists = false;
                            for (ProfileModel each : userList.userList) {
                                App.updateUserIntoFireBaseDB(each);
                                if (each.id.equals(key)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                DatabaseReference deleteRef = fcChatList.child(key);
                                deleteRef.removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).start();

    }

    public static void checkIfClassUnscribed() {
        if (!APP_STU) return;
        new APIManager().service.get_all_classes_stu("get_all_groups_members", App.currUser.id).enqueue(new Callback<ClassResStu>() {
            @Override
            public void onResponse(Call<ClassResStu> call, Response<ClassResStu> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                App.removeChatsIfUnscbscribed(response.body());
            }

            @Override
            public void onFailure(Call<ClassResStu> call, Throwable t) {
            }
        });
    }

    // call when open all chats page
    public static void removeChatsIfUnscbscribed(final ClassResStu classRes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(ALL_CHATS);
                final DatabaseReference fcChatList = fcDBRef.child(App.currUser.id);
                fcChatList.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String key = child.getKey();
                            FcmChatProps chatProps = child.getValue(FcmChatProps.class);
                            if (chatProps.type == ChatActivity.ChatType.SINGLE) continue;

                            boolean exists = false;
                            for (Class2 each : classRes.array) {
                                if (each.group_code.equals(key)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                DatabaseReference deleteRef = fcChatList.child(key);
                                deleteRef.removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).start();
    }

    public static void updateUserDetails() {
        new APIManager().service.updateUserDetails("update_user",
                App.currUser.id, App.currUser.first_name,
                App.currUser.university, App.currUser.department,
                "pic_raw", App.currUser.office_room, App.currUser.desig).enqueue(new Callback<ProfileModel>() {
            @Override
            public void onResponse(Call<ProfileModel> call, Response<ProfileModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    App.currUser = response.body();
                    App.prefs.putObject(App.KEY_PROFILE, App.currUser);
                    LocalMessageManager.getInstance().send(OBSERVE_CURR_USER_REFRESH);
                }
            }

            @Override
            public void onFailure(Call<ProfileModel> call, Throwable t) {

            }
        });
    }

    public static void getAllEventsForMonth(String userId, final ArrayList<CalendarDay> decorationDaysList, final boolean shouldProcessRecurring) {

        new APIManager().service.events_for_month("get_all_events", userId).enqueue(new Callback<AllEventsModel>() {
            @Override
            public void onResponse(Call<AllEventsModel> call, final Response<AllEventsModel> response) {
                if (!response.isSuccessful()) return;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AllEventsModel model = response.body();
                        App.prefs.putObject(Const.KEY_ALL_EVENTS, response.body());
                        App.processAllEvents(model, CalendarDay.today(), decorationDaysList, shouldProcessRecurring);
                        LocalMessageManager.getInstance().send(OBSERVE_EVENTS_REFRESH, model);
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<AllEventsModel> call, Throwable t) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object obj = App.prefs.getObject(Const.KEY_ALL_EVENTS, AllEventsModel.class);
                        if (obj != null) {
                            AllEventsModel model = (AllEventsModel) obj;
                            App.processAllEvents(model, CalendarDay.today(), decorationDaysList, shouldProcessRecurring);
                            LocalMessageManager.getInstance().send(OBSERVE_EVENTS_REFRESH, model);
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * reduce image size, processes on the file itself
     *
     * @param file
     */
    public static File scaleImageFileAndReduceSize(File file) {
        int area = 60000;
        try {
            Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
            int w = b.getWidth(), h = b.getHeight();
            if (w * h <= area + 10)
                return null;

            // create new file
            File newFile = new File(App.ctx.getCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg");
            double ratio = Math.sqrt(w * h / area);
            Log.e("Scale", w + "-" + h);
            Log.e("Scale", (int) (w / ratio) + "-" + (int) (h / ratio));
            Bitmap out = Bitmap.createScaledBitmap(b, (int) (w / ratio), (int) (h / ratio), true);
            FileOutputStream fOut;
            fOut = new FileOutputStream(newFile);
            out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
            return newFile;
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            FirebaseCrash.report(e);
            return null;
        }
    }

    public static MultipartBody.Part prepareFilePart(String partName, File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        ctx = getApplicationContext();
        prefs = new AwesomePref(ctx);
        APP_STU = BuildConfig.FLAVOR.equalsIgnoreCase("professor") ? false : true;
        APP_PRO = !APP_STU;
        COLOR_RIPPLE = ContextCompat.getColor(getApplicationContext(), R.color.ripple_color);

        gson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .create();
    }

}
