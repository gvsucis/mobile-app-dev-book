package edu.gvsu.cis.traxy;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JournalViewActivity extends AppCompatActivity {

    @BindView(R.id.journal_name) TextView title;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_view);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        Intent i = this.getIntent();
        String name = i.getStringExtra("TRIP_NAME");
        title.setText(name);

    }

    @OnClick(R.id.fab)
    public void fab_tapped()
    {
        Snackbar.make(title, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
