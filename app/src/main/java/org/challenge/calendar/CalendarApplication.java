package org.challenge.calendar;

import android.app.Application;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class CalendarApplication extends Application implements HasSupportFragmentInjector {
    @Inject
    DispatchingAndroidInjector<Fragment> mDispatchingFragmentInjector;

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mDispatchingFragmentInjector;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerCalendarApplicationComponent.create().inject(this);
    }
}
