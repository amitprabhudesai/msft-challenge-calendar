package org.challenge.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgendaViewAdapter extends SectionedRecyclerViewAdapter<AgendaViewAdapter.AgendaItemViewHolder> {

    private static final String TAG = AgendaViewAdapter.class.getSimpleName();

    private final Map<String, List<CalendarEvent>> mEvents;
    private final Map<Integer, String> mSectionHeaders;

    public AgendaViewAdapter() {
        mSectionHeaders = new HashMap<>();
        mEvents = new HashMap<>();
    }

    public void setDataSource(AgendaDataSource dataSource) {
        mEvents.putAll(dataSource.events());
        int i = 0;
        for (final String day: dataSource.days()) {
            mSectionHeaders.put(i++, day);
        }
    }

    @Override
    public int getSectionCount() {
        return mEvents.size();
    }

    @Override
    public int getItemCount(int sectionIndex) {
        return mEvents.get(mSectionHeaders.get(sectionIndex)).size();
    }

    @Override
    public void onBindHeaderViewHolder(AgendaItemViewHolder holder, int section, boolean expanded) {
        holder.header.setText(mSectionHeaders.get(section));
    }

    @Override
    public void onBindViewHolder(AgendaItemViewHolder holder, int section, int relativePosition, int absolutePosition) {
        CalendarEvent event = mEvents.get(mSectionHeaders.get(section)).get(relativePosition);
        holder.beginTime.setText(event.getBeginTime());
        holder.endTime.setText(event.getEndTime());
        holder.title.setText(event.getTitle());
        holder.location.setText(event.getLocation());
    }

    @Override
    public void onBindFooterViewHolder(AgendaItemViewHolder holder, int section) {
    }

    @Override
    public AgendaItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            default:
            case VIEW_TYPE_ITEM:
                layoutRes = R.layout.agenda_event;
                break;
            case VIEW_TYPE_HEADER:
                layoutRes = R.layout.agenda_header;
                break;
        }
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new AgendaItemViewHolder(v);
    }

    static final class AgendaItemViewHolder extends SectionedViewHolder {

        final TextView header;
        final TextView beginTime;
        final TextView endTime;
        final TextView title;
        final TextView location;

        public AgendaItemViewHolder(View headerView) {
            super(headerView);

            this.header = (TextView) headerView.findViewById(R.id.text_day_header);
            this.beginTime = (TextView) itemView.findViewById(R.id.text_begin_time);
            this.endTime = (TextView) itemView.findViewById(R.id.text_end_time);
            this.title = (TextView) itemView.findViewById(R.id.text_event_title);
            this.location = (TextView) itemView.findViewById(R.id.text_event_location);
        }
    }
}
