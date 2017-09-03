package in.einfosolutions.koble.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.util.ArrayList;
import java.util.Date;

import eu.inloop.localmessagemanager.LocalMessage;
import eu.inloop.localmessagemanager.LocalMessageCallback;
import eu.inloop.localmessagemanager.LocalMessageManager;
import in.einfosolutions.koble.Activities.AddEventActivity;
import in.einfosolutions.koble.Activities.EventDetailsActivity;
import in.einfosolutions.koble.Adapters.OfficeHoursAdapter;
import in.einfosolutions.koble.Models.AddEventInput;
import in.einfosolutions.koble.Models.AllEventsModel;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.Activities.EventDetailsActivity.KEY_INPUT_EVENT;

public class OfficeHoursFragment extends Fragment implements LocalMessageCallback, OfficeHoursAdapter.OnItemClickListener {

    private OfficeHoursAdapter officeHoursAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<EventModel> officeHoursList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    public ProfileModel inputProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_office_hours, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        officeHoursAdapter = new OfficeHoursAdapter();
        recyclerView.setAdapter(officeHoursAdapter);
        officeHoursAdapter.setOnItemClickListener(this);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        officeHoursAdapter.setList(officeHoursList);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_office_hour, menu);
        if (inputProfile.id.equals(App.currUser.id))
            menu.findItem(R.id.menuAdd).setVisible(true);
        else
            menu.findItem(R.id.menuAdd).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAdd:
                Intent i = new Intent(getActivity(), AddEventActivity.class);
                AddEventInput input = new AddEventInput();

                Date d = new Date();
                DateTime date = new DateTime(new Date());
                DateTime dateTime = new DateTime(d).withHourOfDay(date.getHourOfDay()).withMinuteOfHour(date.getMinuteOfHour()).withSecondOfMinute(date.getSecondOfMinute());

                input.eventType = EventType.OFFICE;
                input.startDateTime = dateTime.toDate();
                input.endDateTime = dateTime.plusHours(1).toDate();
                input.recurringDate = dateTime.plusDays(7).toDate();

                i.putExtra(AddEventActivity.KEY_INPUTS, App.gson.toJson(input));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Office Hours");
        LocalMessageManager.getInstance().addListener(this);
        getOfficeHours();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalMessageManager.getInstance().removeListener(this);
    }

    void getOfficeHours() {
        tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        App.getAllEventsForMonth(inputProfile.id, null, false);
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if (localMessage.getId() == App.OBSERVE_EVENTS_REFRESH) {
            if (localMessage.getObject() != null && localMessage.getObject() instanceof AllEventsModel) {
                AllEventsModel eventsList = ((AllEventsModel) localMessage.getObject());

                progressBar.setVisibility(View.GONE);

                officeHoursList.clear();

                // remove passed office hours
                int l = eventsList.events.size();
                for (int i = 0; i < l; i++) {
                    EventModel event = eventsList.events.get(i);
                    if (event.eventDetails.event_type == EventType.OFFICE && DateTimeComparator.getDateOnlyInstance().compare(event.eventDetails.startDateTime, DateTime.now()) >= 0) {
                        officeHoursList.add(event);
                    }
                }

                officeHoursAdapter.setList(officeHoursList);

                if (eventsList.events.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            } else { // no internet
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onItemClick(View view, EventModel eventModel) {
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        intent.putExtra(KEY_INPUT_EVENT, App.gson.toJson(eventModel));
        startActivity(intent);
    }
}
