package in.einfosolutions.koble.Activities;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.annotations.SerializedName;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;

import in.einfosolutions.koble.Models.ChatSettings;
import in.einfosolutions.koble.Models.MsgOne;
import in.einfosolutions.koble.Models.MsgStatus;
import in.einfosolutions.koble.Models.NotiType;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.Push.FirebaseNoti;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.utilities.App.dp2px;

/**
 * Created by joker on 1/16/17.
 */

public class ChatActivity extends ChatBaseActivity {

    public static final String KEY_INPUT_OTHER_USER = "KEY_INPUT_OTHER_USER";
    public static final String ALL_CHATS = "ALL_CHATS";
    public static final String CHATS = "CHATS";
    public static final String CHAT_SETTINGS = "CHAT_SETTINGS";
    private static final String CHAT_TYPE = "type";
    private static final DateTimeFormatter dtfDateTimeShow = DateTimeFormat.forPattern("dd MMM HH:mm");
    private static final DateTimeFormatter dtfTimeShow = DateTimeFormat.forPattern("HH:mm");

    ProfileModel otherUser;
    private FireOneChatAdapter adapter;
    private DatabaseReference fireMyNode;
    private DatabaseReference fireElseNode;
    private DatabaseReference fireElseChatSettings;
    private ValueEventListener onlineOfflineListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        otherUser = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_OTHER_USER), ProfileModel.class);

        getSupportActionBar().setTitle(otherUser.first_name);

        fcDBRef = FirebaseDatabase.getInstance().getReference(ALL_CHATS);
        fireMyNode = fcDBRef.child(App.currUser.id).child(otherUser.id);
        fireElseNode = fcDBRef.child(otherUser.id).child(App.currUser.id);
        fireElseChatSettings = FirebaseDatabase.getInstance().getReference(CHAT_SETTINGS).child(otherUser.id);

        fireMyNode.keepSynced(true);
        fireElseNode.keepSynced(true);

        App.updateUserIntoFireBaseDB(App.currUser);
        App.updateUserIntoFireBaseDB(otherUser);

        adapter = new FireOneChatAdapter(
                MsgOne.class,
                R.layout.item_msg_chat,
                MsgOneViewHolder.class,
                fireMyNode.child(CHATS));

        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(etMsg.getText().toString().trim());
            }
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setAdapter(adapter);

        if (App.APP_STU) {
            // use chat settings
            etMsg.setEnabled(false);
            bSend.setEnabled(false);

            onlineOfflineListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatSettings chatSettings = dataSnapshot.getValue(ChatSettings.class);
                    if (chatSettings == null) chatSettings = new ChatSettings();
                    etMsg.setEnabled(chatSettings.chattable);
                    bSend.setEnabled(chatSettings.chattable);
                    if (chatSettings.chattable) tvOffline.setVisibility(View.GONE);
                    else tvOffline.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    etMsg.setEnabled(true);
                    bSend.setEnabled(true);
                    tvOffline.setVisibility(View.GONE);
                }
            };
            fireElseChatSettings.addValueEventListener(onlineOfflineListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            fireElseChatSettings.removeEventListener(onlineOfflineListener);
        } catch (Exception e) {}
    }

    void sendMessage(final String msg) {

        final HashMap<String, Object> chatProps = new HashMap<>();
        //chatProps.put("last_seen", ServerValue.TIMESTAMP);
        chatProps.put(CHAT_TYPE, ChatType.SINGLE);

        // updating chat count and type
        fireMyNode.updateChildren(chatProps);
        fireElseNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long newCount = 0;
                try {
                    newCount = (long) dataSnapshot.child(NEW_COUNT).getValue();
                    Log.e("newcount", "new count " + newCount);
                } catch (Exception e) {
                    Log.e("newcount", "new count " + newCount + "    --" + e.getMessage());
                }
                newCount++;
                chatProps.put(NEW_COUNT, newCount);
                fireElseNode.updateChildren(chatProps);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        MsgOne msgModel = new MsgOne(App.currUser.id, otherUser.id, msg, DateTime.now().getMillis());
        fireElseNode.child(CHATS).push().setValue(msgModel);
        msgModel.status = MsgStatus.READ;
        fireMyNode.child(CHATS).push().setValue(msgModel);

        // send notification
        FirebaseNoti firebaseNoti = new FirebaseNoti(otherUser.token, App.currUser.first_name, msg, NotiType.SINGLE.string(), App.currUser.id);
        if (!firebaseNoti.to.equals("")) {
            APIManager.sendPushNotificatonToFirebaseServer(firebaseNoti);
            Log.e("SendPush", "success : " + App.gson.toJson(firebaseNoti));
        } else
            Log.e("SendPush", "No token available for id:" + otherUser.id);

        etMsg.setText("");
    }

    public static enum ChatType {
        @SerializedName("single")SINGLE,
        @SerializedName("group")GROUP;
    }

    public static class MsgOneViewHolder extends RecyclerView.ViewHolder {

        public TextView tvMsg;
        public TextView tvTime;
        public RoundedImageView ivLogo;
        public LinearLayout llContainer;
        public CardView cardView;

        public MsgOneViewHolder(View itemView) {
            super(itemView);
            tvMsg = (TextView) itemView.findViewById(R.id.tvMsg);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            ivLogo = (RoundedImageView) itemView.findViewById(R.id.ivLogo);
            llContainer = (LinearLayout) itemView.findViewById(R.id.llContainer);
            cardView = (CardView) itemView.findViewById(R.id.root);
        }
    }

    private class FireOneChatAdapter extends FirebaseRecyclerAdapter<MsgOne, MsgOneViewHolder> {

        public FireOneChatAdapter(Class<MsgOne> modelClass, int modelLayout, Class<MsgOneViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        public FireOneChatAdapter(Class<MsgOne> modelClass, int modelLayout, Class<MsgOneViewHolder> viewHolderClass, DatabaseReference ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(MsgOneViewHolder h, MsgOne model, final int position) {
            if (model.status == MsgStatus.UNREAD) {
                model.status = MsgStatus.READ;
                adapter.getRef(position).setValue(model);
                final HashMap<String, Object> chatProps = new HashMap<>();
                chatProps.put(NEW_COUNT, 0);
                fireMyNode.updateChildren(chatProps);
            }

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            h.ivLogo.setVisibility(View.GONE);

            h.tvMsg.setText(model.message);

            // setting time
            String time;
            DateTime dateTime = new DateTime(model.timestamp);
            if (DateTimeComparator.getDateOnlyInstance().compare(dateTime, DateTime.now()) == 0)
                time = dtfTimeShow.print(dateTime);
            else
                time = dtfDateTimeShow.print(dateTime);
            h.tvTime.setText(time);

            if (model.senderId.equals(App.currUser.id)) { // sent by me
                h.llContainer.setPadding(dp2px(50), dp2px(4), dp2px(4), dp2px(4));
                h.llContainer.setGravity(Gravity.RIGHT);
            } else {
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) h.tvMsg.getLayoutParams();
                p.gravity = Gravity.LEFT;
                h.tvMsg.setLayoutParams(p);

                h.llContainer.setPadding(dp2px(4), dp2px(4), dp2px(50), dp2px(4));
                h.llContainer.setGravity(Gravity.LEFT);
            }

            h.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(ChatActivity.this)
                            .setMessage("Delete this chat?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    adapter.getRef(position).removeValue();
                                }
                            })
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                    return false;
                }
            });
        }
    }
}
