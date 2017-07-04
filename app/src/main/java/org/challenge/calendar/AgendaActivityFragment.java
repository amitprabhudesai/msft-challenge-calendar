package org.challenge.calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class AgendaActivityFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private AgendaViewAdapter mEventsAdapter;

    public AgendaActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agenda, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEventsAdapter = new AgendaViewAdapter();
        mRecyclerView.setAdapter(mEventsAdapter);
        mEventsAdapter.shouldShowFooters(false);
        mEventsAdapter.notifyDataSetChanged();
    }
}
