package org.challenge.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

/**
 * Listen for changes to the Calendar content provider.
 * This then signals back to the {@link CalendarCursorLoader} to
 * refresh itself.
 */
public class CalendarIntentReceiver extends BroadcastReceiver {

    private final CalendarCursorLoader mLoader;

    public CalendarIntentReceiver(@NonNull final CalendarCursorLoader loader) {
        mLoader = loader;
        IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
        filter.addDataScheme("content");
        mLoader.getContext().registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mLoader.onContentChanged();
    }
}
