package org.challenge.calendar;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Instances;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
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
import android.widget.CalendarView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.ItemCoord;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.READ_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.challenge.calendar.AgendaDataSource.INVALID_TIME;

public class MainActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private CalendarView mCalendarView;
    private RecyclerView mRecyclerView;
    private AgendaViewAdapter mAdapter;
    private TextView mTextView;

    private AgendaDataSource mDataSource;

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

    private static final int PERMISSIONS_REQUEST_READ_CALENDAR = 1;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            try {
                super.onScrolled(recyclerView, dx, dy);
                //TODO(amit.prabhudesai) Add a note on what happens here
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                AgendaViewAdapter adapter = (AgendaViewAdapter) recyclerView.getAdapter();
                ItemCoord coord = adapter.getRelativePosition(layoutManager.findFirstVisibleItemPosition());
                long time = mDataSource.getTime(coord.section());
                if (INVALID_TIME == time) {
                    return;
                }
                mCalendarView.setDate(new Date(time).getTime(),
                        false /* no animation */,
                        true /* center */);
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }
        }
    };

    public MainActivityFragment() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            default:
                break;
            case PERMISSIONS_REQUEST_READ_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    init();
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.fragment_main, container, false);

        // calendar view
        Calendar prevYear = Calendar.getInstance();
        prevYear.add(Calendar.YEAR, -1);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        mCalendarView =
                (CalendarView) contentView.findViewById(R.id.calendar_view);
        mCalendarView.setMinDate(prevYear.getTimeInMillis());
        mCalendarView.setMaxDate(nextYear.getTimeInMillis());
        mCalendarView.setDate(new Date().getTime() /* today */);

        // text view to be displayed if no events found
        mTextView = (TextView) contentView.findViewById(R.id.text_view_no_events);

        // agenda view
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(mOnScrollListener);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // only initialize the loader if the app has permissions
        if (PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), READ_CALENDAR)) {
            requestPermissions(new String[]{READ_CALENDAR}, PERMISSIONS_REQUEST_READ_CALENDAR);
        } else {
            init();
        }
    }

    private void init() {
        mDataSource = new AgendaDataSource(Calendar.getInstance(),
                new SimpleDateFormat("EEE, d MMM", Locale.US),
                new SimpleDateFormat("HH:mm", Locale.US));
        mAdapter = new AgendaViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.shouldShowFooters(false);
        getLoaderManager().initLoader(0, null, this);
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
        final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            while (data.moveToNext()) {
                long id = data.getLong(PROJECTION_EVENT_ID_INDEX);
                long calId = data.getLong(PROJECTION_CALENDAR_ID_INDEX);
                String title = data.getString(PROJECTION_TITLE_INDEX);
                String location = data.getString(PROJECTION_EVENT_LOCATION_INDEX);
                int allDay = data.getInt(PROJECTION_ALL_DAY_INDEX);
                long beginVal = data.getLong(PROJECTION_BEGIN_INDEX);
                CalendarEvent event = new CalendarEvent(id, calId, title, beginVal, location, allDay);
                if (0 == allDay) {
                    long endVal = data.getLong(PROJECTION_END_INDEX);
                    event.setEndTime(endVal);
                }

                long section = computeSectionHeader(cal, beginVal, formatter);
                if (INVALID_TIME == section) continue;
                mDataSource.addEvent(section, event);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }

        mTextView.setVisibility(View.GONE);
        mAdapter.setDataSource(mDataSource);
        long currentSection =
                computeSectionHeader(cal, new Date().getTime() /* now */, formatter);
        mRecyclerView.scrollToPosition(mDataSource.rank(currentSection));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private long computeSectionHeader(Calendar cal, long beginVal, DateFormat formatter) {
        cal.setTimeInMillis(beginVal);
        try {
            return formatter.parse(formatter.format(cal.getTime())).getTime();
        } catch (ParseException e) {
            // ignore, for now
            return INVALID_TIME;
        }
    }
}
