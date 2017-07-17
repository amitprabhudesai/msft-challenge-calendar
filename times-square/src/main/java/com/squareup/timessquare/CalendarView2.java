package com.squareup.timessquare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.WEEK_OF_MONTH;
import static java.util.Calendar.YEAR;

public class CalendarView2 extends RecyclerView {

    private final CalendarView2.WeekAdapter adapter;
    private final IndexedLinkedHashMap<String, List<WeekCellDescriptor>> cells =
            new IndexedLinkedHashMap<>();
    final List<WeekDescriptor> weeks = new ArrayList<>();
    WeekCellDescriptor selectedCell;
    WeekCellDescriptor highlightedCell;
    Calendar selectedCal;
    Calendar highlightedCal;
    private Locale locale;
    private TimeZone timeZone;
    private WeekView.Listener listener = new CellClickedListener();
    private DateFormat weekdayNameFormat;
    private DateFormat fullDateFormat;
    private Calendar minCal;
    private Calendar maxCal;
    private Calendar weekCounter;
    private boolean displayOnly;
    Calendar today;
    private int dayBackgroundResId;
    private int dayTextColorResId;
    private int titleTextColor;
    private Typeface titleTypeface;
    private Typeface dateTypeface;

    private DateSelectionChangedListener dateListener;
    private DateSelectableFilter dateConfiguredListener;
    private OnInvalidDateSelectedListener invalidDateListener =
            new DefaultOnInvalidDateSelectedListener();
    private CellClickInterceptor cellClickInterceptor;
    private CalendarCellDecorator decorator;
    private DayViewAdapter dayViewAdapter = new DefaultDayViewAdapter();

    public CalendarView2(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarPickerView);
        final int bg = a.getColor(R.styleable.CalendarPickerView_android_background,
                res.getColor(R.color.calendar_bg));
        dayBackgroundResId = a.getResourceId(R.styleable.CalendarPickerView_tsquare_dayBackground,
                R.drawable.calendar_bg_selector);
        dayTextColorResId = a.getResourceId(R.styleable.CalendarPickerView_tsquare_dayTextColor,
                R.color.calendar_text_selector);
        titleTextColor = a.getColor(R.styleable.CalendarPickerView_tsquare_titleTextColor,
                res.getColor(R.color.calendar_text_active));
        a.recycle();

        setLayoutManager(new LinearLayoutManager(context));
        adapter = new WeekAdapter();
        setBackgroundColor(bg);
        timeZone = TimeZone.getDefault();
        locale = Locale.getDefault();
        today = Calendar.getInstance(timeZone, locale);
        minCal = Calendar.getInstance(timeZone, locale);
        maxCal = Calendar.getInstance(timeZone, locale);
        weekCounter = Calendar.getInstance(timeZone, locale);
        weekdayNameFormat = new SimpleDateFormat(context.getString(R.string.day_name_format), locale);
        weekdayNameFormat.setTimeZone(timeZone);
        fullDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        fullDateFormat.setTimeZone(timeZone);

