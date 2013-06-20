package tests;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import net.alexoro.calendar.CalendarView;
import net.alexoro.calendar.R;
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
        vCalendarView.setMonthTransition(CalendarView.MonthTransition.HORIZONTAL);

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
                CalendarView.MonthTransition mt = vCalendarView.getMonthTransition();
                if (mt == CalendarView.MonthTransition.NONE) {
                    vCalendarView.setMonthTransition(CalendarView.MonthTransition.HORIZONTAL);
                } else if (mt == CalendarView.MonthTransition.HORIZONTAL) {
                    vCalendarView.setMonthTransition(CalendarView.MonthTransition.VERTICAL);
                } else {
                    vCalendarView.setMonthTransition(CalendarView.MonthTransition.NONE);
                }
            }
        });
    }

}