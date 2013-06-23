package net.alexoro.calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.*;
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

    private static final int ACTION_MASK = 255; // MotionEvent.ACTION_MASK was introduce only in API #5

    private TextView vMonthName;
    private LinearLayout vDaysGroup;
    private CalendarGridView vGrid;

    private LocalDate mMonthToShow;
    private Pair<LocalDate, LocalDate> mEnabledRange;
    private boolean mAllowMonthChangeByUi;

    private long mTouchEventStartTime;
    private SimpleDateFormat mMonthFormat;
    private OnDateChangedListener mUserOnDateChangedListener;


    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mMonthToShow = new LocalDate();
        mEnabledRange = new Pair<LocalDate, LocalDate>(null, null);
        mTouchEventStartTime = -1L;
        mMonthFormat = new SimpleDateFormat("LLLL yyyy");
        mUserOnDateChangedListener = null;

        setOrientation(VERTICAL);
        initUi();
        initUiEvents();

        mAllowMonthChangeByUi = true;
        updateMonthName();
        updateEnabledRange();
    }

    protected void initUi() {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        mInflater.inflate(R.layout.nac__header, this, true);

        vMonthName = (TextView) findViewById(R.id.month_name);
        vDaysGroup = (LinearLayout) findViewById(R.id.days);

        vGrid = new CalendarGridView(getContext());
        vGrid.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        addView(vGrid);

        int day = Calendar.getInstance().getFirstDayOfWeek();
        String[] dayNames = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();

        for (int i = 0; i < 7; i++) {
            TextView tv = (TextView) mInflater.inflate(R.layout.nac__day, vDaysGroup, false);
            tv.setLayoutParams(new LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            vDaysGroup.addView(tv);

            if (day == 8) {
                day = 1;
            }
            tv.setText(dayNames[day]);
            day++;
        }
    }

    protected void initUiEvents() {
        vGrid.setOnDateChangedListener(new OnDateChanged());
    }

    public void setAllowMonthChangeByUi(boolean state) {
        mAllowMonthChangeByUi = state;
        updateEnabledRange();
    }

    public boolean getAllowMonthChangeByUi() {
        return mAllowMonthChangeByUi;
    }


    protected void updateEnabledRange() {
        if (mAllowMonthChangeByUi) {
            Drawable left = null, right = null;
            if (mEnabledRange.first != null
                    && compareByMonth(mMonthToShow.minusMonths(1), mEnabledRange.first) >= 0) {
                left = getResources().getDrawable(R.drawable.nac__arrow_left);
            }
            if (mEnabledRange.second != null
                    && compareByMonth(mMonthToShow.plusMonths(1), mEnabledRange.second) <= 0) {
                right = getResources().getDrawable(R.drawable.nac__arrow_right);
            }
            vMonthName.setCompoundDrawablesWithIntrinsicBounds(left, null, right, null);
        } else {
            vMonthName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    protected void updateMonthName() {
        vMonthName.setText(
                mMonthFormat.format(mMonthToShow.toDate()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchEventStartTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - mTouchEventStartTime < (long) ViewConfiguration.getLongPressTimeout()) {
                    mTouchEventStartTime = -1;
                    onClickXY(event.getX(), event.getY());
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    protected void onClickXY(float x, float y) {
        int width = getWidth();
        if (x < width/3
                && mAllowMonthChangeByUi
                && mEnabledRange.first != null
                && compareByMonth(mMonthToShow.minusMonths(1), mEnabledRange.first) >= 0) {
            previousMonth();
        } else if (x > width*2/3
                && mAllowMonthChangeByUi
                && mEnabledRange.second != null
                && compareByMonth(mMonthToShow.plusMonths(1), mEnabledRange.second) <= 0) {
            nextMonth();
        }
    }

    protected int compareByMonth(LocalDate d1, LocalDate d2) {
        d1 = new LocalDate(d1.getYear(), d1.getMonthOfYear(), 1);
        d2 = new LocalDate(d2.getYear(), d2.getMonthOfYear(), 1);
        return d1.compareTo(d2);
    }

    class OnDateChanged implements OnDateChangedListener {
        @Override
        public void onChanged(LocalDate month) {
            mMonthToShow = month;
            updateMonthName();
            updateEnabledRange();
            if (mUserOnDateChangedListener != null) {
                mUserOnDateChangedListener.onChanged(month);
            }
        }
    }


    //region Facade methods with overriding

    public void setEnabledRange(LocalDate startIncluding, LocalDate endIncluding) {
        mEnabledRange = new Pair<LocalDate, LocalDate>(
                startIncluding,
                endIncluding);
        vGrid.setEnabledRange(startIncluding, endIncluding);
        updateEnabledRange();
    }

    public LocalDate getEnabledRangeStart() {
        return vGrid.getEnabledRangeStart();
    }

    //endregion


    //region Facade methods

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


    public OnDateChangedListener getOnDateChangedListener() {
        return mUserOnDateChangedListener;
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mUserOnDateChangedListener = onDateChangedListener;
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

    //endregion

}