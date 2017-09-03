package in.einfosolutions.koble.Fragments;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;

import in.einfosolutions.koble.Activities.AllChatUsersActivity;
import in.einfosolutions.koble.Activities.ChatActivity;
import in.einfosolutions.koble.Activities.GroupChatActivity;
import in.einfosolutions.koble.Models.ChatSettings;
import in.einfosolutions.koble.Models.Class2;
import in.einfosolutions.koble.Models.FcmChatProps;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import in.einfosolutions.koble.utilities.RecyclerTouchListener;
import in.einfosolutions.koble.utilities.badgetextview.MaterialBadgeTextView;

import static in.einfosolutions.koble.Activities.ChatActivity.ALL_CHATS;
import static in.einfosolutions.koble.Activities.ChatActivity.CHATS;
import static in.einfosolutions.koble.Activities.ChatActivity.CHAT_SETTINGS;
import static in.einfosolutions.koble.utilities.App.APP_STU;
import static in.einfosolutions.koble.utilities.App.GROUP_DETAILS;
import static in.einfosolutions.koble.utilities.App.USER_DETAILS;
import static in.einfosolutions.koble.utilities.App.dp2px;

public class AllChatsFragment extends Fragment {

    DatabaseReference fcDBRef;
    RecyclerView recyclerView;
    LinearLayoutManager mLinearLayoutManager;
    ProgressBar progressBar;
    //private Query fcChatList;
    private FireChatListAdapter adapter;

    private Drawable greenOnline, redOffline;
    private TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        fcDBRef = FirebaseDatabase.getInstance().getReference(ALL_CHATS);
        Query fcChatList = fcDBRef.child(App.currUser.id);

        adapter = new FireChatListAdapter(
                FcmChatProps.class,
                R.layout.user_item,
                ProfileHolder.class,
                fcChatList);

