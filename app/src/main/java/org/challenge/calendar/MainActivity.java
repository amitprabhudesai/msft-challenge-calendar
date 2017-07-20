package org.challenge.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Listener to notify the result of using an intent to insert
     * a new calendar event
     */
    public interface CalendarIntentResultListener {
        /**
         * Called to signal that the request was completed.
         * <p><strong>Note</strong> The calendar app always returns
         * with a RESULT_CANCELED in the Activity result callback, so
         * this method is called without checking the result code.
         * Implementations would typically force a reload for the CursorLoader.
         */
        void onRequestCompleted();
    }

    private CalendarIntentResultListener mListener;
    private final static int REQUEST_ADD_CALENDAR_EVENT = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_ADD_CALENDAR_EVENT == requestCode) {
            mListener.onRequestCompleted();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mListener = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

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

}
