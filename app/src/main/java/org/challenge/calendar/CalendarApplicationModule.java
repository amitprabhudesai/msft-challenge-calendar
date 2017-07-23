package org.challenge.calendar;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CalendarApplicationModule {
    @ContributesAndroidInjector
    abstract CalendarActivity contributeActivityInjector();
}
