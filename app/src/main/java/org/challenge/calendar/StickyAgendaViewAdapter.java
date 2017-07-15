package org.challenge.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.zakariya.stickyheaders.SectioningAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Adapter for the agenda view with sticky section headers.
 */
public class StickyAgendaViewAdapter extends SectioningAdapter {

    private static final String TAG = StickyAgendaViewAdapter.class.getSimpleName();

    private AgendaDataSource mDataSource;

    public StickyAgendaViewAdapter() {
    }

    public void setDataSource(AgendaDataSource dataSource) {
        mDataSource = dataSource;
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
        if (event.isAllDay()) {
            holder.beginTime.setVisibility(GONE);
            holder.endTime.setVisibility(GONE);
            holder.allDay.setText(R.string.text_all_day_event);
            holder.allDay.setVisibility(VISIBLE);
        } else {
            holder.allDay.setVisibility(GONE);
            holder.beginTime.setText(formatter.beginTimeAsDisplayText(event));
            holder.endTime.setText(formatter.endTimeAsDisplayText(event));
            holder.beginTime.setVisibility(VISIBLE);
            holder.endTime.setVisibility(VISIBLE);
        }
        holder.title.setText(event.getTitle());
        holder.location.setText(event.getLocation());
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
        final TextView allDay;
        final TextView beginTime;
        final TextView endTime;
        final TextView title;
        final TextView location;

        public AgendaItemViewHolder(View itemView) {
            super(itemView);

            this.allDay = (TextView) itemView.findViewById(R.id.text_all_day);
            this.beginTime = (TextView) itemView.findViewById(R.id.text_begin_time);
            this.endTime = (TextView) itemView.findViewById(R.id.text_end_time);
            this.title = (TextView) itemView.findViewById(R.id.text_event_title);
            this.location = (TextView) itemView.findViewById(R.id.text_event_location);
        }
    }
}
