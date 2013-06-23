package net.alexoro.calendar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.joda.time.LocalDate;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * User: UAS
 * Date: 21.06.13
 * Time: 5:10
 */
public class CalendarView extends LinearLayout {

    private TextView vMonthName;
    private CalendarGridView2 vGrid;
    private LocalDate mMonthToShow;
    private SimpleDateFormat mMonthFormat;


    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(R.layout.nac__header, this, true);

        mMonthFormat = new SimpleDateFormat("LLLL yyyy");
        mMonthToShow = new LocalDate();

        vMonthName = (TextView) findViewById(R.id.month_name);
        updateMonthName();

        vGrid = new CalendarGridView2(getContext());
        vGrid.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addView(vGrid);

        int day = Calendar.getInstance().getFirstDayOfWeek();
        String[] dayNames = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();

        LinearLayout vDaysGroup = (LinearLayout) findViewById(R.id.days);
        for (int i = 0; i < 7; i++) {
            TextView tv = (TextView) mInflater.inflate(R.layout.nac__day, vDaysGroup, false);
            tv.setLayoutParams(new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1
            ));
            vDaysGroup.addView(tv);

            if (day == 8) {
                day = 1;
            }
            tv.setText(dayNames[day]);
            day++;
        }
    }


    public void setDayCellSize(int width, int height) {
        vGrid.setDayCellSize(width, height);
    }

    public int getDayCellWidth() {
        return vGrid.getDayCellWidth();
    }

    public int getDayCellHeight() {
        return vGrid.getDayCellHeight();
    }

    public void setMonthTransition(MonthTransition transition) {
        vGrid.setMonthTransition(transition);
    }

    public MonthTransition getMonthTransition() {
        return vGrid.getMonthTransition();
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
        mMonthToShow = mMonthToShow.plusMonths(1);
        updateMonthName();
    }

    public void previousMonth() {
        vGrid.previousMonth();
        mMonthToShow = mMonthToShow.minusMonths(1);
        updateMonthName();
    }

    public void show(LocalDate month) {
        vGrid.show(month);
    }

    protected void updateMonthName() {
        vMonthName.setText(
                mMonthFormat.format(mMonthToShow.toDate()));
    }

}