        progressBar.setVisibility(View.GONE);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
            }
        });

        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(adapter);

        greenOnline = ContextCompat.getDrawable(App.ctx, R.drawable.circle_green);
        redOffline = ContextCompat.getDrawable(App.ctx, R.drawable.circle_red);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(Menu.NONE, 1, Menu.NONE, "New Chat").setIcon(App.getTintedDrawable(R.drawable.ic_msg, android.R.color.white)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startActivity(new Intent(getActivity(), AllChatUsersActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chats");
        App.checkIfClassUnscribed();
        App.refreshUserList();

        tvEmpty.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    public static class ProfileHolder extends RecyclerView.ViewHolder {

        public TextView tvTitle, tvDesc;
        public MaterialBadgeTextView tvCount;
        public RoundedImageView ivLogo;
        public View root, vOnlineOffline;

        public ProfileHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvCount = (MaterialBadgeTextView) itemView.findViewById(R.id.tvCount);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDesc);
            ivLogo = (RoundedImageView) itemView.findViewById(R.id.ivLogo);
            root = itemView.findViewById(R.id.root);
            vOnlineOffline = itemView.findViewById(R.id.vOnlineOffline);
        }
    }

    private class FireChatListAdapter extends FirebaseRecyclerAdapter<FcmChatProps, ProfileHolder> {

        public FireChatListAdapter(Class<FcmChatProps> modelClass, int modelLayout, Class<ProfileHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        public FireChatListAdapter(Class<FcmChatProps> modelClass, int modelLayout, Class<ProfileHolder> viewHolderClass, DatabaseReference ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(final ProfileHolder h, final FcmChatProps model, final int position) {
            final String key = adapter.getRef(position).getKey();

            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);

            h.tvDesc.setText("");
            h.root.setLongClickable(true);
            h.root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Warning")
                            .content("Sure you want to delete?")
                            .positiveText("Delete").onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    DatabaseReference fcChatList = fcDBRef.child(App.currUser.id).child(key);
                                    fcChatList.removeValue();
                                }
                            })
                            .negativeText("Cancel")
                            .show();
                    return true;
                }
            });

            if (model.type == ChatActivity.ChatType.SINGLE) { // for single chat
                String userId = key;
                // getting user profile details
                DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(USER_DETAILS).child(userId);
                fcDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            final ProfileModel profileModel = dataSnapshot.getValue(ProfileModel.class);
                            if (profileModel != null) {
                                h.tvTitle.setText(profileModel.first_name);
                                h.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (App.userNotVerified(getActivity())) return;
                                        Intent i2 = new Intent(getActivity(), ChatActivity.class);
                                        i2.putExtra(ChatActivity.KEY_INPUT_OTHER_USER, App.gson.toJson(profileModel));
                                        startActivity(i2);
                                    }
                                });
                                //RippleCompat.apply(h.root, ContextCompat.getColor(getActivity(), R.color.ripple_color));
                                Glide.with(AllChatsFragment.this)
                                        .load(APIManager.PIC_BASE_URL + profileModel.pic)
                                        .crossFade()
                                        .centerCrop()
                                        .dontAnimate()
                                        .placeholder(R.drawable.ic_user_placeholder)
                                        .into(h.ivLogo);
                                h.root.setEnabled(true);
                            } else {
                                h.tvTitle.setText("User");
                                h.root.setEnabled(false);
                            }
                        } catch (Exception e) {
                            h.tvTitle.setText("User");
                            h.root.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // getting online offline status of user
                h.vOnlineOffline.setVisibility(APP_STU ? View.VISIBLE : View.GONE);
                DatabaseReference fcDBChatSettings = FirebaseDatabase.getInstance().getReference(CHAT_SETTINGS).child(userId);
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

            } else { // for group chat
                h.tvTitle.setText(model.name);
                h.vOnlineOffline.setVisibility(View.GONE);
                // getting group details
                final String groupCodeKey = key;
                DatabaseReference fcDBRef = FirebaseDatabase.getInstance().getReference(GROUP_DETAILS).child(groupCodeKey);
                fcDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            final Class2 class2 = dataSnapshot.getValue(Class2.class);
                            if (class2 != null) {
                                h.tvTitle.setText(class2.group_name);
                                h.root.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (App.userNotVerified(getActivity())) return;
                                        Intent i = new Intent(getActivity(), GroupChatActivity.class);
                                        i.putExtra(GroupChatActivity.KEY_INPUT_CLASS, App.gson.toJson(class2));
                                        startActivity(i);
                                    }
                                });
                                h.root.setEnabled(true);
                            } else {
                                h.tvTitle.setText(model.name);
                                h.root.setEnabled(false);
                            }
                        } catch (Exception e) {
                            h.tvTitle.setText(model.name);
                            h.root.setEnabled(false);
                        }

                        // create color drawable using group name
                        ColorGenerator generator = ColorGenerator.MATERIAL;
                        int color2 = generator.getColor(groupCodeKey);
                        TextDrawable drawable1 = TextDrawable.builder()
                                .beginConfig()
                                .textColor(Color.WHITE)
                                .width(dp2px(60))
                                .height(dp2px(60))
                                .toUpperCase()
                                .endConfig()
                                .buildRect(String.valueOf(model.name.charAt(0)), color2);
                        //.buildRoundRect(String.valueOf(model.name.charAt(0)), color2, dp2px(60)); // radius in px
                        h.ivLogo.setImageDrawable(drawable1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            // setting chat count
            h.tvCount.setBadgeCount((int) model.new_count, true);

            // setting last chat text
            Query fcDBRef = adapter.getRef(position).child(CHATS).limitToLast(1);
            fcDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot each : dataSnapshot.getChildren()) {
                        HashMap<String, Object> chats = (HashMap<String, Object>) each.getValue();
                        h.tvDesc.setText((String) chats.get("message"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    h.tvDesc.setText("");
                }
            });
        }
    }

}
