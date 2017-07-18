package com.squareup.timessquare;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarHeaderView extends LinearLayout {

    private DateFormat weekdayNameFormat;
    private DateFormat monthNameFormat;

    public void init(Calendar today, DateFormat weekdayNameFormat, DateFormat monthNameFormat,
                     int headerTextColor, Locale locale) {
        this.weekdayNameFormat = weekdayNameFormat;
        this.monthNameFormat = monthNameFormat;

        final TextView monthLabel = (TextView) getChildAt(0);
        monthLabel.setText(monthNameFormat.format(today.getTime()));

        final CalendarRowView headerRow = (CalendarRowView) getChildAt(1);
        final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int firstDayOfWeek = today.getFirstDayOfWeek();

        for (int offset = 0; offset < 7; offset++) {
            today.set(Calendar.DAY_OF_WEEK, getDayOfWeek(firstDayOfWeek, offset, isRtl(locale)));
            final TextView textView = (TextView) headerRow.getChildAt(offset);
            textView.setText(weekdayNameFormat.format(today.getTime()));
        }
        headerRow.setCellTextColor(headerTextColor);
        today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
    }

    public CalendarHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void handleDateSelectionChanged(Date newDate) {
        final TextView monthLabel = (TextView) getChildAt(0);
        monthLabel.setText(monthNameFormat.format(newDate));
    }

    private int getDayOfWeek(int firstDayOfWeek, int offset, boolean isRtl) {
        int dayOfWeek = firstDayOfWeek + offset;
        if (isRtl) {
            return 8 - dayOfWeek;
        }
        return dayOfWeek;
    }

    private boolean isRtl(Locale locale) {
        // TODO convert the build to gradle and use getLayoutDirection instead of this (on 17+)?
        final int directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }
}
