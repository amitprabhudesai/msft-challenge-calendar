package org.challenge.calendar;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Custom implementation of an {@link CursorLoader} to query for,
 * and listen for changes to calendar events.
 */
public class CalendarCursorLoader extends CursorLoader {

    private CalendarIntentReceiver mCalendarObserver;

    public CalendarCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        if (null == mCalendarObserver) {
            mCalendarObserver = new CalendarIntentReceiver(this);
        }

        if (takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (mCalendarObserver != null) {
            getContext().unregisterReceiver(mCalendarObserver);
            mCalendarObserver = null;
        }
    }
}
