package org.challenge.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Instances;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.timessquare.CalendarHeaderView;
import com.squareup.timessquare.CalendarUtils;
import com.squareup.timessquare.CalendarView2;

import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.READ_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.CalendarContract.Events.DTSTART;
import static org.challenge.calendar.AgendaDataSource.INVALID_TIME;

public class CalendarFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = CalendarFragment.class.getSimpleName();

    /**
     * Listener to notify of date selection changes.
     */
    public interface DateSelectionChangedListener {
        /**
         * Called when a date was selected
         * @param newDate
         */
        void onDateSelected(Date newDate);
    }

    private DateSelectionChangedListener mListener;
    private CalendarHeaderView mCalendarHeaderView;
    private CalendarView2 mCalendarView;
    private RecyclerView mRecyclerView;
    private StickyAgendaViewAdapter mStickyAdapter;
    private TextView mTextView;

    private AgendaDataSource mDataSource;

    private final Calendar minCal;
    private final Calendar maxCal;

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
    private static final int ID_LOADER_CALENDAR_EVENTS = 0;

    private final StickyHeaderLayoutManager.HeaderPositionChangedCallback mHeaderPositionCallback =
            new StickyHeaderLayoutManager.HeaderPositionChangedCallback() {
                @Override
                public void onHeaderPositionChanged(int section, View header,
                                                    StickyHeaderLayoutManager.HeaderPosition oldPosition,
                                                    StickyHeaderLayoutManager.HeaderPosition newPosition) {
                    if (StickyHeaderLayoutManager.HeaderPosition.STICKY == newPosition) {
                        Date selected = new Date(mDataSource.getTime(section));
                        mCalendarView.selectDate(selected, true);
                        mListener.onDateSelected(selected);
                    }
                }
            };

    private final CalendarView2.DateSelectionChangedListener mDateSelectionChangedListener = new CalendarView2.DateSelectionChangedListener() {
        @Override
        public void onDateSelected(Date date) {
            mRecyclerView.scrollToPosition(mStickyAdapter
                    .getAdapterPositionForSectionHeader(mDataSource
                            .getSectionCeil(date.getTime())));
        }

        @Override
        public void onDateUnselected(Date date) {
        }
    };

    private final CalendarView2.OnScrolledListener mOnCalendarScrolledListener = new CalendarView2.OnScrolledListener() {
        @Override
        public void onScrolled(Date firstVisibleDate) {
            mListener.onDateSelected(firstVisibleDate);
        }
    };

    public CalendarFragment() {
        minCal = Calendar.getInstance();
        minCal.add(Calendar.MONTH, -1);
        CalendarUtils.setMidnight(minCal);
        maxCal = Calendar.getInstance();
        maxCal.add(Calendar.MONTH, 1);
        CalendarUtils.setMidnight(maxCal);
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

    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DateSelectionChangedListener) context;
        } catch (ClassCastException cce) {
            throw new ClassCastException(context.toString()
                    + " must implement DateSelectionChangedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.fragment_main, container, false);

        // Calendar header with month and weekday name labels
        // styles
        Calendar today = Calendar.getInstance();
        DateFormat weekdayNameFormat =
                new SimpleDateFormat(getContext().getString(R.string.day_name_format),
                        Locale.US);
        DateFormat monthNameFormat =
                new SimpleDateFormat(getContext().getString(R.string.month_name_format),
                        Locale.US);
        Resources res = getContext().getResources();
        TypedArray a = getContext().obtainStyledAttributes(R.styleable.CalendarPickerView);
        int headerTextColor = a.getColor(R.styleable.CalendarPickerView_tsquare_headerTextColor,
                res.getColor(R.color.calendar_text_active));
        a.recycle();

        // actual calendar header widget
        mCalendarHeaderView =
                (CalendarHeaderView) contentView.findViewById(R.id.calendar_header);
        mCalendarHeaderView.init(Calendar.getInstance(),
                weekdayNameFormat, monthNameFormat, headerTextColor, Locale.US);

        // Scrollable calendar
        mCalendarView =
                (CalendarView2) contentView.findViewById(R.id.calendar_view);
        mCalendarView.init(minCal.getTime(), maxCal.getTime()).withSelectedDate(today.getTime());
        mCalendarView.setDateSelectionChangedListener(mDateSelectionChangedListener);
        mCalendarView.setOnScrolledListener(mOnCalendarScrolledListener);

        // text view to be displayed if no events found
        mTextView = (TextView) contentView.findViewById(R.id.text_view_no_events);

        // agenda view - use the stickyheaders library to implement this
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        StickyHeaderLayoutManager layoutManager = new StickyHeaderLayoutManager();
        layoutManager.setHeaderPositionChangedCallback(mHeaderPositionCallback);
        mRecyclerView.setLayoutManager(layoutManager);

        // recycler view does not draw an item divider by default
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Calendar today = Calendar.getInstance();
        mListener.onDateSelected(today.getTime());

        // only initialize the loader if the app has permissions
        if (PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), READ_CALENDAR)) {
            requestPermissions(new String[]{READ_CALENDAR}, PERMISSIONS_REQUEST_READ_CALENDAR);
        } else {
            init();
        }
    }

    // Create the data-source and adapters; also initialize the loader
    private void init() {
        mStickyAdapter = new StickyAgendaViewAdapter(getActivity());
        mRecyclerView.setAdapter(mStickyAdapter);
        getLoaderManager().initLoader(ID_LOADER_CALENDAR_EVENTS, null, this);
    }

    public void refresh() {
        // NOTE *Always* clear the existing data before a reload to prevent
        // duplicate events
        mDataSource.clear();
        getLoaderManager().getLoader(ID_LOADER_CALENDAR_EVENTS).forceLoad();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, minCal.getTimeInMillis());
        ContentUris.appendId(builder, maxCal.getTimeInMillis());
        // For some reason setting the date range in the content URIs
        // does not work reliably and returns some events outside this
        // range. Use the selection clause to fix this and get events
        // in the correct range. Not doing so induces a crash while
        // scrolling the agenda view to this date (outside the range)
        // upon setting the date selection in the {@link CalendarView2}
        // due to an IllegalArgumentException thrown by the
        // {@link CalendarView2#validateDate(Date) method
        String selection = "(( " + DTSTART + " >= " + minCal.getTimeInMillis() +
                " ) AND ( " + DTSTART + " <= " + maxCal.getTimeInMillis() + " ))";

        return new CalendarCursorLoader(getActivity(),
                builder.build(), INSTANCES_PROJECTION,
                selection, null, // select in range
                INSTANCES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (null == data || data.getCount() <= 0) {
            return;
        }

        // create a new instance every time we are notified
        // to prevent duplicate events or having to clear the
        // older data
        mDataSource = new AgendaDataSource(Calendar.getInstance(),
                new SimpleDateFormat("EEE, d MMM", Locale.US),
                new SimpleDateFormat("HH:mm", Locale.US));

        final Calendar cal = Calendar.getInstance();
        final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            while (data.moveToNext()) {
                int id = data.getInt(PROJECTION_EVENT_ID_INDEX);
                int calId = data.getInt(PROJECTION_CALENDAR_ID_INDEX);
                String title = data.getString(PROJECTION_TITLE_INDEX);
                String location = data.getString(PROJECTION_EVENT_LOCATION_INDEX);
                int allDay = data.getInt(PROJECTION_ALL_DAY_INDEX);
                long beginVal = data.getLong(PROJECTION_BEGIN_INDEX);
                CalendarEvent event = new CalendarEvent(id, calId, title, beginVal, location, allDay);
                if (0 == allDay) {
                    long endVal = data.getLong(PROJECTION_END_INDEX);
                    event.setEndTime(endVal);
                }

                // compute the section header
                // this is just the begin time with the HH:mm:ss zeroed out
                long section = computeSectionHeader(cal, beginVal, formatter);
                if (INVALID_TIME == section) continue;
                mDataSource.addEvent(section, event);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }

        mTextView.setVisibility(View.GONE);
        mStickyAdapter.setDataSource(mDataSource);
        mStickyAdapter.notifyAllSectionsDataSetChanged();
        // show today's agenda
        // to do this compute the section floor, which is the section
        // for the last event that begins no later than the current time
        mRecyclerView.scrollToPosition(mStickyAdapter
                .getAdapterPositionForSectionHeader(mDataSource
                        .getSectionFloor(new Date().getTime() /* now */)));
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
            Log.w(TAG, "Error computing section: " + e.getMessage());
            return INVALID_TIME;
        }
    }
}
