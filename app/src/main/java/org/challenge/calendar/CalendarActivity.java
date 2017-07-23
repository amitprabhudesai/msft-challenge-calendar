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

import dagger.android.AndroidInjection;

public class CalendarActivity extends AppCompatActivity implements
        CalendarFragment.DateSelectionChangedListener {

    private static final String TAG = CalendarActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use an intent to insert a new event
                // This way we don't need to hold (or check) the WRITE_CALENDAR permission
                Intent intent = new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);
                startActivity(intent);
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
