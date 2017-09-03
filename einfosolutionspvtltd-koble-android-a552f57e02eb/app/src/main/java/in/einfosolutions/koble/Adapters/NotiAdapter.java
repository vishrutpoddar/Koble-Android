package in.einfosolutions.koble.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.einfosolutions.koble.Fragments.ScheduleFragmentCopy;
import in.einfosolutions.koble.Models.EventType;
import in.einfosolutions.koble.Models.NotiModel;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

/**
 * Created by joker on 5/10/17.
 */

public class NotiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    ArrayList<NotiModel> notiList = new ArrayList<>();
    // on item Clicking
    private OnItemClickListener onItemClickListener;

    public void setList(ArrayList<NotiModel> eventsList) {
        this.notiList = eventsList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_event, parent, false);
        return new ScheduleFragmentCopy.EventItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final NotiModel item = notiList.get(position);
        final ScheduleFragmentCopy.EventItemHolder h = (ScheduleFragmentCopy.EventItemHolder) holder;
        h.root.setOnClickListener(this);
        h.root.setTag(item);

        if (App.APP_STU) {
            h.tvType.setVisibility(item.type == EventType.APPO ? View.INVISIBLE : View.VISIBLE);
        } else {
            h.tvType.setVisibility(item.type == EventType.CLASS ? View.INVISIBLE : View.VISIBLE);
        }

        h.tvName.setText(item.message);
        h.tvTime.setText(item.type.stringVal());
        h.tvType.setText(item.date);
        h.tvStatus.setText(item.title);

    }

    @Override
    public int getItemCount() {
        return notiList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(v, (NotiModel) v.getTag());
    }

    public interface OnItemClickListener {
        void onItemClick(View view, NotiModel eventModel);
    }
}
