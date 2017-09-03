package in.einfosolutions.koble.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import in.einfosolutions.koble.Fragments.ScheduleFragmentCopy;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.R;

/**
 * Created by joker on 5/10/17.
 */

public class OfficeHoursAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final DateTimeFormatter dtfTimeShow = DateTimeFormat.forPattern("hh:mm a");
    private static final DateTimeFormatter dtfDateShow = DateTimeFormat.forPattern("EEEE, dd MMMM yyyy");
    private static String[] dayNames = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    ArrayList<EventModel> eventsList = new ArrayList<>();
    // on item Clicking
    private OnItemClickListener onItemClickListener;

    public void setList(ArrayList<EventModel> eventsList) {
        this.eventsList = eventsList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_event, parent, false);
        return new ScheduleFragmentCopy.EventItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final EventModel item = eventsList.get(position);
        final ScheduleFragmentCopy.EventItemHolder h = (ScheduleFragmentCopy.EventItemHolder) holder;
        h.root.setOnClickListener(this);
        h.root.setTag(item);

        h.tvName.setText(item.eventDetails.event_name);
        h.tvType.setText(dtfTimeShow.print(item.eventDetails.startDateTime));
        h.tvStatus.setText(dtfTimeShow.print(item.eventDetails.endDateTime));

        if (item.eventDetails.recurring) {
            String recurringDays = "";
            for (int i = 1; i <= 7; i++) {
                if (item.eventDetails.recurringDays.contains(String.valueOf(i)))
                    recurringDays += ", " + dayNames[i];
            }
            if (!recurringDays.isEmpty()) {
                recurringDays = recurringDays.substring(2);
            }
            h.tvTime.setText(recurringDays);
        } else {
            h.tvTime.setText(dtfDateShow.print(item.eventDetails.startDateTime));
        }
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(v, (EventModel) v.getTag());
    }

    public interface OnItemClickListener {
        void onItemClick(View view, EventModel eventModel);
    }

}