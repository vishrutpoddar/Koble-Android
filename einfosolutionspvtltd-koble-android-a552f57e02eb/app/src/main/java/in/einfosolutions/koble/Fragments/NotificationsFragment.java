package in.einfosolutions.koble.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import in.einfosolutions.koble.Activities.BaseActivity;
import in.einfosolutions.koble.Adapters.NotiAdapter;
import in.einfosolutions.koble.Models.NotiModel;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private NotiAdapter notiAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<NotiModel> notiList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);

        notiAdapter = new NotiAdapter();
        recyclerView.setAdapter(notiAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        notiAdapter.setList(notiList);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notifications");
        getNotfictions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_notification, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDismiss:
                dismissAllNoti();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void getNotfictions() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        new APIManager().service.getNotifications("notification", App.currUser.id).enqueue(new Callback<NotiModel.NotiRes>() {
            @Override
            public void onResponse(Call<NotiModel.NotiRes> call, Response<NotiModel.NotiRes> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body().notiList == null)
                    notiList.clear();
                else
                    notiList = response.body().notiList;

                notiAdapter.setList(notiList);
                if (notiList.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<NotiModel.NotiRes> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void dismissAllNoti() {
        final MaterialDialog progressDialog = ((BaseActivity) getActivity()).progressDialog;
        progressDialog.show();
        new APIManager().service.dismissNotifications("notification_remove", App.currUser.id).enqueue(new Callback<Response1>() {
            @Override
            public void onResponse(Call<Response1> call, Response<Response1> response) {
                progressDialog.dismiss();
                getNotfictions();
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.notification_cleared), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Response1> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

}