package net.alexoro.calendar.samples;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.alexoro.calendar.CalendarView;
import net.alexoro.calendar.SelectionType;
import org.joda.time.LocalDate;
import org.joda.time.Months;

/**
 * User: UAS
 * Date: 19.06.13
 * Time: 23:25
 */
public class CalendarListActivity extends Activity {

    private ListView vList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests_list_calendar);
        vList = (ListView) findViewById(R.id.list);

        LocalDate start = new LocalDate().minusMonths(2);
        LocalDate end = new LocalDate().plusMonths(2);
        MonthsAdapter adapter = new MonthsAdapter(this, start, end);
        vList.setAdapter(adapter);
    }

    class MonthsAdapter extends ArrayAdapter<LocalDate> {
        private LocalDate mStart;
        private LocalDate mEnd;
        public MonthsAdapter(Context context, LocalDate start, LocalDate end) {
            super(context, -1);
            mStart = start;
            mEnd = end;
        }

        @Override
        public LocalDate getItem(int position) {
            return mStart.plusMonths(position);
        }

        @Override
        public int getCount() {
            return Months.monthsBetween(mStart, mEnd).getMonths();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CalendarView cv;
            if (convertView == null) {
                cv = (CalendarView) getLayoutInflater().inflate(R.layout.tests_list_calendar_item, parent, false);
                cv.setSelectionType(SelectionType.NONE);
                cv.setEnabledRange(mStart, mEnd);
                cv.setAllowMonthChangeByUi(false);
            } else {
                cv = (CalendarView) convertView;
            }

            LocalDate target = getItem(position);
            cv.show(target);
            return cv;
        }

    }

}