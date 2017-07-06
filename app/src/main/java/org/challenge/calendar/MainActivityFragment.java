package org.challenge.calendar;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Instances;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private List<CalendarEvent> mEvents;

    private static final String[] INSTANCES_PROJECTION = new String[] {
            Instances.EVENT_ID,        // 0
            Instances.CALENDAR_ID,     // 1
            Instances.TITLE,           // 2
            Instances.EVENT_LOCATION,  // 3
            Instances.ALL_DAY,         // 4
            Instances.BEGIN,           // 5
            Instances.END              // 6
     };

    private static final String INSTANCES_SORT_ORDER =
            Instances.START_DAY + " ASC, " + Instances.START_MINUTE + " ASC";
    private static final int PROJECTION_EVENT_ID_INDEX       = 0;
    private static final int PROJECTION_CALENDAR_ID_INDEX    = 1;
    private static final int PROJECTION_TITLE_INDEX          = 2;
    private static final int PROJECTION_EVENT_LOCATION_INDEX = 3;
    private static final int PROJECTION_ALL_DAY_INDEX        = 4;
    private static final int PROJECTION_BEGIN_INDEX          = 5;
    private static final int PROJECTION_END_INDEX            = 6;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.fragment_main, container, false);

        // calendar view
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        CalendarPickerView calendar =
                (CalendarPickerView) contentView.findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);

        // agenda view
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mEvents = new ArrayList<>();
        getLoaderManager().initLoader(0, null, this);

        AgendaViewAdapter agendaViewAdapter = new AgendaViewAdapter();
        mRecyclerView.setAdapter(agendaViewAdapter);
        agendaViewAdapter.shouldShowFooters(false);
        agendaViewAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long now = new Date().getTime();
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
        ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);

        return new CursorLoader(getActivity(),
                builder.build(), INSTANCES_PROJECTION,
                null, null, // select all
                INSTANCES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null == data || data.getCount() <= 0) {
            return;
        }

        final Calendar cal = Calendar.getInstance();
        final DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.US);
        String begin, end;
        try {
            while (data.moveToNext()) {
                long id = data.getLong(PROJECTION_EVENT_ID_INDEX);
                long calId = data.getLong(PROJECTION_CALENDAR_ID_INDEX);
                String title = data.getString(PROJECTION_TITLE_INDEX);
                String location = data.getString(PROJECTION_EVENT_LOCATION_INDEX);
                int allDay = data.getInt(PROJECTION_ALL_DAY_INDEX);
                begin = "";
                end = "";
                if (0 == allDay) {
                    long beginVal = data.getLong(PROJECTION_BEGIN_INDEX);
                    cal.setTimeInMillis(beginVal);
                    begin = formatter.format(cal.getTime());
                    long endVal = data.getLong(PROJECTION_END_INDEX);
                    cal.setTimeInMillis(endVal);
                    end = formatter.format(cal.getTime());
                }

                CalendarEvent event = new CalendarEvent(id, calId, title, location, allDay);
                if (!event.isAllDay()) {
                    event.setBeginTime(begin);
                    event.setEndTime(end);
                }

                mEvents.add(event);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }

        //TODO(amit.prabhudesai) Set the data source for the adapter
        printEvents();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void printEvents() {
        if (null == mEvents || 0 == mEvents.size()) {
            Log.w(TAG, "No events found");
            return;
        }

        for (final CalendarEvent evt : mEvents) {
            Log.d(TAG, evt.toString());
        }
    }

}
