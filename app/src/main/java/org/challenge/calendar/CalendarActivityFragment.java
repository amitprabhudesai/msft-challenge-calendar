package org.challenge.calendar;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.timessquare.CalendarPickerView;

import java.util.Calendar;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class CalendarActivityFragment extends Fragment {

    private static final String TAG = CalendarActivityFragment.class.getSimpleName();

    public CalendarActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View resultView = inflater.inflate(R.layout.fragment_calendar, container, false);
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        CalendarPickerView calendar =
                (CalendarPickerView) resultView.findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today);
        calendar.setCellClickInterceptor(new CalendarPickerView.CellClickInterceptor() {
            @Override
            public boolean onCellClicked(Date date) {
                Log.d(TAG, "Clicked date: " + date.toString());
                startActivity(new Intent(getActivity(), AgendaActivity.class));
                return true;
            }
        });

        return resultView;
    }
}
