package com.squareup.timessquare;

import java.util.Date;

/**
 * Describes the state of a particular date cell in an {@link WeekView}.
 */
class WeekCellDescriptor {

    private final Date date;
    private final int value;
    private final boolean isCurrentWeek;
    private boolean isSelected;
    private final boolean isToday;
    private final boolean isSelectable;
    private boolean isHighlighted;

    WeekCellDescriptor(Date date, boolean currentWeek,
                       boolean selectable, boolean selected,
                       boolean today, boolean highlighted, int value) {
       this.date = date;
       isCurrentWeek = currentWeek;
       isSelectable = selectable;
       isHighlighted = highlighted;
       isSelected = selected;
       isToday = today;
       this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public int getValue() {
        return value;
    }

    public boolean isCurrentWeek() {
        return isCurrentWeek;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isToday() {
        return isToday;
    }

    public boolean isSelectable() {
        return isSelectable;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
    }
}
