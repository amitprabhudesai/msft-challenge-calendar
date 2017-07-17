package com.squareup.timessquare;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeekView extends LinearLayout {

    /**
     * Listener to be notified of cell click events
     */
    public interface Listener {
        /**
         * Called to notify that a cell was clicked.
         * @param cell
         */
        void onCellClicked(WeekCellDescriptor cell);
    }

    Listener listener;
    CalendarCellDecorator decorator;
    boolean isRtl;
    Locale locale;

    public WeekView(Context context) {
        super(context);
    }

    public WeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static WeekView create(ViewGroup parent, LayoutInflater inflater,
                                   DateFormat weekdayNameFormat, Listener listener, Calendar today,
                                   int dayBackgroundResId, int dayTextColorResId, int titleTextColor, Locale locale, DayViewAdapter adapter) {
        return create(parent, inflater, weekdayNameFormat, listener, today,
                dayBackgroundResId, dayTextColorResId, titleTextColor, null,
                locale, adapter);
    }

    public static WeekView create(ViewGroup parent, LayoutInflater inflater,
                                   DateFormat weekdayNameFormat, Listener listener, Calendar today,
                                   int dayBackgroundResId, int dayTextColorResId, int titleTextColor, CalendarCellDecorator decorator, Locale locale,
                                   DayViewAdapter adapter) {
        final WeekView view = (WeekView) inflater.inflate(R.layout.row, parent, false);
        view.setDayViewAdapter(adapter);
        view.setDayTextColor(dayTextColorResId);

        if (dayBackgroundResId != 0) {
            view.setDayBackground(dayBackgroundResId);
        }

        final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        view.isRtl = isRtl(locale);
        view.locale = locale;
        today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
        view.listener = listener;
        view.decorator = decorator;
        return view;
    }

    private static boolean isRtl(Locale locale) {
        // TODO convert the build to gradle and use getLayoutDirection instead of this (on 17+)?
        final int directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public void setDecorator(CalendarCellDecorator decorator) {
        this.decorator = decorator;
    }

    public CalendarCellDecorator getDecorator() {
        return decorator;
    }

    public void init(WeekDescriptor week, List<WeekCellDescriptor> cells,
                     boolean displayOnly, Typeface titleTypeface, Typeface dateTypeface) {
        Logr.d("Initializing WeekView (%d) for %s", System.identityHashCode(this), week);
        long start = System.currentTimeMillis();
        NumberFormat numberFormatter = NumberFormat.getInstance(locale);
        CalendarRowView weekRow = (CalendarRowView) getChildAt(0);
        weekRow.setListener2(listener);

        for (int c = 0; c < cells.size(); c++) {
            WeekCellDescriptor cell = cells.get(isRtl ? 6 - c : c);
            CalendarCellView cellView = (CalendarCellView) weekRow.getChildAt(c);
            String cellDate = numberFormatter.format(cell.getValue());
            if (!cellView.getDayOfMonthTextView().getText().equals(cellDate)) {
                cellView.getDayOfMonthTextView().setText(cellDate);
            }
            cellView.setEnabled(cell.isCurrentWeek());
            cellView.setClickable(!displayOnly);
            cellView.setSelectable(cell.isSelectable());
            cellView.setSelected(cell.isSelected());
            cellView.setCurrentMonth(cell.isCurrentWeek());
            cellView.setToday(cell.isToday());
            cellView.setHighlighted(cell.isHighlighted());
            cellView.setTag(cell);

            if (null != decorator) {
                decorator.decorate(cellView, cell.getDate());
            }
        }

        if (dateTypeface != null) {
            ((CalendarRowView) getChildAt(1)).setTypeface(dateTypeface);
        }

        Logr.d("WeekView.init took %d ms", System.currentTimeMillis() - start);
    }

    public void setDayBackground(int resId) {
        ((CalendarRowView) getChildAt(0)).setCellBackground(resId);
    }

    public void setDayTextColor(int resId) {
        ((CalendarRowView) getChildAt(0)).setCellTextColor(resId);
    }

    public void setDayViewAdapter(DayViewAdapter adapter) {
        ((CalendarRowView) getChildAt(0)).setDayViewAdapter(adapter);
    }
}
