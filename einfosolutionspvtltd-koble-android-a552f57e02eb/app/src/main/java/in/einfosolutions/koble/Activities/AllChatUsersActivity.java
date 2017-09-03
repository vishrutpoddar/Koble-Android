package in.einfosolutions.koble.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import eu.inloop.localmessagemanager.LocalMessage;
import eu.inloop.localmessagemanager.LocalMessageCallback;
import eu.inloop.localmessagemanager.LocalMessageManager;
import in.einfosolutions.koble.Adapters.ChatUsersAdapter;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.UserListRes;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

import static in.einfosolutions.koble.utilities.App.APP_STU;

/**
 * Created by joker on 5/22/17.
 */

public class AllChatUsersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, ChatUsersAdapter.OnItemClickListener, LocalMessageCallback {

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private ChatUsersAdapter usersAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chat_users);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (APP_STU) {
            getSupportActionBar().setTitle("Select Professor");
            tvEmpty.setText("No Professors Available");
        } else {
            getSupportActionBar().setTitle("Select Student");
            tvEmpty.setText("No Students Available");
        }

        swipeRefreshLayout.setOnRefreshListener(this);

        usersAdapter = new ChatUsersAdapter(this);
        recyclerView.setAdapter(usersAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        usersAdapter.setOnItemClickListener(this);
        recyclerView.setHasFixedSize(true);

    }

    @Override
    public void onRefresh() {
        getChatUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChatUsers();
        LocalMessageManager.getInstance().addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalMessageManager.getInstance().removeListener(this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        progressBar.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        if (localMessage.getId() == App.OBSERVE_USER_LIST_REFRESH) {
            if (localMessage.getObject() != null && localMessage.getObject() instanceof UserListRes) {
                UserListRes userListRes = ((UserListRes) localMessage.getObject());
                usersAdapter.setList(userListRes.userList);
                if (userListRes.userList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }
        } else if (localMessage.getId() == App.OBSERVE_NO_INTERNET) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View view, ProfileModel userInfo) {
        if (App.userNotVerified(this)) return;
        Intent i2 = new Intent(this, ChatActivity.class);
        i2.putExtra(ChatActivity.KEY_INPUT_OTHER_USER, App.gson.toJson(userInfo));
        finish();
        startActivity(i2);
    }

    void getChatUsers() {
        progressBar.setVisibility(View.VISIBLE);
        App.refreshUserList();
    }

}
