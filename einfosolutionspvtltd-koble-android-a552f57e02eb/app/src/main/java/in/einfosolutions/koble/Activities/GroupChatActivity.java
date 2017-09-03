package in.einfosolutions.koble.Activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;

import in.einfosolutions.koble.Models.Class2;
import in.einfosolutions.koble.Models.Member;
import in.einfosolutions.koble.Models.MsgGroup;
import in.einfosolutions.koble.Models.MsgStatus;
import in.einfosolutions.koble.Models.NotiType;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.ProfileType;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.Push.FirebaseNoti;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.Activities.ChatActivity.ALL_CHATS;
import static in.einfosolutions.koble.Activities.ChatActivity.CHATS;
import static in.einfosolutions.koble.utilities.App.dp2px;

/**
 * Created by joker on 1/16/17.
 */

/**
 * go to firebase console -> db for json structure
 */
public class GroupChatActivity extends ChatBaseActivity {

    public static final String KEY_INPUT_CLASS = "KEY_INPUT_CLASS";
    private static final DateTimeFormatter dtfDateTimeShow = DateTimeFormat.forPattern("dd MMM HH:mm");
    private static final DateTimeFormatter dtfTimeShow = DateTimeFormat.forPattern("HH:mm");
    Class2 inputClass;

    private FireGroupChatAdapter adapter;
    private DatabaseReference fcMyGroupNode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputClass = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_CLASS), Class2.class);
        Log.e("class", getIntent().getStringExtra(KEY_INPUT_CLASS));

        getSupportActionBar().setTitle(inputClass.group_name);

        fcDBRef = FirebaseDatabase.getInstance().getReference(ALL_CHATS);
        fcDBRef.keepSynced(true);

        fcMyGroupNode = fcDBRef.child(App.currUser.id).child(inputClass.group_code);

        adapter = new FireGroupChatAdapter(
                MsgGroup.class,
                R.layout.item_group_msg_chat,
                MsgGroupViewHolder.class,
                fcMyGroupNode.child(CHATS));

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_group_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                Intent i = new Intent(this, ClassMembersActivity.class);
                i.putExtra(ClassMembersActivity.KEY_INPUT_CLASS, App.gson.toJson(inputClass));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void sendMessage(String msg) {
        final HashMap<String, Object> chatProps = new HashMap<>();
        //chatProps.put("last_seen", ServerValue.TIMESTAMP);
        chatProps.put("type", ChatActivity.ChatType.GROUP);
        chatProps.put("name", inputClass.group_name);

        MsgGroup msgModel = new MsgGroup(App.currUser.id, msg, DateTime.now().getMillis());

        for (Member each : inputClass.members) {
            ProfileModel p = each.members;
            final DatabaseReference fcEachGroupNode = fcDBRef.child(p.id).child(inputClass.group_code);

            if (p.type == ProfileType.prof) continue;

            if (p.id.equals(App.currUser.id)) {
                msgModel.status = MsgStatus.READ;
                fcEachGroupNode.updateChildren(chatProps);
            } else {
                msgModel.status = MsgStatus.UNREAD;

                // update new count for every member
                fcEachGroupNode.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long newCount = 0;
                        try {
                            newCount = (long) dataSnapshot.child(NEW_COUNT).getValue();
                        } catch (Exception e) {
                        }
                        newCount++;
                        chatProps.put(NEW_COUNT, newCount);
                        fcEachGroupNode.updateChildren(chatProps);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // send notification
                FirebaseNoti firebaseNoti = new FirebaseNoti(p.token, inputClass.group_name, App.currUser.first_name + ": " + msg, NotiType.GROUP.string(), inputClass.group_code);
                if (!firebaseNoti.to.equals("")) {
                    APIManager.sendPushNotificatonToFirebaseServer(firebaseNoti);
                    Log.e("SendPush", "success : " + App.gson.toJson(firebaseNoti));
                } else
                    Log.e("SendPush", "No token available for id:" + p.id);
                Log.e("SendPush", "success : " + App.gson.toJson(firebaseNoti));
            }

            fcEachGroupNode.child(CHATS).push().setValue(msgModel);
        }

        etMsg.setText("");
    }

    // get profile model using id, to show the pic
    ProfileModel getProfileModel(String id) {
        for (Member each : inputClass.members) {
            if (each.members.id.equals(id))
                return each.members;
        }
        return null;
    }

    public static class MsgGroupViewHolder extends RecyclerView.ViewHolder {

        public TextView tvMsg, tvName;
        public TextView tvTime;
        public RoundedImageView ivLogo;
        public LinearLayout llContainer;
        public CardView cardView;

        public MsgGroupViewHolder(View itemView) {
            super(itemView);
            tvMsg = (TextView) itemView.findViewById(R.id.tvMsg);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            ivLogo = (RoundedImageView) itemView.findViewById(R.id.ivLogo);
            llContainer = (LinearLayout) itemView.findViewById(R.id.llContainer);
            cardView = (CardView) itemView.findViewById(R.id.root);
        }
    }

    private class FireGroupChatAdapter extends FirebaseRecyclerAdapter<MsgGroup, MsgGroupViewHolder> {

        public FireGroupChatAdapter(Class<MsgGroup> modelClass, int modelLayout, Class<MsgGroupViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        public FireGroupChatAdapter(Class<MsgGroup> modelClass, int modelLayout, Class<MsgGroupViewHolder> viewHolderClass, DatabaseReference ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(MsgGroupViewHolder h, MsgGroup model, final int position) {
            if (model.status == MsgStatus.UNREAD) {
                model.status = MsgStatus.READ;
                adapter.getRef(position).setValue(model);
                final HashMap<String, Object> chatProps = new HashMap<>();
                chatProps.put(NEW_COUNT, 0);
                fcMyGroupNode.updateChildren(chatProps);
            }

            progressBar.setVisibility(ProgressBar.INVISIBLE);

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
                h.ivLogo.setVisibility(View.GONE);
                h.tvName.setVisibility(View.GONE);
            } else {
                h.tvName.setVisibility(View.VISIBLE);
                h.llContainer.setPadding(dp2px(4), dp2px(4), dp2px(50), dp2px(4));
                h.llContainer.setGravity(Gravity.LEFT);

                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) h.tvMsg.getLayoutParams();
                p.gravity = Gravity.LEFT;
                h.tvMsg.setLayoutParams(p);

                h.ivLogo.setVisibility(View.VISIBLE);
                final ProfileModel profile = getProfileModel(model.senderId);
                if (profile != null) {
                    h.tvName.setText(profile.first_name);
                    Glide.with(GroupChatActivity.this)
                            .load(APIManager.PIC_BASE_URL + profile.pic)
                            .crossFade()
                            .centerCrop()
                            .dontAnimate()
                            .placeholder(R.drawable.ic_user_placeholder)
                            .into(h.ivLogo);
                }

                h.ivLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(GroupChatActivity.this, OtherUserProfileActivity.class);
                        i.putExtra(OtherUserProfileActivity.KEY_INPUT_PROFILE, App.gson.toJson(profile));
                        i.putExtra(OtherUserProfileActivity.KEY_SHOW_CHAT_MENU, false);
                        i.putExtra(OtherUserProfileActivity.KEY_SHOW_APPO_MENU, false);
                        startActivity(i);
                    }
                });
                int prevId = position - 1;
                if (prevId != -1) {
                    MsgGroup item = adapter.getItem(prevId);
                    if (item.senderId.equals(model.senderId)) {
                        h.ivLogo.setVisibility(View.GONE);
                        h.tvName.setVisibility(View.GONE);
                    } else {
                        h.ivLogo.setVisibility(View.VISIBLE);
                        h.tvName.setVisibility(View.VISIBLE);
                    }
                }
            }

            h.ivLogo.setVisibility(View.GONE);
            h.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(GroupChatActivity.this)
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
