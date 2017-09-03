package in.einfosolutions.koble.Network;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import in.einfosolutions.koble.Models.AllEventsModel;
import in.einfosolutions.koble.Models.ClassMembersRes;
import in.einfosolutions.koble.Models.ClassResProf;
import in.einfosolutions.koble.Models.ClassResStu;
import in.einfosolutions.koble.Models.JoinClassRes;
import in.einfosolutions.koble.Models.NotiModel;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.ResModel;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Models.Unused;
import in.einfosolutions.koble.Models.UserListRes;
import in.einfosolutions.koble.Push.FirebaseNoti;
import in.einfosolutions.koble.utilities.App;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public class APIManager {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String PIC_BASE_URL = "http://www.einfodemolink.com/cc/pics/";
    public static String BaseURL = "http://einfodemolink.com/cc/";
    public APIService service;
    private Retrofit retrofit;


    public APIManager() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(APIService.class);
    }

    public static void sendPushNotificatonToFirebaseServer(FirebaseNoti firebaseNoti) {
        final String KEY = "AAAA7l6A1jI:APA91bH2oUNSdtifz3qE9RAynqSEa3JgkXARdH6tD_3S8cm9--cSU5Qz0-zfgVQ2aTnYWvsypmZv0mMXUBxqa6R8i14sgMeW0HF2B3WlEFRstdQ3HXrvES9mUZk8YfXr82gLEDC8uGQD";
        String json = App.gson.toJson(firebaseNoti);
        Log.e("SendingPushJSON", json);
        RequestBody body = RequestBody.create(JSON, json);
        final Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=" + KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    executeRequest(request);
                }
            }).start();
        } else {
            executeRequest(request);
        }
    }

    private static void executeRequest(Request request) {
        final OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("SendingPushErr", App.gson.toJson(call));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                Log.e("SendingPush", response.body().string());
            }
        });
    }

    public interface APIService {

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ResModel> addOrUpdateEvent(
                @Field("tag") String tag, // add_event / edit_event
                @Field("event_slug") String eventSlug,
                @Field("username") String username,
                @Field("event_name") String event_name,
                @Field("desc") String desc,
                @Field("location") String location,
                @Field("start_time") String start_time,
                @Field("end_time") String end_time,
                @Field("start") String start,
                @Field("end") String end,
                @Field("tag_users") String tag_users,
                @Field("type") String type,
                @Field("notes") String notes,
                @Field("recurring") String recurring,
                @Field("days") String recurringDays
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ProfileModel> login(
                @Field("tag") String tag,
                @Field("email") String email,
                @Field("password") String password
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ProfileModel> register(
                @Field("tag") String tag,
                @Field("name") String name,
                @Field("email") String email,
                @Field("password") String password,
                @Field("univ") String university,
                @Field("dept") String department,
                @Field("office_room") String office_room,
                @Field("desig") String designation,
                @Field("token") String token
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<AllEventsModel> events_for_month(
                @Field("tag") String tag, // get_all_events
                @Field("username") String username
        );


        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<JoinClassRes> joinClass( // redeem_code
                                      @Field("tag") String tag,
                                      @Field("userid") String email,
                                      @Field("class_code") String password
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ClassResProf> get_all_classes_prof(
                @Field("tag") String tag,
                @Field("userid") String userid
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ClassMembersRes> get_class_members(
                @Field("tag") String tag,
                @Field("class_code") String class_code
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ClassResStu> get_all_classes_stu(
                @Field("tag") String tag, // get_all_groups_members / get_all_groups_members_sort
                @Field("userid") String userid
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Unused> updateDeviceToken(
                @Field("tag") String tag,
                @Field("userid") String userid,
                @Field("token") String token
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<ProfileModel> updateUserDetails(
                @Field("tag") String tag, // update_user
                @Field("userid") String userid,
                @Field("name") String name,
                @Field("univ") String univ,
                @Field("dept") String dept,
                @Field("pic_raw") String pic_raw, // "raw_image"
                @Field("desig") String desig,
                @Field("office_room") String office_room
        );

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<UserListRes> getUserList(
                @Field("tag") String tag, // get_all_profs_new / get_all_studs_new
                @Field("userid") String userid);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<NotiModel.NotiRes> getNotifications(
                @Field("tag") String tag, // notification
                @Field("userid") String userid);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> dismissNotifications(
                @Field("tag") String tag, // notification_remove
                @Field("userid") String userid);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> deleteUserFromClass(
                @Field("tag") String tag, // delete_user_from_class
                @Field("group_code") String groupCode,
                @Field("prof_id") String profId,
                @Field("del_user_id") String delUserId);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> deleteEvent(
                @Field("tag") String tag, // delete_event
                @Field("userid") String userid,
                @Field("event_slug") String eventSlug);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> updateNotificationStatus(
                @Field("tag") String tag, // update_notification
                @Field("event_slug") String event_slug,
                @Field("status") String status,
                @Field("tagged_username") String tagged_username,
                @Field("tagged_by_username") String tagged_by_username,
                @Field("msg") String msg);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> studentLeaveClass(
                @Field("tag") String tag, // student_leave_class
                @Field("class_code") String classCode,
                @Field("userid") String userid);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> profDeleteClass(
                @Field("tag") String tag, // delete_event
                @Field("event_slug") String event_slug,
                @Field("userid") String userid);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> removeProfilePic(
                @Field("tag") String tag, // delete_img
                @Field("user_id") String userid);

        @Multipart
        @POST("ios_v2.php") // tag = update_profile_image, userid
        Call<Response1> changeProfilePic(@PartMap() Map<String, RequestBody> partMap,
                                         @Part MultipartBody.Part image);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> submitFeedback(
                @Field("tag") String tag, // feedback
                @Field("userid") String userid);

        @FormUrlEncoded
        @POST("ios_v2.php")
        Call<Response1> forgotPassApi(
                @Field("tag") String tag, // forgot_pass
                @Field("email") String email);

    }

}
