package in.einfosolutions.koble.Adapters;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.desmond.ripple.RippleCompat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.einfosolutions.koble.Fragments.AllChatsFragment;
import in.einfosolutions.koble.Models.ChatSettings;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.ProfileType;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.Activities.ChatActivity.CHAT_SETTINGS;
import static in.einfosolutions.koble.utilities.App.APP_STU;

public class ChatUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    APIManager api;
    AppCompatActivity acitvity;
    private ArrayList<ProfileModel> mList = new ArrayList<>();
    // on item Clicking
    private OnItemClickListener onItemClickListener;
    private Drawable greenOnline, redOffline;

    public ChatUsersAdapter(AppCompatActivity fragment) {
        api = new APIManager();
        this.acitvity = fragment;
        greenOnline = ContextCompat.getDrawable(App.ctx, R.drawable.circle_green);
        redOffline = ContextCompat.getDrawable(App.ctx, R.drawable.circle_red);
    }

    public void setList(ArrayList<ProfileModel> mList) {
        this.mList = mList;
        for (int i = 0; i < mList.size(); i++) {
            ProfileModel item = mList.get(i);
            if (item.id.equals(App.currUser.id)) {
                mList.remove(i);
                mList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new AllChatsFragment.ProfileHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ProfileModel item = mList.get(position);
        final AllChatsFragment.ProfileHolder h = (AllChatsFragment.ProfileHolder) holder;
        h.root.setOnClickListener(this);
        h.root.setTag(item);
        RippleCompat.apply(h.root, ContextCompat.getColor(acitvity, R.color.ripple_color));

        String name = item.first_name;
        if (item.id.equals(App.currUser.id)) {
            name = "Me";
        }
        h.tvTitle.setText(name);

        h.tvDesc.setText(item.department);
        Glide.with(acitvity)
                .load(APIManager.PIC_BASE_URL + item.pic)
                .crossFade()
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(h.ivLogo);

        h.tvCount.setVisibility(View.GONE);

        // getting online offline status of user
        h.vOnlineOffline.setVisibility(View.GONE);
        if(APP_STU && item.type == ProfileType.prof) {
            h.vOnlineOffline.setVisibility(View.VISIBLE);
            DatabaseReference fcDBChatSettings = FirebaseDatabase.getInstance().getReference(CHAT_SETTINGS).child(item.id);
            fcDBChatSettings.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatSettings settings = dataSnapshot.getValue(ChatSettings.class);
                    if (settings == null) settings = new ChatSettings();
                    h.vOnlineOffline.setBackground(settings.chattable ? greenOnline : redOffline);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    h.vOnlineOffline.setBackground(greenOnline);
                }
            });
        }

        App.updateUserIntoFireBaseDB(item);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onItemClick(v, (ProfileModel) v.getTag());
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ProfileModel userInfo);
    }

}