        if (isInEditMode()) {
            Calendar nextYear = Calendar.getInstance(timeZone, locale);
            nextYear.add(Calendar.YEAR, 1);

            init(new Date(), nextYear.getTime()) //
                    .withSelectedDate(new Date());
        }
    }

    /**
     * Both date parameters must be non-null and their {@link Date#getTime()} must not return 0. Time
     * of day will be ignored.  For instance, if you pass in {@code minDate} as 11/16/2012 5:15pm and
     * {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first selectable date and
     * 11/15/2013 will be the last selectable date ({@code maxDate} is exclusive).
     * <p>
     * The calendar will be constructed using the given time zone and the given locale. This means
     * that all dates will be in given time zone, all names (months, days) will be in the language
     * of the locale and the weeks start with the day specified by the locale.
     *
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public FluentInitializer init(Date minDate, Date maxDate, TimeZone timeZone, Locale locale) {
        if (minDate == null || maxDate == null) {
            throw new IllegalArgumentException(
                    "minDate and maxDate must be non-null.  " + dbg(minDate, maxDate));
        }
        if (minDate.after(maxDate)) {
            throw new IllegalArgumentException(
                    "minDate must be before maxDate.  " + dbg(minDate, maxDate));
        }
        if (locale == null) {
            throw new IllegalArgumentException("Locale is null.");
        }
        if (timeZone == null) {
            throw new IllegalArgumentException("Time zone is null.");
        }

        // Make sure that all calendar instances use the same time zone and locale.
        this.timeZone = timeZone;
        this.locale = locale;
        today = Calendar.getInstance(timeZone, locale);
        minCal = Calendar.getInstance(timeZone, locale);
        maxCal = Calendar.getInstance(timeZone, locale);
        weekdayNameFormat =
                new SimpleDateFormat(getContext().getString(R.string.day_name_format), locale);
        weekdayNameFormat.setTimeZone(timeZone);
        fullDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        fullDateFormat.setTimeZone(timeZone);

        // Clear previous state.
        cells.clear();
        weeks.clear();
        minCal.setTime(minDate);
        maxCal.setTime(maxDate);
        setMidnight(minCal);
        setMidnight(maxCal);
        displayOnly = false;

        // maxDate is exclusive: bump back to the previous day
        // so if maxDate is the first of a month, we don't accidentally
        // include that month in the view.
        maxCal.add(MINUTE, -1);

        // Now iterate between minCal and maxCal and build up our list of weeks to show.
        weekCounter.setTime(minCal.getTime());
        final int maxMonth = maxCal.get(WEEK_OF_MONTH);
        final int maxYear = maxCal.get(YEAR);
        while ((weekCounter.get(MONTH) <= maxMonth // Up to, including the month.
                || weekCounter.get(YEAR) < maxYear) // Up to the year.
                && weekCounter.get(YEAR) < maxYear + 1) { // But not > next yr.
            Date date = weekCounter.getTime();
            WeekDescriptor week =
                    new WeekDescriptor(weekCounter.get(WEEK_OF_MONTH),
                            weekCounter.get(MONTH),
                            weekCounter.get(YEAR), date);
            cells.put(weekKey(week), getWeekCells(week, weekCounter));
            Logr.d("Adding week %s", week);
            weeks.add(week);
            weekCounter.add(WEEK_OF_MONTH, 1);
        }

        validateAndUpdate();
        return new FluentInitializer();
    }

    /**
     * Both date parameters must be non-null and their {@link Date#getTime()} must not return 0. Time
     * of day will be ignored.  For instance, if you pass in {@code minDate} as 11/16/2012 5:15pm and
     * {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first selectable date and
     * 11/15/2013 will be the last selectable date ({@code maxDate} is exclusive).
     * <p>
     * The calendar will be constructed using the default locale as returned by
     * {@link java.util.Locale#getDefault()} and default time zone as returned by
     * {@link java.util.TimeZone#getDefault()}. If you wish the calendar to be constructed using a
     * different locale or time zone, use
     * {@link #init(java.util.Date, java.util.Date, java.util.Locale)},
     * {@link #init(java.util.Date, java.util.Date, java.util.TimeZone)} or
     * {@link #init(java.util.Date, java.util.Date, java.util.TimeZone, java.util.Locale)}.
     *
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public FluentInitializer init(Date minDate, Date maxDate) {
        return init(minDate, maxDate, TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Both date parameters must be non-null and their {@link Date#getTime()} must not return 0. Time
     * of day will be ignored.  For instance, if you pass in {@code minDate} as 11/16/2012 5:15pm and
     * {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first selectable date and
     * 11/15/2013 will be the last selectable date ({@code maxDate} is exclusive).
     * <p>
     * The calendar will be constructed using the given time zone and the default locale as returned
     * by {@link java.util.Locale#getDefault()}. This means that all dates will be in given time zone.
     * If you wish the calendar to be constructed using a different locale, use
     * {@link #init(java.util.Date, java.util.Date, java.util.Locale)} or
     * {@link #init(java.util.Date, java.util.Date, java.util.TimeZone, java.util.Locale)}.
     *
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public FluentInitializer init(Date minDate, Date maxDate, TimeZone timeZone) {
        return init(minDate, maxDate, timeZone, Locale.getDefault());
    }

    /**
     * Both date parameters must be non-null and their {@link Date#getTime()} must not return 0. Time
     * of day will be ignored.  For instance, if you pass in {@code minDate} as 11/16/2012 5:15pm and
     * {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first selectable date and
     * 11/15/2013 will be the last selectable date ({@code maxDate} is exclusive).
     * <p>
     * The calendar will be constructed using the given locale. This means that all names
     * (months, days) will be in the language of the locale and the weeks start with the day
     * specified by the locale.
     * <p>
     * The calendar will be constructed using the given locale and the default time zone as returned
     * by {@link java.util.TimeZone#getDefault()}. This means that all names (months, days) will be
     * in the language of the locale and the weeks start with the day specified by the locale.
     * If you wish the calendar to be constructed using a different time zone, use
     * {@link #init(java.util.Date, java.util.Date, java.util.TimeZone)} or
     * {@link #init(java.util.Date, java.util.Date, java.util.TimeZone, java.util.Locale)}.
     *
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public FluentInitializer init(Date minDate, Date maxDate, Locale locale) {
        return init(minDate, maxDate, TimeZone.getDefault(), locale);
    }

    public class FluentInitializer {
        /**
         * Set an initially-selected date.
         * The calendar will scroll to that date if it's not already visible.
         */
        public FluentInitializer withSelectedDate(Date selectedDate) {
            selectDate(selectedDate);
            //FIXME Scroll to selected date
            scrollToSelectedDate();

            validateAndUpdate();
            return this;
        }

        @SuppressLint("SimpleDateFormat")
        public FluentInitializer setShortWeekdays(String[] newShortWeekdays) {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            symbols.setShortWeekdays(newShortWeekdays);
            weekdayNameFormat =
                    new SimpleDateFormat(
                            getContext().getString(R.string.day_name_format),
                            symbols);
            return this;
        }

        public FluentInitializer displayOnly() {
            displayOnly = true;
            return this;
        }
    }

    private void validateAndUpdate() {
        if (getAdapter() == null) {
            setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }

    private void scrollToSelectedWeek(final int selectedIndex) {
        scrollToSelectedWeek(selectedIndex, false);
    }

    private void scrollToSelectedWeek(final int selectedIndex,
                                      final boolean smoothScroll) {
        post(new Runnable() {
            @Override
            public void run() {
                Logr.d("Scrolling to position %d", selectedIndex);

                if (smoothScroll) {
                    smoothScrollToPosition(selectedIndex);
                } else {
                    //TODO(amit.prabhudesai) Check this out
                    ((LinearLayoutManager) getLayoutManager())
                            .scrollToPositionWithOffset(selectedIndex, 0);
                }
            }
        });
    }

    private void scrollToSelectedDate() {
        Integer selectedIndex = null;
        Integer todayIndex = null;
        Calendar today = Calendar.getInstance(timeZone, locale);
        for (int c = 0; c < weeks.size(); c++) {
            WeekDescriptor week = weeks.get(c);
            if (selectedIndex == null) {
                if (sameWeek(selectedCal, week)) {
                    selectedIndex = c;
                }
                if (selectedIndex == null && todayIndex == null && sameWeek(today, week)) {
                    todayIndex = c;
                }
            }
        }
        if (selectedIndex != null) {
            scrollToSelectedWeek(selectedIndex);
        } else if (todayIndex != null) {
            scrollToSelectedWeek(todayIndex);
        }
    }

    public boolean scrollToDate(Date date) {
        Integer selectedIndex = null;

        Calendar cal = Calendar.getInstance(timeZone, locale);
        cal.setTime(date);
        for (int c = 0; c < weeks.size(); c++) {
            WeekDescriptor week = weeks.get(c);
            if (sameWeek(cal, week)) {
                selectedIndex = c;
                break;
            }
        }
        if (selectedIndex != null) {
            scrollToSelectedWeek(selectedIndex);
            return true;
        }
        return false;
    }

    /**
     * Set the typeface to be used for month titles.
     */
    public void setTitleTypeface(Typeface titleTypeface) {
        this.titleTypeface = titleTypeface;
        validateAndUpdate();
    }

    /**
     * Sets the typeface to be used within the date grid.
     */
    public void setDateTypeface(Typeface dateTypeface) {
        this.dateTypeface = dateTypeface;
        validateAndUpdate();
    }

    /**
     * Sets the typeface to be used for all text within this calendar.
     */
    public void setTypeface(Typeface typeface) {
        setTitleTypeface(typeface);
        setDateTypeface(typeface);
    }

    public Date getSelectedDate() {
        //TODO(amit.prabhudesai) Check if a cal is always selected
        return selectedCal.getTime();
    }

    /**
     * Returns a string summarizing what the client sent us for init() params.
     */
    private static String dbg(Date minDate, Date maxDate) {
        return "minDate: " + minDate + "\nmaxDate: " + maxDate;
    }

    /**
     * Clears out the hours/minutes/seconds/millis of a Calendar.
     */
    static void setMidnight(Calendar cal) {
        cal.set(HOUR_OF_DAY, 0);
        cal.set(MINUTE, 0);
        cal.set(SECOND, 0);
        cal.set(MILLISECOND, 0);
    }

    private class CellClickedListener implements WeekView.Listener {
        @Override
        public void onCellClicked(WeekCellDescriptor cell) {
            Date clickedDate = cell.getDate();
            if (cellClickInterceptor != null && cellClickInterceptor.onCellClicked(clickedDate)) {
                return;
            }
            if (!betweenDates(clickedDate, minCal, maxCal) || !isDateSelectable(clickedDate)) {
                if (invalidDateListener != null) {
                    invalidDateListener.onInvalidDateSelected(clickedDate);
                }
            } else {
                boolean wasSelected = doSelectDate(clickedDate, cell);

                if (dateListener != null) {
                    if (wasSelected) {
                        dateListener.onDateSelected(clickedDate);
                    } else {
                        dateListener.onDateUnselected(clickedDate);
                    }
                }
            }
        }
    }

    /**
     * Select a new date.
     * <p>
     * If the selection was made (selectable date, in range), the view will scroll to the newly
     * selected date if it's not already visible.
     *
     * @return - whether we were able to set the date
     */
    public boolean selectDate(Date date) {
        return selectDate(date, false);
    }

    /**
     * Select a new date.
     * <p>
     * If the selection was made (selectable date, in range), the view will scroll to the newly
     * selected date if it's not already visible.
     *
     * @return - whether we were able to set the date
     */
    public boolean selectDate(Date date, boolean smoothScroll) {
        validateDate(date);

        WeekCellWithWeekIndex weekCellWithIndexByDate = getWeekCellWithIndexByDate(date);
        if (weekCellWithIndexByDate == null || !isDateSelectable(date)) {
            return false;
        }
        boolean wasSelected = doSelectDate(date, weekCellWithIndexByDate.cell);
        if (wasSelected) {
            scrollToSelectedWeek(weekCellWithIndexByDate.monthIndex, smoothScroll);
        }
        return wasSelected;
    }

    private void validateDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Selected date must be non-null.");
        }
        if (date.before(minCal.getTime()) || date.after(maxCal.getTime())) {
            throw new IllegalArgumentException(String.format(
                    "SelectedDate must be between minDate and maxDate."
                            + "%nminDate: %s%nmaxDate: %s%nselectedDate: %s", minCal.getTime(), maxCal.getTime(),
                    date));
        }
    }

    private boolean doSelectDate(Date date, WeekCellDescriptor cell) {
        Calendar newlySelectedCal = Calendar.getInstance(timeZone, locale);
        newlySelectedCal.setTime(date);
        // Sanitize input: clear out the hours/minutes/seconds/millis.
        setMidnight(newlySelectedCal);

        clearOldSelections();

        if (date != null) {
            // Select a new cell.
            if (selectedCell != cell) {
                selectedCell = cell;
                cell.setSelected(true);
            }
            selectedCal = newlySelectedCal;
        }

        // Update the adapter.
        validateAndUpdate();
        return date != null;
    }

    private String weekKey(Calendar cal) {
        return cal.get(YEAR) + "-" + cal.get(MONTH) + "-" + cal.get(WEEK_OF_MONTH);
    }

    private String weekKey(WeekDescriptor week) {
        return week.getYear() + "-" + week.getMonth() + "-" + week.getWeek();
    }

    private void clearOldSelections() {
        if (selectedCell != null) {
            selectedCell.setSelected(false);
        }
        selectedCell = null;
        selectedCal = null;
    }

    /**
     * Hold a cell with a week-index.
     */
    private static class WeekCellWithWeekIndex {
        public WeekCellDescriptor cell;
        public int monthIndex;

        public WeekCellWithWeekIndex(WeekCellDescriptor cell, int monthIndex) {
            this.cell = cell;
            this.monthIndex = monthIndex;
        }
    }

    /**
     * Return cell and week-index (for scrolling) for a given Date.
     */
    private WeekCellWithWeekIndex getWeekCellWithIndexByDate(Date date) {
        Calendar searchCal = Calendar.getInstance(timeZone, locale);
        searchCal.setTime(date);
        String weekKey = weekKey(searchCal);
        Calendar actCal = Calendar.getInstance(timeZone, locale);

        int index = cells.getIndexOfKey(weekKey);
        List<WeekCellDescriptor> weekCells = cells.get(weekKey);
        for (WeekCellDescriptor actCell : weekCells) {
            actCal.setTime(actCell.getDate());
            if (sameDate(actCal, searchCal) && actCell.isSelectable()) {
                return new WeekCellWithWeekIndex(actCell, index);
            }
        }

        return null;
    }

    private final class WeekAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public WeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout layout = new LinearLayout(parent.getContext());
            layout.setLayoutParams(new RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            return new WeekViewHolder(layout, parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            WeekViewHolder holder = (WeekViewHolder) viewHolder;
            holder.weekView.init(weeks.get(position), cells.getValueAtIndex(position), displayOnly, titleTypeface, dateTypeface);
        }

        @Override
        public int getItemCount() {
            return weeks.size();
        }

        final class WeekViewHolder extends ViewHolder {

            private final WeekView weekView;

            public WeekViewHolder(View itemView, ViewGroup parent) {
                super(itemView);
                this.weekView = WeekView.create(parent, LayoutInflater.from(parent.getContext()),
                        weekdayNameFormat, listener, today,
                        dayBackgroundResId, dayTextColorResId, titleTextColor, decorator, locale, dayViewAdapter);

                LinearLayout layout = (LinearLayout) itemView;
                layout.addView(weekView);
            }
        }
    }

    List<WeekCellDescriptor> getWeekCells(WeekDescriptor week, Calendar startCal) {
        Calendar cal = Calendar.getInstance(timeZone, locale);
        cal.setTime(startCal.getTime());
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        if (offset > 0) {
            offset -= 7;
        }
        cal.add(Calendar.DATE, offset);
        List<WeekCellDescriptor> weekCells = new ArrayList<>();

        Logr.d("Building week row starting at %s", cal.getTime());
        for (int c = 0; c < 7; c++) {
            Date date = cal.getTime();
            boolean isCurrentWeek = cal.get(WEEK_OF_MONTH) == week.getWeek();
            boolean isSelected = isCurrentWeek && containsDate(selectedCal, cal);
            boolean isSelectable =
                    isCurrentWeek && betweenDates(cal, minCal, maxCal) && isDateSelectable(date);
            boolean isToday = sameDate(cal, today);
            boolean isHighlighted = containsDate(highlightedCal, cal);
            int value = cal.get(DAY_OF_MONTH);

            weekCells.add(new WeekCellDescriptor(date, isCurrentWeek,
                    isSelectable, isSelected, isToday, isHighlighted, value));
            cal.add(DATE, 1);
        }
        return weekCells;
    }

    private boolean containsDate(Calendar selectedCal, Date date) {
        Calendar cal = Calendar.getInstance(timeZone, locale);
        cal.setTime(date);
        return containsDate(selectedCal, cal);
    }

    private static boolean containsDate(Calendar selectedCal, Calendar cal) {
        return null != selectedCal && sameDate(cal, selectedCal);
    }

    private static Calendar minDate(List<Calendar> selectedCals) {
        if (selectedCals == null || selectedCals.size() == 0) {
            return null;
        }
        Collections.sort(selectedCals);
        return selectedCals.get(0);
    }

    private static Calendar maxDate(List<Calendar> selectedCals) {
        if (selectedCals == null || selectedCals.size() == 0) {
            return null;
        }
        Collections.sort(selectedCals);
        return selectedCals.get(selectedCals.size() - 1);
    }

    private static boolean sameDate(Calendar selectedDate, Calendar cal) {
        return cal.get(MONTH) == selectedDate.get(MONTH)
                && cal.get(YEAR) == selectedDate.get(YEAR)
                && cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
    }

    private static boolean betweenDates(Calendar cal, Calendar minCal, Calendar maxCal) {
        final Date date = cal.getTime();
        return betweenDates(date, minCal, maxCal);
    }

    static boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
        final Date min = minCal.getTime();
        return (date.equals(min) || date.after(min)) // >= minCal
                && date.before(maxCal.getTime()); // && < maxCal
    }

    private static boolean sameWeek(Calendar cal, WeekDescriptor week) {
        return (cal.get(WEEK_OF_MONTH) == week.getMonth() &&
                cal.get(MONTH) == week.getMonth() &&
                cal.get(YEAR) == week.getYear());
    }

    private boolean isDateSelectable(Date date) {
        return dateConfiguredListener == null || dateConfiguredListener.isDateSelectable(date);
    }

    public void setDateSelectionChangedListener(DateSelectionChangedListener listener) {
        dateListener = listener;
    }

    /**
     * Set a listener to react to user selection of a disabled date.
     *
     * @param listener the listener to set, or null for no reaction
     */
    public void setOnInvalidDateSelectedListener(OnInvalidDateSelectedListener listener) {
        invalidDateListener = listener;
    }

    /**
     * Set a listener used to discriminate between selectable and unselectable dates. Set this to
     * disable arbitrary dates as they are rendered.
     * <p>
     * Important: set this before you call {@link #init(Date, Date)} methods.  If called afterwards,
     * it will not be consistently applied.
     */
    public void setDateSelectableFilter(DateSelectableFilter listener) {
        dateConfiguredListener = listener;
    }

    /**
     * Set an adapter used to initialize {@link CalendarCellView} with custom layout.
     * <p>
     * Important: set this before you call {@link #init(Date, Date)} methods.  If called afterwards,
     * it will not be consistently applied.
     */
    public void setCustomDayView(DayViewAdapter dayViewAdapter) {
        this.dayViewAdapter = dayViewAdapter;
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Set a listener to intercept clicks on calendar cells.
     */
    public void setCellClickInterceptor(CellClickInterceptor listener) {
        cellClickInterceptor = listener;
    }

    /**
     * Interface to be notified when a new date is selected or unselected. This will only be called
     * when the user initiates the date selection.  If you call {@link #selectDate(Date)} this
     * listener will not be notified.
     *
     * @see #setDateSelectionChangedListener(DateSelectionChangedListener)
     */
    public interface DateSelectionChangedListener {
        void onDateSelected(Date date);

        void onDateUnselected(Date date);
    }

    /**
     * Interface to be notified when an invalid date is selected by the user. This will only be
     * called when the user initiates the date selection. If you call {@link #selectDate(Date)} this
     * listener will not be notified.
     *
     * @see #setOnInvalidDateSelectedListener(OnInvalidDateSelectedListener)
     */
    public interface OnInvalidDateSelectedListener {
        void onInvalidDateSelected(Date date);
    }

    /**
     * Interface used for determining the selectability of a date cell when it is configured for
     * display on the calendar.
     *
     * @see #setDateSelectableFilter(DateSelectableFilter)
     */
    public interface DateSelectableFilter {
        boolean isDateSelectable(Date date);
    }

    /**
     * Interface to be notified when a cell is clicked and possibly intercept the click.  Return true
     * to intercept the click and prevent any selections from changing.
     *
     * @see #setCellClickInterceptor(CellClickInterceptor)
     */
    public interface CellClickInterceptor {
        boolean onCellClicked(Date date);
    }

    private class DefaultOnInvalidDateSelectedListener implements OnInvalidDateSelectedListener {
        @Override
        public void onInvalidDateSelected(Date date) {
            String errMessage =
                    getResources().getString(R.string.invalid_date, fullDateFormat.format(minCal.getTime()),
                            fullDateFormat.format(maxCal.getTime()));
            Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT).show();
        }

    }
}
