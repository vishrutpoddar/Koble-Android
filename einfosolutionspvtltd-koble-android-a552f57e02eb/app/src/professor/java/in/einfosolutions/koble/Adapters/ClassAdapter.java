package in.einfosolutions.koble.Adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import in.einfosolutions.koble.Fragments.ClassesFragment;
import in.einfosolutions.koble.Models.Class1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;

import static in.einfosolutions.koble.utilities.App.dp2px;

/**
 * Created by joker on 1/11/17.
 */
public class ClassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ArrayList<Class1> mList = new ArrayList<>();

    ClassesFragment activity;
    APIManager api;

    DateTimeFormatter dtfDate = DateTimeFormat.forPattern("E, dd M yyyy");
    //DateTimeFormatter dtfTime = DateTimeFormat.forPattern("hh:mm a");

    public ClassAdapter(ClassesFragment fragment) {
        this.activity = fragment;
        api = new APIManager();
    }

    public void setList(ArrayList<Class1> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item, parent, false);
        return new ViewHolderItem(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Class1 item = mList.get(position);
        ViewHolderItem hItem = (ViewHolderItem) holder;

        //hItem.root.setOnClickListener(this);
        //hItem.root.setTag(item);

        //RippleCompat.apply(hItem.cardView, App.COLOR_RIPPLE);

        hItem.mTitle.setText(item.event_details.event_name);
        hItem.tvDesc.setText(dtfDate.print(item.event_details.startDateTime));
        hItem.tvDesc2.setText("CODE: " + item.class_details.code.toUpperCase());
        hItem.tvDesc3.setVisibility(View.GONE);

        // create color drawable using group name
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color2 = generator.getColor(item.class_details.code);
        TextDrawable drawable1 = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .width(dp2px(60))
                .height(dp2px(60))
                .toUpperCase()
                .endConfig()
                .buildRect(String.valueOf(item.class_details.title.charAt(0)), color2);
        //.buildRoundRect(String.valueOf(model.name.charAt(0)), color2, dp2px(60)); // radius in px
        hItem.ivLogo.setImageDrawable(drawable1);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ViewHolderItem extends RecyclerView.ViewHolder {

        TextView mTitle, tvDesc, tvDesc2, tvDesc3;
        RoundedImageView ivLogo;

        public ViewHolderItem(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDesc);
            tvDesc2 = (TextView) itemView.findViewById(R.id.tvDesc2);
            tvDesc3 = (TextView) itemView.findViewById(R.id.tvDesc3);
            ivLogo = (RoundedImageView) itemView.findViewById(R.id.ivLogo);
        }
    }

}
