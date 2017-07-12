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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.sectionedrecyclerview.ItemCoord;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.challenge.calendar.AgendaDataSource.INVALID_HEADER;


public class MainActivityFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private CalendarPickerView mCalendarView;
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

    private static final String PERMISSION_READ_CALENDAR = "android.permission.READ_CALENDAR";
    private static final String TOAST_TEXT_MISSING_PERMISSION =
            "Please check if you have granted the READ_CALENDAR permissions";

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
                long header = mDataSource.getHeader(coord.section());
                if (INVALID_HEADER == header) {
                    return;
                }
                mCalendarView.selectDate(new Date(header));
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }
        }
    };

    public MainActivityFragment() {
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
                (CalendarPickerView) contentView.findViewById(R.id.calendar_view);
        Date today = new Date();
        mCalendarView.init(prevYear.getTime(), nextYear.getTime())
                .withSelectedDate(today);

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
        if (PERMISSION_GRANTED == getActivity()
                .checkCallingOrSelfPermission(PERMISSION_READ_CALENDAR)) {

            getLoaderManager().initLoader(0, null, this);
            mDataSource = new AgendaDataSource(Calendar.getInstance(),
                    new SimpleDateFormat("EEE, d MMM", Locale.US),
                    new SimpleDateFormat("HH:mm", Locale.US));
            mAdapter = new AgendaViewAdapter();
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.shouldShowFooters(false);
        } else {
            Toast.makeText(getActivity(),
                    TOAST_TEXT_MISSING_PERMISSION, Toast.LENGTH_SHORT)
                    .show();
        }
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

        int offset = 0;
        try {
            // today
            long now = new Date().getTime() + DateUtils.DAY_IN_MILLIS;
            final Calendar cal = Calendar.getInstance();
            final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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
                if (INVALID_HEADER == section) continue;
                mDataSource.addItem(section, event);
                /*
                if (!mAllEvents.containsKey(beginVal)) {
                    mBeginValues.add(beginVal);
                    List<CalendarEvent> events = new ArrayList<>();
                    events.add(event);
                    mAllEvents.put(beginVal, events);
                    if (beginVal <= now) offset++;
                } else {
                    mAllEvents.get(beginVal).add(event);
                    if (beginVal <= now) offset++;
                }
                */
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }

        mTextView.setVisibility(View.GONE);
        mAdapter.setDataSource(mDataSource);
        mRecyclerView.scrollToPosition(offset);
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
            return INVALID_HEADER;
        }
    }
}
