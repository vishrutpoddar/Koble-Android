package in.einfosolutions.koble.Activities;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.DateTime;

import java.util.Date;

import in.einfosolutions.koble.Fragments.FeedbackFragment;
import in.einfosolutions.koble.Fragments.ProfileFragment;
import in.einfosolutions.koble.Fragments.ScheduleFragmentCopy;
import in.einfosolutions.koble.Models.AddEventInput;
import in.einfosolutions.koble.Models.Class2;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.Push.Data;
import in.einfosolutions.koble.Push.MyFirebaseInstanceIdService;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.utilities.App.GROUP_DETAILS;
import static in.einfosolutions.koble.utilities.App.USER_DETAILS;

/**
 * Created by joker on 1/13/17.
 */

public class BaseMainActivity extends BaseActivity {

    public static final String INPUT_RECEIVED_NOTI = "INPUT_RECEIVED_NOTI";

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FloatingActionsMenu floatingActionsMenu;
    View blurView;

    ScheduleFragmentCopy scheduleFragment = new ScheduleFragmentCopy();

    // for blurview
    int colorVisible = Color.argb(200, 255, 255, 255);
    int colorGone = Color.argb(0, 255, 255, 255);
    long durBlur = 100;
    ValueAnimator animVisible;
    ValueAnimator animGone;
    private RoundedImageView ivLogo;
    private TextView profileName;
    public NavigationView navigationViewTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
        blurView = findViewById(R.id.blurView);

        animVisible = new ValueAnimator();
        animVisible.setIntValues(colorGone, colorVisible);
        animVisible.setEvaluator(new ArgbEvaluator());
        animVisible.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                blurView.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });
        animVisible.setDuration(durBlur);
        animVisible.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                blurView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animGone = new ValueAnimator();
        animGone.setIntValues(colorVisible, colorGone);
        animGone.setEvaluator(new ArgbEvaluator());
        animGone.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                blurView.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });
        animGone.setDuration(durBlur);
        animGone.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                blurView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                blurView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        blurView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floatingActionsMenu.collapse();
            }
        });

        floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                animVisible.start();
            }

            @Override
            public void onMenuCollapsed() {
                animGone.start();
            }
        });

        initialiseDrawer();
    }

    // please override
    public void selectTopNavigationItem(MenuItem item) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(this)
                .load(APIManager.PIC_BASE_URL + App.currUser.pic)
                .crossFade()
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .dontAnimate()
                .into(ivLogo);
        profileName.setText(App.currUser.first_name);
        MyFirebaseInstanceIdService.updateTokenToPhpServer();
    }

    public void selectBottomNavigationItem(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.feedback:
                Fragment fragment = new  FeedbackFragment();
                floatingActionsMenu.setVisibility(View.GONE);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                break;
            case R.id.logout:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                logout();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
                break;
        }

        drawerLayout.closeDrawers();
    }

    void logout() {
        App.prefs.putObject(App.KEY_PROFILE, null);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    public void startAddEventAcitvity(EventType type) {
        Intent i = new Intent(this, AddEventActivity.class);
        AddEventInput input = new AddEventInput();

        Date d = scheduleFragment.calendarView.getSelectedDate().getDate();
        DateTime date = new DateTime(new Date());
        DateTime dateTime = new DateTime(d).withHourOfDay(date.getHourOfDay()).withMinuteOfHour(date.getMinuteOfHour()).withSecondOfMinute(date.getSecondOfMinute());

        input.eventType = type;
        input.startDateTime = dateTime.toDate();
        input.endDateTime = dateTime.plusHours(1).toDate();
        input.recurringDate = dateTime.plusDays(7).toDate();

        i.putExtra(AddEventActivity.KEY_INPUTS, App.gson.toJson(input));
        startActivity(i);
    }

    public void initialiseDrawer() {

        navigationViewTop = (NavigationView) findViewById(R.id.navigation_view_top);
        NavigationView navigationViewBottom = (NavigationView) findViewById(R.id.navigation_view_bottom);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        View drawerHeader = navigationViewTop.getHeaderView(0);
        ivLogo = (RoundedImageView) drawerHeader.findViewById(R.id.profile_image);
        profileName = (TextView) drawerHeader.findViewById(R.id.profile_name);
        profileName.setText(App.currUser.first_name);

        Glide.with(this)
                .load(APIManager.PIC_BASE_URL + App.currUser.pic)
                .crossFade()
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .dontAnimate()
                .into(ivLogo);

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileFragment profileFragment = new ProfileFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                floatingActionsMenu.setVisibility(View.GONE);
                drawerLayout.closeDrawers();
            }
        });

        drawerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        navigationViewTop.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectTopNavigationItem(item);
                return true;
            }
        });

        navigationViewBottom.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectBottomNavigationItem(item);
                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                floatingActionsMenu.collapse();
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // handle notification
        if (getIntent().hasExtra(INPUT_RECEIVED_NOTI)) {
            Data recievedNoti = App.gson.fromJson(getIntent().getStringExtra(INPUT_RECEIVED_NOTI), Data.class);
            selectTopNavigationItem(navigationViewTop.getMenu().findItem(R.id.chats));

            // clear all notifications
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();

            if (recievedNoti.chat_type.equals("SINGLE")) {
                DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(USER_DETAILS).child(recievedNoti.sender_id);
                fcDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            ProfileModel profileModel = dataSnapshot.getValue(ProfileModel.class);
                            if (profileModel == null) return;
                            Intent i2 = new Intent(BaseMainActivity.this, ChatActivity.class);
                            i2.putExtra(ChatActivity.KEY_INPUT_OTHER_USER, App.gson.toJson(profileModel));
                            startActivity(i2);
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(GROUP_DETAILS).child(recievedNoti.sender_id);
                fcDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            final Class2 class2 = dataSnapshot.getValue(Class2.class);
                            if (class2 == null) return;
                            Intent i = new Intent(BaseMainActivity.this, GroupChatActivity.class);
                            i.putExtra(GroupChatActivity.KEY_INPUT_CLASS, App.gson.toJson(class2));
                            startActivity(i);
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else {
            selectTopNavigationItem(navigationViewTop.getMenu().getItem(0));
        }
    }

}
