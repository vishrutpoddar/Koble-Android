package in.einfosolutions.koble.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;

import in.einfosolutions.koble.Activities.EventDetailsActivity;
import in.einfosolutions.koble.Adapters.AppoAdapter;
import in.einfosolutions.koble.Models.AllEventsModel;
import in.einfosolutions.koble.Models.EventDetailsModel;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import in.einfosolutions.koble.utilities.RecyclerTouchListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.einfosolutions.koble.Activities.EventDetailsActivity.KEY_INPUT_EVENT;

public class AppointmentsFragment extends Fragment {

    private AppoAdapter appoAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<EventModel> appoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        appoAdapter = new AppoAdapter(this);
        recyclerView.setAdapter(appoAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        appoAdapter.setList(appoList);

        getAllEvents(null);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.KEY_INPUT_EVENT, App.gson.toJson(appoList.get(position)));
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Appointments");
    }

    public void getAllEvents(final CalendarDay date) {
        progressBar.setVisibility(View.VISIBLE);
        new APIManager().service.events_for_month("get_all_events", App.currUser.id).enqueue(new Callback<AllEventsModel>() {
            @Override
            public void onResponse(Call<AllEventsModel> call, final Response<AllEventsModel> response) {
                progressBar.setVisibility(View.GONE);
                AllEventsModel allEventsModel = response.body();
                int l = allEventsModel.events.size();
                ArrayList<EventModel> appoArray = new ArrayList<EventModel>();
                for (int i = 0; i < l; i++) {

                    EventDetailsModel eventDetail = allEventsModel.events.get(i).eventDetails;

                    eventDetail.startDateTime = App.dtfDateTime.parseDateTime(eventDetail.start_date + " " + eventDetail.start_time);
                    eventDetail.endDateTime = App.dtfDateTime.parseDateTime(eventDetail.start_date + " " + eventDetail.end_time);
                    eventDetail.recurringEnd = App.dtfDateTime.parseDateTime(eventDetail.end_date + " " + eventDetail.end_time);

                    if (allEventsModel.events.get(i).eventDetails.event_type == EventType.APPO)
                        appoArray.add(allEventsModel.events.get(i));
                }
                appoList = appoArray;
                appoAdapter.setList(appoList);
            }

            @Override
            public void onFailure(Call<AllEventsModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

}
