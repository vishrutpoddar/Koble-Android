package in.einfosolutions.koble.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import in.einfosolutions.koble.Activities.BaseActivity;
import in.einfosolutions.koble.Activities.ClassMembersActivity;
import in.einfosolutions.koble.Adapters.ClassAdapter;
import in.einfosolutions.koble.Models.Class1;
import in.einfosolutions.koble.Models.ClassResProf;
import in.einfosolutions.koble.Models.EventInfo;
import in.einfosolutions.koble.Models.Response1;
import in.einfosolutions.koble.Network.APIManager;
import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;
import in.einfosolutions.koble.utilities.RecyclerTouchListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassesFragment extends Fragment {

    APIManager api = new APIManager();
    private ClassAdapter classAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<Class1> classList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_classes, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        classAdapter = new ClassAdapter(this);
        recyclerView.setAdapter(classAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        classAdapter.setList(classList);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent i = new Intent(getActivity(), ClassMembersActivity.class);
                i.putExtra(ClassMembersActivity.KEY_INPUT_CLASS_CODE, classList.get(position).class_details.code);
                startActivity(i);
            }

            @Override
            public void onLongClick(View view, final int position) {
                new MaterialDialog.Builder(getActivity())
                        .title("Delete Class")
                        .content("Sure you want to delete class?")
                        .negativeText("No")
                        .positiveText("YES")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Class1 class1 = classList.get(position);
                                deleteClassApiCall(class1.event_details.event_slug);
                            }
                        }).show();
            }
        }));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Classes");
        getAllClasses();
    }

    public void getAllClasses() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        api.service.get_all_classes_prof("get_all_classes", App.currUser.id).enqueue(new Callback<ClassResProf>() {
            @Override
            public void onResponse(Call<ClassResProf> call, Response<ClassResProf> response) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                ClassResProf model = response.body();

                int l = model.array.size();
                for (int i = 0; i < l; i++) {
                    EventInfo eventDetail = model.array.get(i).event_details;
                    eventDetail.startDateTime = App.dtfDateTime.parseDateTime(eventDetail.start + " " + eventDetail.start_time);
                    eventDetail.endDateTime = App.dtfDateTime.parseDateTime(eventDetail.start + " " + eventDetail.end_time);
                    eventDetail.recurringEnd = App.dtfDateTime.parseDateTime(eventDetail.end + " " + eventDetail.end_time);
                }

                classList = model.array;
                classAdapter.setList(classList);

                if (classList.size() == 0) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ClassResProf> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void deleteClassApiCall(String eventSlug) {
        final MaterialDialog progressDialog = ((BaseActivity) getActivity()).progressDialog;
        progressDialog.show();
        new APIManager().service.profDeleteClass("delete_event", eventSlug, App.currUser.id).enqueue(new Callback<Response1>() {
            @Override
            public void onResponse(Call<Response1> call, Response<Response1> response) {
                progressDialog.hide();
                if (response.isSuccessful() && response.body().status.equals("0")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Class Deleted!", Toast.LENGTH_SHORT).show();
                    getAllClasses();
                }
                else
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.try_again), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Response1> call, Throwable t) {
                progressDialog.hide();
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
