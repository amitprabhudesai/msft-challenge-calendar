package org.challenge.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.timessquare.CalendarPickerView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<CalendarEvent>> {

    private static final String TAG = MainActivityFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View contentView = inflater.inflate(R.layout.fragment_main, container, false);

        // calendar view
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        CalendarPickerView calendar =
                (CalendarPickerView) contentView.findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);

        // agenda view
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);

        AgendaViewAdapter agendaViewAdapter = new AgendaViewAdapter();
        mRecyclerView.setAdapter(agendaViewAdapter);
        agendaViewAdapter.shouldShowFooters(false);
        agendaViewAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<List<CalendarEvent>> onCreateLoader(int id, Bundle args) {
        return new CalendarEventsLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<CalendarEvent>> loader, List<CalendarEvent> events) {
        Log.d(TAG, "Load finished");
        //TODO(amit.prabhudesai) Set the data source for the adapter
    }

    @Override
    public void onLoaderReset(Loader<List<CalendarEvent>> loader) {
        Log.d(TAG, "Loader reset");
        //TODO(amit.prabhudesai) Clear the adapter data
    }
}
