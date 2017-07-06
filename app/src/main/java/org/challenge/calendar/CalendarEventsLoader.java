package org.challenge.calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarEventsLoader extends android.support.v4.content.AsyncTaskLoader<List<CalendarEvent>> {

    private static final String TAG = CalendarEventsLoader.class.getSimpleName();

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

    public CalendarEventsLoader(Context context) {
        super(context);
    }

    @Override
    public List<CalendarEvent> loadInBackground() {
        Log.v(TAG, "loadInBackground() called");
        List<CalendarEvent> result = new ArrayList<>();
        if (PackageManager.PERMISSION_DENIED == getContext()
                .checkCallingOrSelfPermission("android.permission.READ_CALENDAR")) {
            Log.e(TAG, "No permission to read calendar!");
            return result;
        }

        ContentResolver cr = getContext().getContentResolver();
        long now = new Date().getTime();
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
        ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);
        Cursor data = cr.query(builder.build(), INSTANCES_PROJECTION,
                null, null, // select all
                INSTANCES_SORT_ORDER);

        if (null == data || data.getCount() <= 0) {
            return result;
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

                result.add(event);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        } finally {
            data.close();
        }

        printEvents(result);
        return result;
    }

    private void printEvents(List<CalendarEvent> events) {
        int i = 0;
        for (final CalendarEvent evt : events) {
            Log.d(TAG, ++i + evt.toString());
        }
    }

    @Override
    public void deliverResult(List<CalendarEvent> events) {
        if (isReset()) {
            if (events != null) {
                onReleaseResources(events);
            }
        }

        List<CalendarEvent> oldEvents = mEvents;
        mEvents = events;

        if (isStarted()) {
            // if started, deliver results immediately
            super.deliverResult(events);
        }

        // older data is no longer in use
        if (oldEvents != null) {
            onReleaseResources(oldEvents);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mEvents != null) {
            deliverResult(mEvents);
        }

        // start watching for changes in events
        if (takeContentChanged() || null == mEvents) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<CalendarEvent> events) {
        super.onCanceled(events);
        onReleaseResources(events);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mEvents != null) {
            onReleaseResources(mEvents);
            mEvents = null;
        }

        // stop watching for events
    }

    protected void onReleaseResources(List<CalendarEvent> events) {

    }
}
