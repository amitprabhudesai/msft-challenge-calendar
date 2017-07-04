package org.challenge.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;

import java.util.Locale;

public class AgendaViewAdapter extends SectionedRecyclerViewAdapter<AgendaViewAdapter.AgendaItemViewHolder> {

    @Override
    public int getSectionCount() {
        return 30;
    }

    @Override
    public int getItemCount(int sectionIndex) {
        return 3;
    }

    @Override
    public void onBindHeaderViewHolder(AgendaItemViewHolder holder, int section, boolean expanded) {
        holder.header.setText(String.format(Locale.US, "July %d", section+1));
    }

    @Override
    public void onBindViewHolder(AgendaItemViewHolder holder, int section, int relativePosition, int absolutePosition) {
        holder.beginTime.setText("09:00");
        holder.endTime.setText("10:00");
        holder.title.setText("Video 2.0 + WebView");
        holder.location.setText("IN-BLR-7F-Ruzzle(4)");
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
