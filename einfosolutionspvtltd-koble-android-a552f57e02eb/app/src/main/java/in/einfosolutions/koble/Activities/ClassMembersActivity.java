package in.einfosolutions.koble.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import in.einfosolutions.koble.Adapters.OtherUsersAdapter;
import in.einfosolutions.koble.Models.Class2;
import in.einfosolutions.koble.Models.ClassMembersRes;
import in.einfosolutions.koble.Models.Member;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.ProfileType;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by joker on 1/11/17.
 */

public class ClassMembersActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, OtherUsersAdapter.OnItemClickListener {

    public static final String KEY_INPUT_CLASS_CODE = "KEY_INPUT_CLASS_CODE"; // for professor app
    public static final String KEY_INPUT_CLASS = "KEY_INPUT_CLASS"; // for student app
    public static final String KEY_TITLE = "KEY_TITLE";

    private OtherUsersAdapter usersAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<ProfileModel> userList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbar;
    private String classCode;
    private Class2 classModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Members");

        if (getIntent().hasExtra(KEY_TITLE))
            getSupportActionBar().setTitle(getIntent().getStringExtra(KEY_TITLE));

        // for professor app
        classCode = getIntent().getStringExtra(KEY_INPUT_CLASS_CODE);

        // for student app
        classModel = App.gson.fromJson(getIntent().getStringExtra(KEY_INPUT_CLASS), Class2.class);
        userList = new ArrayList<>();
        if (classModel != null)
            for (Member each : classModel.members) {
                userList.add(each.members);
            }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this);

        usersAdapter = new OtherUsersAdapter(this);
        recyclerView.setAdapter(usersAdapter);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        usersAdapter.setOnItemClickListener(this);
        recyclerView.setHasFixedSize(true);

        usersAdapter.setList(userList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (App.APP_PRO)
            getMembersOfClass();
    }

    public void getMembersOfClass() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        new APIManager().service.get_class_members("get_class_members", classCode).enqueue(new Callback<ClassMembersRes>() {
            @Override
            public void onResponse(Call<ClassMembersRes> call, Response<ClassMembersRes> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                userList = response.body().array;
                usersAdapter.setList(userList);
                if (userList.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ClassMembersRes> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onRefresh() {
        if (App.APP_PRO)
            getMembersOfClass();
    }

    @Override
    public void onItemClick(View view, ProfileModel userInfo) {
        if(userInfo.id.equals(App.currUser.id)) return;

        Intent i = new Intent(this, OtherUserProfileActivity.class);
        i.putExtra(OtherUserProfileActivity.KEY_INPUT_PROFILE, App.gson.toJson(userInfo));

        boolean showChat = false;
        boolean showAppo = false;

        if (App.APP_STU) {
            if (userInfo.type == ProfileType.prof) {
                showChat = showAppo = true;
            }
        } else {
            showChat = true;
            showAppo = false;
        }

        i.putExtra(OtherUserProfileActivity.KEY_SHOW_CHAT_MENU, showChat);
        i.putExtra(OtherUserProfileActivity.KEY_SHOW_APPO_MENU, showAppo);

        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
