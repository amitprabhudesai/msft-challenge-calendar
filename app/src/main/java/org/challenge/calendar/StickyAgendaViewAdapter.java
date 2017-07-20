package org.challenge.calendar;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.zakariya.stickyheaders.SectioningAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Adapter for the agenda view with sticky section headers.
 */
public class StickyAgendaViewAdapter extends SectioningAdapter {

    private static final String TAG = StickyAgendaViewAdapter.class.getSimpleName();

    private final Context mContext;
    private final Map<Integer, List<Integer>> mCalendarIndicators;
    private AgendaDataSource mDataSource;

    public StickyAgendaViewAdapter(@NonNull final Context context) {
        mContext = context;
        mCalendarIndicators = new HashMap<>();
    }

    public void setDataSource(@NonNull final AgendaDataSource dataSource) {
        mDataSource = dataSource;
        slotCalendarsIntoColorBins(dataSource);
    }

    private void slotCalendarsIntoColorBins(AgendaDataSource dataSource) {
        int[] colors  = mContext.getResources().getIntArray(R.array.calendar_id_colors);
        int numColors = colors.length;
        for (int c = 0; c < numColors; c++) {
            mCalendarIndicators.put(colors[c], new ArrayList<Integer>());
        }

        int colorIndex = 0;
        for (int section = 0; section < dataSource.getSectionCount(); section++) {
            for (int index = 0; index < dataSource.getEventCount(section); index++) {
                CalendarEvent event = dataSource.getEventItem(section, index);
                boolean found = false;
                for (int c = 0; c < numColors; c++) {
                    List<Integer> ids = mCalendarIndicators.get(colors[c]);
                    if (ids.contains(event.getCalendarId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // slot this calendar into one of the bins
                    int color = colors[colorIndex];
                    colorIndex = (colorIndex + 1) % numColors;
                    if (null == mCalendarIndicators.get(color)) {
                        List<Integer> ids = new ArrayList<>();
                        ids.add(event.getCalendarId());
                        mCalendarIndicators.put(color, ids);
                    } else {
                        mCalendarIndicators.get(color).add(event.getCalendarId());
                    }
                }
            }
        }
    }

    @Override
    public int getNumberOfSections() {
        return null == mDataSource ? 0 : mDataSource.getSectionCount();
    }

    @Override
    public int getNumberOfItemsInSection(int section) {
        return null == mDataSource ? 0 : mDataSource.getEventCount(section);
    }

    @Override
    public boolean doesSectionHaveHeader(int section) {
        return true;
    }

    @Override
    public boolean doesSectionHaveFooter(int section) {
        return false;
    }

    @Override
    public AgendaItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_event, parent, false);
        return new AgendaItemViewHolder(v);
    }

    @Override
    public AgendaHeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_header, parent, false);
        return new AgendaHeaderViewHolder(v);
    }

    @Override
    public void onBindItemViewHolder(SectioningAdapter.ItemViewHolder itemViewHolder,
                                     int section, int position, int viewType) {
        AgendaItemViewHolder holder = (AgendaItemViewHolder) itemViewHolder;
        CalendarEvent event = mDataSource.getEventItem(section, position);
        CalendarEvent.Formatter formatter =
                new CalendarEvent.Formatter(mDataSource.getCalendar(),
                        mDataSource.getEventTimeFormatter());

        GradientDrawable bgCalendarIndicator = (GradientDrawable) holder.calendarIndicator.getBackground();
        bgCalendarIndicator.setColor(getColorForCalendar(event.getCalendarId()));
        if (event.isAllDay()) {
            holder.beginTime.setText(R.string.text_all_day_event);
            holder.beginTime.setTypeface(null, Typeface.NORMAL);
            holder.endTime.setVisibility(GONE);
        } else {
            holder.beginTime.setText(formatter.beginTimeAsDisplayText(event));
            holder.endTime.setText(formatter.endTimeAsDisplayText(event));
            holder.beginTime.setVisibility(VISIBLE);
            holder.endTime.setVisibility(VISIBLE);
        }
        holder.title.setText(event.getTitle());
        holder.location.setText(event.getLocation());
    }

    private int getColorForCalendar(int id) {
        for (Map.Entry<Integer, List<Integer>> entry : mCalendarIndicators.entrySet()) {
            if (entry.getValue().contains(id)) return entry.getKey();
        }
        return 0xC33FAB;
    }

    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder headerViewHolder,
                                       int section, int viewType) {
        AgendaHeaderViewHolder holder = (AgendaHeaderViewHolder) headerViewHolder;
        holder.header.setText(mDataSource.getHeaderAsDisplayText(section));
    }

    /**
     * ViewHolder for the section header.
     */
    static final class AgendaHeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        final TextView header;

        public AgendaHeaderViewHolder(View headerView) {
            super(headerView);

            this.header = (TextView) headerView.findViewById(R.id.text_day_header);
        }
    }

    /**
     * ViewHolder for a single item.
     */
    static final class AgendaItemViewHolder extends SectioningAdapter.ItemViewHolder {
        final View calendarIndicator;
        final TextView beginTime;
        final TextView endTime;
        final TextView title;
        final TextView location;

        public AgendaItemViewHolder(View itemView) {
            super(itemView);

            this.calendarIndicator = itemView.findViewById(R.id.calendar_id_indicator);
            this.beginTime = (TextView) itemView.findViewById(R.id.text_begin_time);
            this.endTime = (TextView) itemView.findViewById(R.id.text_end_time);
            this.title = (TextView) itemView.findViewById(R.id.text_event_title);
            this.location = (TextView) itemView.findViewById(R.id.text_event_location);
        }
    }
}
