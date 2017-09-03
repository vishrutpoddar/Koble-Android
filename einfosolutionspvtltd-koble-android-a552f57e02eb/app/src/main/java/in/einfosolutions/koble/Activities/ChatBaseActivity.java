package in.einfosolutions.koble.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.desmond.ripple.RippleCompat;
import com.google.firebase.database.DatabaseReference;

import in.einfosolutions.koble.R;
import in.einfosolutions.koble.utilities.App;

/**
 * Created by joker on 1/16/17.
 */
public class ChatBaseActivity extends BaseActivity {

    static final String NEW_COUNT = "new_count";

    protected DatabaseReference fcDBRef;
    protected RecyclerView recyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    Toolbar toolbar;
    EditText etMsg;
    ImageView bSend;
    ProgressBar progressBar;
    AppCompatTextView tvOffline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Chats");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        etMsg = (EditText) findViewById(R.id.etMsg);
        tvOffline = (AppCompatTextView) findViewById(R.id.tvOffline);
        bSend = (ImageView) findViewById(R.id.ivSend);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RippleCompat.apply(bSend, ContextCompat.getColor(getApplicationContext(), R.color.ripple_color));

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLinearLayoutManager);

        bSend.setEnabled(false);
        bSend.setBackgroundResource(R.drawable.ic_send_disable);

        etMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    bSend.setEnabled(true);
                    bSend.setBackgroundResource(R.drawable.ic_send_eanble);
                } else {
                    bSend.setEnabled(false);
                    bSend.setBackgroundResource(R.drawable.ic_send_disable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        if (App.isNetworkAvailable())
            progressBar.setVisibility(View.GONE);
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
