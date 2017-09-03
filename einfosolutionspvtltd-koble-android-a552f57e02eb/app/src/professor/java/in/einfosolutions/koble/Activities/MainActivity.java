package in.einfosolutions.koble.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;

import in.einfosolutions.koble.Fragments.AllChatsFragment;
import in.einfosolutions.koble.Fragments.AppointmentsFragment;
import in.einfosolutions.koble.Fragments.ClassesFragment;
import in.einfosolutions.koble.Fragments.NotificationsFragment;
import in.einfosolutions.koble.Fragments.OfficeHoursFragment;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

public class MainActivity extends BaseMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FloatingActionButton officeHours = new FloatingActionButton(this);
        officeHours.setIcon(R.drawable.office_hours);
        officeHours.setColorNormalResId(R.color.colorPrimaryDark);
        officeHours.setColorPressedResId(R.color.colorPrimaryDarkPressed);
        officeHours.setTitle("Add Office Hours");
        officeHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.userNotVerified(MainActivity.this)) {
                } else
                    startAddEventAcitvity(EventType.OFFICE);
                floatingActionsMenu.collapse();
            }
        });
        floatingActionsMenu.addButton(officeHours);

        FloatingActionButton classes = new FloatingActionButton(this);
        classes.setIcon(R.drawable.classes);
        classes.setColorNormalResId(R.color.colorPrimaryDark);
        classes.setColorPressedResId(R.color.colorPrimaryDarkPressed);
        classes.setTitle("Create Class");
        classes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (App.userNotVerified(MainActivity.this)) {
                } else
                    startAddEventAcitvity(EventType.CLASS);
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

    public void selectTopNavigationItem(MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.schedule:
                fragment = scheduleFragment;
                break;
            case R.id.classes:
                fragment = new ClassesFragment();
                break;
            case R.id.appointments:
                fragment = new AppointmentsFragment();
                break;
            case R.id.office_hours:
                OfficeHoursFragment f = new OfficeHoursFragment();
                f.inputProfile = App.currUser;
                fragment = f;
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

}