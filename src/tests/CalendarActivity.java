package tests;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import net.alexoro.calendar.*;
import org.joda.time.LocalDate;

/**
 * User: UAS
 * Date: 19.06.13
 * Time: 23:25
 */
public class CalendarActivity extends Activity {

    private CalendarView vCalendarView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests_calendar);

        vCalendarView = (CalendarView) findViewById(R.id.calendar);
        vCalendarView.setMonthTransition(MonthTransition.HORIZONTAL);
        vCalendarView.setEnabledRange(new LocalDate(2013, 5, 28), new LocalDate(2013, 7, 12));
        vCalendarView.setSelectedRange(new LocalDate(2013, 6, 4), new LocalDate(2013, 6, 5));
        vCalendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(LocalDate date) {
                Toast.makeText(CalendarActivity.this, date.toDate().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vCalendarView.previousMonth();
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vCalendarView.nextMonth();
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vCalendarView.show(new LocalDate());
            }
        });
        findViewById(R.id.switcher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonthTransition mt = vCalendarView.getMonthTransition();
                if (mt == MonthTransition.NONE) {
                    vCalendarView.setMonthTransition(MonthTransition.HORIZONTAL);
                } else if (mt == MonthTransition.HORIZONTAL) {
                    vCalendarView.setMonthTransition(MonthTransition.VERTICAL);
                } else {
                    vCalendarView.setMonthTransition(MonthTransition.NONE);
                }
            }
        });
    }

}