package org.challenge.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        MainActivityFragment.DateSelectionChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private MainActivityFragment mMainActivityFragment;

    private final static int REQUEST_ADD_CALENDAR_EVENT = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_ADD_CALENDAR_EVENT == requestCode) {
            mMainActivityFragment.refresh();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mMainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use an intent to insert a new event
                // This way we don't need to hold (or check) the WRITE_CALENDAR permission
                Intent intent = new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);
                startActivityForResult(intent, REQUEST_ADD_CALENDAR_EVENT);
            }
        });
    }

    @Override
    public void onDateSelected(Date newDate) {
        DateFormat monthNameFormat =
                new SimpleDateFormat(getString(R.string.month_name_format), Locale.US);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(monthNameFormat.format(newDate));
        }
    }
}
