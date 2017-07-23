package org.challenge.calendar;

import android.app.Activity;
import android.app.Application;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;

public class CalendarApplication extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> mDispatchingActivityInjector;

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mDispatchingActivityInjector;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerCalendarApplicationComponent.create().inject(this);
    }
}
