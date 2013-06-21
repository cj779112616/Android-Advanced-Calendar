package tests;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import net.alexoro.calendar.CalendarGridView;
import net.alexoro.calendar.R;
import org.joda.time.LocalDate;

/**
 * User: UAS
 * Date: 19.06.13
 * Time: 23:25
 */
public class CalendarActivity extends Activity {

    private CalendarGridView mVCalendarGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests_calendar_grid);

        mVCalendarGridView = (CalendarGridView) findViewById(R.id.calendar);
        mVCalendarGridView.setMonthTransition(CalendarGridView.MonthTransition.HORIZONTAL);
        mVCalendarGridView.setEnabledRange(new LocalDate(2013, 4, 10), new LocalDate(2013, 8, 20));
        mVCalendarGridView.setSelectedRange(new LocalDate(2013, 6, 4), new LocalDate(2013, 6, 5));
        mVCalendarGridView.setOnDateClickListener(new CalendarGridView.OnDateClickListener() {
            @Override
            public void onClick(LocalDate date) {
                Toast.makeText(CalendarActivity.this, date.toDate().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVCalendarGridView.previousMonth();
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVCalendarGridView.nextMonth();
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVCalendarGridView.show(new LocalDate());
            }
        });
        findViewById(R.id.switcher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarGridView.MonthTransition mt = mVCalendarGridView.getMonthTransition();
                if (mt == CalendarGridView.MonthTransition.NONE) {
                    mVCalendarGridView.setMonthTransition(CalendarGridView.MonthTransition.HORIZONTAL);
                } else if (mt == CalendarGridView.MonthTransition.HORIZONTAL) {
                    mVCalendarGridView.setMonthTransition(CalendarGridView.MonthTransition.VERTICAL);
                } else {
                    mVCalendarGridView.setMonthTransition(CalendarGridView.MonthTransition.NONE);
                }
            }
        });
    }

}