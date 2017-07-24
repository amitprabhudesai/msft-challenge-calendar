package org.challenge.calendar;

import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Component(modules = {AndroidInjectionModule.class,
        CalendarApplicationModule.class,
        CalendarFragmentModule.class})
public interface CalendarApplicationComponent {
    void inject(CalendarApplication application);
}
