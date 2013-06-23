package tests;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import net.alexoro.calendar.CalendarGridView;
import net.alexoro.calendar.MonthTransition;
import net.alexoro.calendar.OnDateClickListener;
import net.alexoro.calendar.R;
import org.joda.time.LocalDate;

/**
 * User: UAS
 * Date: 19.06.13
 * Time: 23:25
 */
public class CalendarGridActivity extends Activity {

    private CalendarGridView vCalendarGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests_calendar_grid);

        vCalendarGridView = (CalendarGridView) findViewById(R.id.calendar);
        vCalendarGridView.setMonthTransition(MonthTransition.HORIZONTAL);
        vCalendarGridView.setEnabledRange(new LocalDate(2013, 4, 10), new LocalDate(2013, 8, 20));
        vCalendarGridView.setSelectedRange(new LocalDate(2013, 6, 4), new LocalDate(2013, 6, 5));
        vCalendarGridView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onClick(LocalDate date) {
                Toast.makeText(CalendarGridActivity.this, date.toDate().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vCalendarGridView.previousMonth();
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vCalendarGridView.nextMonth();
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vCalendarGridView.show(new LocalDate());
            }
        });
        findViewById(R.id.switcher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MonthTransition mt = vCalendarGridView.getMonthTransition();
                if (mt == MonthTransition.NONE) {
                    vCalendarGridView.setMonthTransition(MonthTransition.HORIZONTAL);
                } else if (mt == MonthTransition.HORIZONTAL) {
                    vCalendarGridView.setMonthTransition(MonthTransition.VERTICAL);
                } else {
                    vCalendarGridView.setMonthTransition(MonthTransition.NONE);
                }
            }
        });
    }

}