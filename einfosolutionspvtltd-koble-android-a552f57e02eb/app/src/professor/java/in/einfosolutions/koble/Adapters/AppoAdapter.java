package in.einfosolutions.koble.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import in.einfosolutions.koble.Fragments.AppointmentsFragment;
import in.einfosolutions.koble.Models.EventModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;

public class AppoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<EventModel> mList = new ArrayList<>();

    AppointmentsFragment activity;
    APIManager api;

    DateTimeFormatter dtfDate = DateTimeFormat.forPattern("dd.MMM.yyyy");
    DateTimeFormatter dtfTime = DateTimeFormat.forPattern("hh:mm a");

    public AppoAdapter(AppointmentsFragment fragment) {
        this.activity = fragment;
        api = new APIManager();
    }

    public void setList(ArrayList<EventModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appo_item, parent, false);
        return new ViewHolderItem(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final EventModel item = mList.get(position);
        ViewHolderItem hItem = (ViewHolderItem) holder;

        Glide.with(activity)
                .load(APIManager.PIC_BASE_URL + item.taggedBy.get(0).pic)
                .crossFade()
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(hItem.ivLogo);

        String time = dtfTime.print(item.eventDetails.startDateTime)
                + " to " + dtfTime.print(item.eventDetails.endDateTime)
                + " on " + dtfDate.print(item.eventDetails.startDateTime);

        hItem.mTitle.setText(item.eventDetails.event_name);
        hItem.tvDesc.setText(item.taggedBy.get(0).first_name + "\n" + time);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolderItem extends RecyclerView.ViewHolder {

        TextView mTitle, tvDesc;
        RoundedImageView ivLogo;

        public ViewHolderItem(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDesc);
            ivLogo = (RoundedImageView) itemView.findViewById(R.id.ivLogo);
        }
    }
}