package in.einfosolutions.koble.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;

import in.einfosolutions.koble.Activities.EventDetailsActivity;
import in.einfosolutions.koble.Models.AllEventsModel;
import in.einfosolutions.koble.Models.EventDetailsModel;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.AnyDayDecorator;
import in.einfosolutions.koble.utilities.App;
import in.einfosolutions.koble.utilities.Const;
import in.einfosolutions.koble.utilities.EventDecorator;
import in.einfosolutions.koble.utilities.TodayDecorator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.einfosolutions.koble.Activities.EventDetailsActivity.KEY_INPUT_EVENT;

public class ScheduleFragmentCopy extends Fragment implements OnMonthChangedListener, OnDateSelectedListener, AdapterView.OnItemClickListener {

    public static final DateTimeFormatter dtfTimeShow = DateTimeFormat.forPattern("hh:mm a");
    public MaterialCalendarView calendarView;
    ProgressBar avi;
    ListView listView;
    ListAdapter adapter;
    AnyDayDecorator anyDayDecorator = new AnyDayDecorator();
    String tag = "get_all_events";
    TextView tvNoEvents;
    AllEventsModel mainAllEventsModel; // using by UI thread
    ArrayList<CalendarDay> decorationDaysList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule_copy, container, false);

        calendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView);
        listView = (ListView) view.findViewById(R.id.event_list_view);
        tvNoEvents = (TextView) view.findViewById(R.id.tvNoEvents);
        avi = (ProgressBar) view.findViewById(R.id.avi);

        avi.setIndeterminate(true);
        avi.setVisibility(View.GONE);
        tvNoEvents.setVisibility(View.INVISIBLE);

        avi.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

        calendarView.setOnMonthChangedListener(this);
        calendarView.setOnDateChangedListener(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        calendarView.setTileHeight(displaymetrics.heightPixels / 16);

        calendarView.setSelectedDate(CalendarDay.today());

        if (adapter == null) adapter = new ListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_schedule, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuGoToday:
                calendarView.setSelectedDate(new Date());
                calendarView.setCurrentDate(new Date());
                onDateSelected(calendarView, CalendarDay.today(), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getAllEvents2() { // not working
        avi.setVisibility(View.VISIBLE);
        adapter.setList(new ArrayList<EventModel>());
        tvNoEvents.setVisibility(View.INVISIBLE);
        decorationDaysList = new ArrayList<>();
        App.getAllEventsForMonth(App.currUser.id, decorationDaysList, true);
    }

    /*@Override // not working
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if (localMessage.getId() == App.OBSERVE_EVENTS_REFRESH) {
            if (localMessage.getObject() != null && localMessage.getObject() instanceof AllEventsModel) {
                AllEventsModel eventsList = ((AllEventsModel) localMessage.getObject());
                mainAllEventsModel = eventsList;
                calendarView.removeDecorators();
                calendarView.addDecorators(new EventDecorator(Color.RED, decorationDaysList), new TodayDecorator(), anyDayDecorator);

                CalendarDay date = calendarView.getSelectedDate();
                adapter.setList(getEventsForADay(new DateTime(date.getDate())));
                onDateSelected(calendarView, date, true);
                calendarView.setSelectedDate(date);
                avi.setVisibility(View.GONE);
            } else { // no internet

            }
        }
    }*/

    public void getAllEvents(final CalendarDay date) {
        avi.setVisibility(View.VISIBLE);
        adapter.setList(new ArrayList<EventModel>());
        tvNoEvents.setVisibility(View.INVISIBLE);
        new APIManager().service.events_for_month(tag, App.currUser.id).enqueue(new Callback<AllEventsModel>() {
            @Override
            public void onResponse(Call<AllEventsModel> call, final Response<AllEventsModel> response) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        App.prefs.putObject(Const.KEY_ALL_EVENTS, response.body());
                        decorationDaysList = new ArrayList<>();
                        App.processAllEvents(response.body(), date, decorationDaysList, true);
                        mainAllEventsModel = response.body();
                        final ArrayList<EventModel> eventsForDay = getEventsForADay(new DateTime(date.getDate()));
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    calendarView.removeDecorators();
                                    calendarView.addDecorators(new EventDecorator(Color.RED, decorationDaysList), new TodayDecorator(), anyDayDecorator);

                                    adapter.setList(eventsForDay);
                                    onDateSelected(calendarView, date, true);
                                    calendarView.setSelectedDate(date);
                                    avi.setVisibility(View.GONE);
                                }
                            });
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<AllEventsModel> call, Throwable t) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AllEventsModel model = (AllEventsModel) App.prefs.getObject(Const.KEY_ALL_EVENTS, AllEventsModel.class);
                        decorationDaysList = new ArrayList<>();
                        App.processAllEvents(model, calendarView.getSelectedDate(), decorationDaysList, true);
                        mainAllEventsModel = model;
                        final ArrayList<EventModel> eventsForDay = getEventsForADay(new DateTime(date.getDate()));
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    calendarView.removeDecorators();
                                    calendarView.addDecorators(new EventDecorator(Color.RED, decorationDaysList), new TodayDecorator(), anyDayDecorator);

                                    adapter.setList(eventsForDay);
                                    onDateSelected(calendarView, date, true);
                                    calendarView.setSelectedDate(date);
                                    avi.setVisibility(View.GONE);
                                }
                            });
                    }
                }).start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //LocalMessageManager.getInstance().addListener(this);
        getAllEvents(calendarView.getSelectedDate());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Schedule");
        App.updateUserDetails();
    }

    @Override
    public void onPause() {
        super.onPause();
        //LocalMessageManager.getInstance().removeListener(this);
    }


    /**
     * filters events for a day only
     *
     * @param dateTime input day, month, year
     * @return returns arraylist of EventModel
     */

    ArrayList<EventModel> getEventsForADay(DateTime dateTime) {
        ArrayList<EventModel> eventsList = new ArrayList<>();

        if (mainAllEventsModel == null) return eventsList;

        int length = mainAllEventsModel.events.size();
        for (int i = 0; i < length; i++) {
            EventDetailsModel event = mainAllEventsModel.events.get(i).eventDetails;
            //Log.e("prob", "sel:" + dateTime.getDayOfMonth() + "   date:" + event.startDateTime.getDayOfMonth());
            if (DateTimeComparator.getDateOnlyInstance().compare(dateTime, event.startDateTime) == 0) {
                eventsList.add(mainAllEventsModel.events.get(i));
            }
        }
        return eventsList;
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        getAllEvents(date);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull final CalendarDay date, boolean selected) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DateTime selDate = new DateTime(date.getDate());
                final ArrayList<EventModel> eventsList = getEventsForADay(selDate);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (eventsList.size() != 0) {
                            adapter.setList(eventsList);
                            listView.setVisibility(View.VISIBLE);
                            tvNoEvents.setVisibility(View.GONE);
                        } else {
                            tvNoEvents.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                        }
                        anyDayDecorator.setDate(date.getDate());
                    }
                });
            }
        }).start();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
        EventModel obj = ((ListAdapter) adapterView.getAdapter()).eventsList.get(i);

        Log.e("json", App.gson.toJson(obj.eventDetails));

        intent.putExtra(KEY_INPUT_EVENT, App.gson.toJson(obj));
        startActivity(intent);
    }

    public static class EventItemHolder extends RecyclerView.ViewHolder {

        public CardView eventCard;
        public TextView tvName;
        public TextView tvTime;
        public TextView tvType;
        public TextView tvStatus;

        public View root;

        public EventItemHolder(View view) {
            super(view);
            root = view;

            eventCard = (CardView) view.findViewById(R.id.event_card);
            tvName = (TextView) view.findViewById(R.id.card_event_name);
            tvTime = (TextView) view.findViewById(R.id.card_event_time);
            tvType = (TextView) view.findViewById(R.id.card_event_type);
            tvStatus = (TextView) view.findViewById(R.id.card_event_status);
        }
    }

    class ListAdapter extends BaseAdapter {

        ArrayList<EventModel> eventsList = new ArrayList<>();

        void setList(ArrayList<EventModel> eventsList) {
            this.eventsList = eventsList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return eventsList.size();
        }

        @Override
        public EventModel getItem(int i) {
            return eventsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.card_event, viewGroup, false);
            }

            //EventItemHolder h = new EventItemHolder(view);

            EventModel item = getItem(i);

            CardView eventCard = (CardView) view.findViewById(R.id.event_card);
            TextView tvName = (TextView) view.findViewById(R.id.card_event_name);
            TextView tvTime = (TextView) view.findViewById(R.id.card_event_time);
            TextView tvType = (TextView) view.findViewById(R.id.card_event_type);
            TextView tvStatus = (TextView) view.findViewById(R.id.card_event_status);

            tvTime.setText(dtfTimeShow.print(item.eventDetails.startDateTime) + " to " + dtfTimeShow.print(item.eventDetails.endDateTime));
            tvType.setText(item.eventDetails.event_type.stringVal());
            tvName.setText(item.eventDetails.event_name);
            if (item.eventDetails.event_type == EventType.APPO) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText(item.status.toString());
            } else {
                tvStatus.setVisibility(View.GONE);
            }
                tvStatus.setText(item.status.stringVal());
            return view;
        }
    }

}
