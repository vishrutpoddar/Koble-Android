package in.einfosolutions.koble.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import eu.inloop.localmessagemanager.LocalMessage;
import eu.inloop.localmessagemanager.LocalMessageCallback;
import eu.inloop.localmessagemanager.LocalMessageManager;
import in.einfosolutions.koble.Activities.OtherUserProfileActivity;
import in.einfosolutions.koble.Adapters.OtherUsersAdapter;
import in.einfosolutions.koble.Models.ProfileModel;
import in.einfosolutions.koble.Models.UserListRes;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

public class ProfessorsFragment extends Fragment implements OtherUsersAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, LocalMessageCallback {

    private OtherUsersAdapter usersAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<ProfileModel> userList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_professors, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this);

        usersAdapter = new OtherUsersAdapter((AppCompatActivity) getActivity());
        recyclerView.setAdapter(usersAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        usersAdapter.setOnItemClickListener(this);
        recyclerView.setHasFixedSize(true);

        usersAdapter.setList(userList);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Professors");
        LocalMessageManager.getInstance().addListener(this);
        getFacultyList();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalMessageManager.getInstance().removeListener(this);
    }

    @Override
    public void onItemClick(View view, ProfileModel userInfo) {
        Intent i = new Intent(getActivity(), OtherUserProfileActivity.class);
        i.putExtra(OtherUserProfileActivity.KEY_INPUT_PROFILE, App.gson.toJson(userInfo));
        i.putExtra(OtherUserProfileActivity.KEY_SHOW_APPO_MENU, true);
        i.putExtra(OtherUserProfileActivity.KEY_SHOW_CHAT_MENU, true);
        startActivity(i);
    }

    public void getFacultyList() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        App.refreshUserList();

        /*new APIManager().service.getAllFaculty("get_all_teachers", App.currentUser.id, App.currentUser.university, App.currentUser.department).enqueue(new Callback<UserListRes>() {
            @Override
            public void onResponse(Call<UserListRes> call, Response<UserListRes> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                userList = response.body().userList;
                usersAdapter.setList(userList);
                if (userList.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<UserListRes> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);
            }
        });*/
    }

    @Override
    public void onRefresh() {
        getFacultyList();
    }

    @Override
    public void handleMessage(@NonNull LocalMessage localMessage) {
        if (localMessage.getId() == App.OBSERVE_USER_LIST_REFRESH) {
            if (localMessage.getObject() != null && localMessage.getObject() instanceof UserListRes) {
                UserListRes userListRes = ((UserListRes) localMessage.getObject());
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                userList = userListRes.userList;
                usersAdapter.setList(userList);
                if (userList.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            } else { // no internet
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.INVISIBLE);
            }
        } else if (localMessage.getId() == App.OBSERVE_NO_INTERNET) {
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }
}
