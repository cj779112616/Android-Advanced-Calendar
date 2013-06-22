package net.alexoro.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.joda.time.LocalDate;

/**
 * User: UAS
 * Date: 21.06.13
 * Time: 5:10
 */
public class CalendarView extends LinearLayout {

    private CalendarGridView2 vGrid;

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(R.layout.nac__header, this, true);

        vGrid = new CalendarGridView2(getContext());
        vGrid.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addView(vGrid);

        LinearLayout vDaysGroup = (LinearLayout) findViewById(R.id.days);
        for (int i = 0; i < 7; i++) {
            TextView tv = (TextView) mInflater.inflate(R.layout.nac__day, vDaysGroup, false);
            tv.setLayoutParams(new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1
            ));
            vDaysGroup.addView(tv);
        }
    }


    public void setMonthTransition(MonthTransition transition) {
        //vGrid.setMonthTransition(transition);
    }

    public MonthTransition getMonthTransition() {
        return null;
    }

    public OnDateClickListener getOnDateClickListener() {
        return vGrid.getOnDateClickListener();
    }

    public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
        vGrid.setOnDateClickListener(onDateClickListener);
    }

    public void setEnabledRange(LocalDate startIncluding, LocalDate endIncluding) {
        vGrid.setEnabledRange(startIncluding, endIncluding);
    }

    public LocalDate getEnabledRangeStart() {
        return vGrid.getEnabledRangeStart();
    }

    public LocalDate getEnabledRangeEnd() {
        return vGrid.getEnabledRangeEnd();
    }

    public void setSelectedRange(LocalDate startIncluding, LocalDate endIncluding) {
        vGrid.setSelectedRange(startIncluding, endIncluding);
    }

    public LocalDate getSelectedRangeStart() {
        return vGrid.getSelectedRangeStart();
    }

    public LocalDate getSelectedRangeEnd() {
        return vGrid.getSelectedRangeEnd();
    }

    public void nextMonth() {
        vGrid.nextMonth();
    }

    public void previousMonth() {
        vGrid.previousMonth();
    }

    public void show(LocalDate month) {
        vGrid.show(month);
    }

}