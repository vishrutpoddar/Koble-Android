package in.einfosolutions.koble.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.desmond.ripple.RippleCompat;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import in.einfosolutions.koble.Models.AddEventInput;
import in.einfosolutions.koble.Models.AllEventsModel;
import in.einfosolutions.koble.Models.EventDetailsModel;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by joker on 12/30/16.
 */

public class MakeAppoActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    public static final String KEY_INPUT_PROF = "KEY_INPUT_PROF";
    public static final int TYPE_BUSY = 1, TYPE_FREE = 2, TYPE_OFFICE = 3, TYPE_SELECT = 4;

    private static final int GAP = 15;
    public AllEventsModel allEventsModel;
    DateTimeFormatter dtfTime = DateTimeFormat.forPattern("hh:mm a");
    DateTimeFormatter dtfTimeMin = DateTimeFormat.forPattern(":mm");

    DateTimeFormatter dtfTimeShow = DateTimeFormat.forPattern("hh:mm a");

    ImageView bToolbarLeft;
    TextView bToolbarRight;
    LinearLayout llDays;
    ListView listViewHours;
    ProgressBar progressBar;
    TextView toolbarTitle, tvEmpty;
    Toolbar toolbar;
    ProfileModel professorModel;
    DateTime initTime = DateTime.now().withTimeAtStartOfDay();
    int MAX = 24 * 4;
    Viewholder[] viewholders = new Viewholder[MAX];

    int iTop = -1, iBot = -1;
    int colorSelection, colorFree, colorBusy, colorOffice;
    boolean[] slotsBusy = new boolean[MAX * 2], slotsOffice = new boolean[MAX * 2];
    boolean createdListItem = false;
    View previousSelectedDay;
    DateTime startTime, endTime;
    private TextView tvBot;
    private ListAdapter adapter = new ListAdapter();
    private FrameLayout divider;

    public void getAllEvents() {
        progressBar.setVisibility(View.VISIBLE);
        listViewHours.setVisibility(View.INVISIBLE);
        divider.setVisibility(View.INVISIBLE);
        tvEmpty.setVisibility(View.GONE);

        new APIManager().service.events_for_month("get_all_events", professorModel.id).enqueue(new Callback<AllEventsModel>() {

            @Override
            public void onResponse(Call<AllEventsModel> call, final Response<AllEventsModel> response) {
                allEventsModel = response.body();
                App.processAllEvents(allEventsModel, CalendarDay.today(), null, true);
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.GONE);
                llDays.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<AllEventsModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                llDays.setVisibility(View.GONE);
            }
        });
    }

    void getEventsForDay(DateTime dateTime) {
        int length = allEventsModel.events.size();
        ArrayList<EventModel> eventModelsForDay = new ArrayList<>();
        for (int i = 0; i < MAX; i++) {
            slotsOffice[i] = false;
            slotsBusy[i] = false;
        }
        for (int i = 0; i < length; i++) {
            EventDetailsModel item = allEventsModel.events.get(i).eventDetails;
            if (DateTimeComparator.getDateOnlyInstance().compare(dateTime, item.startDateTime) == 0) {
                eventModelsForDay.add(allEventsModel.events.get(i));

                int s = item.startDateTime.getMinuteOfDay() / GAP;
                int e = item.endDateTime.getMinuteOfDay() / GAP;
                Log.e("busy", "s=" + s + "  e=" + e);

                if (item.event_type == EventType.OFFICE) {
                    for (int j = s; j <= e; j++) {
                        slotsOffice[j] = true;
                    }
                } else {
                    for (int j = s; j <= e; j++) {
                        slotsBusy[j] = true;
                    }
                }

            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appo);

        professorModel = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_PROF), ProfileModel.class);

        llDays = (LinearLayout) findViewById(R.id.llDays);
        listViewHours = (ListView) findViewById(R.id.listViewHours);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bToolbarLeft = (ImageView) findViewById(R.id.bToolbarLeft);
        bToolbarRight = (TextView) findViewById(R.id.bToolbarRight);
        toolbarTitle = (TextView) findViewById(R.id.tvToolbarTitle);
        divider = (FrameLayout) findViewById(R.id.divider);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        tvBot = (TextView) findViewById(R.id.tvBot);
        tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        listViewHours.setVisibility(View.GONE);
        llDays.setVisibility(View.GONE);

        bToolbarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // cancel click
                finish();
            }
        });

        bToolbarRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // save click
                if (startTime == null) {
                    Toast.makeText(getApplicationContext(), "Select Slot", Toast.LENGTH_SHORT).show();
                    return;
                }
                startAddAppoAcitvity();
            }
        });

        //RippleCompat.apply(bToolbarLeft, ContextCompat.getColor(this, R.color.ripple_color));
        RippleCompat.apply(bToolbarRight, ContextCompat.getColor(this, R.color.ripple_color));

        createTopDaysView(DateTime.now());

        listViewHours.setOnItemClickListener(this);
        listViewHours.setAdapter(adapter);

        colorSelection = ContextCompat.getColor(getApplicationContext(), R.color.colorAppointmentText);
        colorFree = Color.WHITE;
        colorBusy = ContextCompat.getColor(getApplicationContext(), R.color.colorRed);
        colorOffice = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);

        for (int i = 0; i < MAX; i++) {
            slotsOffice[i] = false;
            slotsBusy[i] = false;
        }

        getAllEvents();
    }

    public void startAddAppoAcitvity() {
        Intent i = new Intent(MakeAppoActivity.this, AddEventActivity.class);
        AddEventInput input = new AddEventInput();

        input.eventType = EventType.APPO;
        input.startDateTime = startTime.toDate();
        input.endDateTime = endTime.toDate();
        input.recurringDate = startTime.plusDays(7).toDate();
        input.professorModel = professorModel;

        i.putExtra(AddEventActivity.KEY_INPUTS, App.gson.toJson(input));
        startActivity(i);
    }

    void createTopDaysView(DateTime startingFrom) {
        llDays.removeAllViews();
        for (int i = 0; i < 7; i++) {
            final DateTime date = startingFrom.plusDays(i);
            View view = getLayoutInflater().inflate(R.layout.days_item, llDays, false);
            //RippleCompat.apply(view, ContextCompat.getColor(this, R.color.ripple_color));
            TextView tvDay = (TextView) view.findViewById(R.id.tvDay);
            TextView tvDayName = (TextView) view.findViewById(R.id.tvDayName);
            tvDay.setText("" + date.getDayOfMonth());
            DateTimeFormatter dtf = DateTimeFormat.forPattern("EEE");
            tvDayName.setText(dtf.print(date).toUpperCase());
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listViewHours.setVisibility(View.VISIBLE);
                    if (previousSelectedDay != null) {
                        previousSelectedDay.setBackgroundResource(android.R.color.white);
                        TextView tvDay = (TextView) previousSelectedDay.findViewById(R.id.tvDay);
                        tvDay.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                        TextView tvDayName = (TextView) previousSelectedDay.findViewById(R.id.tvDayName);
                        tvDayName.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray));
                    }
                    previousSelectedDay = view;
                    previousSelectedDay.setBackgroundResource(R.color.colorPrimaryDark);
                    TextView tvDay = (TextView) previousSelectedDay.findViewById(R.id.tvDay);
                    tvDay.setTextColor(colorFree);
                    TextView tvDayName = (TextView) previousSelectedDay.findViewById(R.id.tvDayName);
                    tvDayName.setTextColor(colorFree);
                    onClickDayItem(date, finalI);
                }
            });
            llDays.addView(view);
        }
    }

    void onClickDayItem(DateTime date, int i) {
        getEventsForDay(date);
        createdListItem = false;
        initTime = date.withTimeAtStartOfDay();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (slotsBusy[i] /*|| slotsOffice[i]*/) {
            return;
        }
        if (iTop == -1 && iBot == -1) {
            iTop = i;
            iBot = i;
        } else if (i == iTop) {
            iTop = -1;
            iBot = -1;
        } else if (iBot == i) {
            iBot = iTop;
        } else if (i > iTop) {
            iBot = i;
            for (int j = iTop; j <= iBot; j++) {
                if (slotsBusy[j] /*|| slotsOffice[j]*/) {
                    iBot = j - 1;
                    listViewHours.smoothScrollToPosition(iTop);
                    break;
                }
            }
        } else if (i < iTop) {
            iTop = i;
            iBot = i;
        }

        Log.e("selected", "top=" + iTop + "  bot=" + iBot);

        highLightCells(TYPE_SELECT, iTop, iBot);

        if (iTop != -1 && iBot != -1) {
            startTime = viewholders[iTop].time;
            if (iBot != MAX - 1) {
                endTime = viewholders[iBot + 1].time;
            } else {
                endTime = viewholders[iBot].time.plusMinutes(GAP);
            }
            tvBot.setText(dtfTimeShow.print(startTime) + " to " + dtfTimeShow.print(endTime));
        } else {
            tvBot.setText("Select Slot");
            startTime = null;
            endTime = null;
        }
    }

    void highLightCells(int type, int to, int from) {
        for (int j = 0; j < viewholders.length; j++) {

            viewholders[j].viewSlot.setBackgroundColor(colorFree);

            if (slotsBusy[j])
                viewholders[j].viewSlot.setBackgroundColor(colorBusy);

            if (slotsOffice[j])
                viewholders[j].viewSlot.setBackgroundColor(colorOffice);
        }

        int color = colorFree;
        switch (type) {
            case TYPE_BUSY:
                color = colorBusy;
                break;
            case TYPE_FREE:
                color = colorFree;
                break;
            case TYPE_OFFICE:
                color = colorOffice;
                break;
            case TYPE_SELECT:
                color = colorSelection;
        }
        if (to != -1 && from != -1)
            for (int j = to; j <= from; j++) {
                viewholders[j].viewSlot.setBackgroundColor(color);
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
            return MAX;
        }

        @Override
        public Viewholder getItem(int i) {
            return viewholders[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            // create listView items
            if (!createdListItem) {
                createdListItem = true;
                for (int j = 0; j < MAX; j++) {
                    View v = getLayoutInflater().inflate(R.layout.slot_item, viewGroup, false);
                    viewholders[j] = new Viewholder(v);
                }
                highLightCells(TYPE_FREE, -1, -1);
            }

            Viewholder h = viewholders[i];
            DateTime time = initTime.plusMinutes(i * GAP);
            h.time = time;

            if (time.getMinuteOfHour() == 0) {
                h.viewHourLine.setVisibility(View.GONE);
                h.tvLabel.setText(dtfTime.print(time));
                h.tvLabel.setVisibility(View.VISIBLE);
                h.tvTop15.setVisibility(View.GONE);
            } else {
                h.viewHourLine.setVisibility(View.GONE);
                h.tvTop15.setText(dtfTimeMin.print(time));
                h.tvLabel.setVisibility(View.GONE);
                h.tvTop15.setVisibility(View.VISIBLE);
            }

            return viewholders[i].view;
        }
    }

    class Viewholder {
        public View view;
        public TextView tvLabel;
        public TextView tvTop15;
        public FrameLayout viewHourLine;
        public FrameLayout viewSlot;
        public DateTime time;

        public Viewholder(View view) {
            this.view = view;
            tvLabel = (TextView) view.findViewById(R.id.tvLabel);
            tvTop15 = (TextView) view.findViewById(R.id.tvTop15);
            viewHourLine = (FrameLayout) view.findViewById(R.id.viewHourLine);
            viewSlot = (FrameLayout) view.findViewById(R.id.viewSlot);
        }
    }

}
