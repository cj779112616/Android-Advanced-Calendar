package net.alexoro.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * User: UAS
 * Date: 19.06.13
 * Time: 23:26
 */
public class CalendarView extends View {

    private static final int DAYS_IN_WEEK = 7;  // columns
    private static final int WEEKS_TO_SHOW = 6; // rows

    public enum MonthTransition {
        VERTICAL,
        HORIZONTAL,
        NONE
    }

    static class DrawHelper {
        public Paint paint;
        public float measuredTextWidth;
    }

    static class MonthDrawArgs {
        public Rect area;
        public MonthDescriptor month;
    }

    static class WeekDrawArgs {
        public Rect area;
        public MonthDescriptor month;
        public int row;
    }

    static class DayDrawArgs {
        public Rect area;
        public MonthDescriptor month;
        public int row;
        public int column;
        public String value;
    }

    static class AnimationArgs {
        public boolean active;
        public Interpolator interpolator;
        public long startTime;
        public long duration;
        public MonthTransition transition;
        public int direction;
    }

    private Rect mGridSize;
    private Rect mDayCellSize;
    private MonthTransition mMonthTransition;

    private int mFirstDayOfWeek;
    private LocalDate mToday;
    private LocalDate mMonthToShow;

    private DrawHelper mDrawHelper;
    private MonthDrawArgs mMonthDrawArgs;
    private WeekDrawArgs mWeekDrawArgs;
    private DayDrawArgs mDayDrawArgs;
    private AnimationArgs mAnimationArgs;

    private Map<Integer, String> mMapDayToString;

    private float mDayTextSize;


    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initWithDefaults();
    }

    protected void initWithDefaults() {
        mMapDayToString = new HashMap<Integer, String>();
        for (int i = 1; i <= 31; i++) {
            mMapDayToString.put(i, String.valueOf(i));
        }

        mGridSize = new Rect();
        mDayCellSize = new Rect(0, 0, 40, 40);
        mMonthTransition = MonthTransition.NONE;

        mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
        mToday = new LocalDate();
        mMonthToShow = new LocalDate(mToday);

        mDrawHelper = new DrawHelper();
        mDrawHelper.paint = new Paint();

        mMonthDrawArgs = new MonthDrawArgs();
        mMonthDrawArgs.area = new Rect();
        mMonthDrawArgs.month = getMonthDescriptor(mToday, 0);

        mWeekDrawArgs = new WeekDrawArgs();
        mWeekDrawArgs.area = new Rect();
        mWeekDrawArgs.month = mMonthDrawArgs.month;
        mWeekDrawArgs.row = -1;

        mDayDrawArgs = new DayDrawArgs();
        mDayDrawArgs.area = new Rect();
        mDayDrawArgs.month = mMonthDrawArgs.month;
        mDayDrawArgs.row = -1;
        mDayDrawArgs.column = -1;

        mAnimationArgs = new AnimationArgs();
        mAnimationArgs.interpolator = new AccelerateDecelerateInterpolator();
        mAnimationArgs.duration = 400;

        mDayTextSize = 14f;
    }

    public void setMonthTransition(MonthTransition transition) {
        mMonthTransition = transition;
    }

    public MonthTransition getMonthTransition() {
        return mMonthTransition;
    }

    public void nextMonth() {
        if (mAnimationArgs.active) {
            return;
        }
        if (mMonthTransition == MonthTransition.NONE) {
            mMonthToShow = mMonthToShow.plusMonths(1);
        } else {
            setupAnimation(1);
        }
        invalidate();
    }

    public void previousMonth() {
        if (mAnimationArgs.active) {
            return;
        }
        if (mMonthTransition == MonthTransition.NONE) {
            mMonthToShow = mMonthToShow.minusMonths(1);
        } else {
            setupAnimation(-1);
        }
        invalidate();
    }

    public void show(LocalDate month) {
        mMonthToShow = new LocalDate(month);
        invalidate();
    }

    protected void setupAnimation(int direction) {
        mAnimationArgs.active = true;
        mAnimationArgs.startTime = System.currentTimeMillis();
        mAnimationArgs.direction = direction;
        mAnimationArgs.transition = mMonthTransition;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mGridSize.left = 0;
        mGridSize.right = mDayCellSize.width()*DAYS_IN_WEEK;
        mGridSize.top = 0;
        mGridSize.bottom = mDayCellSize.height() * WEEKS_TO_SHOW;
        setMeasuredDimension(mGridSize.width(), mGridSize.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // animate sliding
        if (mAnimationArgs.active) {
            executeTranslateAnimation(canvas);
        }

        // draw the monthes
        drawMonthes(canvas);
    }

    protected void executeTranslateAnimation(Canvas canvas) {
        // do animation via translation the canvas
        long animOffset = System.currentTimeMillis() - mAnimationArgs.startTime;
        float translate = mAnimationArgs.interpolator.getInterpolation((float)animOffset/mAnimationArgs.duration);
        translate *= -mAnimationArgs.direction;

        if (animOffset < mAnimationArgs.duration) {
            if (mAnimationArgs.transition == MonthTransition.HORIZONTAL) {
                canvas.translate((int)(translate * mGridSize.width()), 0);
            } else {
                canvas.translate(0, (int)(translate * mGridSize.height()));
            }
            invalidate();
        } else {
            if (mAnimationArgs.direction > 0) {
                mMonthToShow = mMonthToShow.plusMonths(1);
            } else {
                mMonthToShow = mMonthToShow.minusMonths(1);
            }
            mAnimationArgs.active = false;
        }
    }

    protected void drawMonthes(Canvas canvas) {
        // draw current month
        mMonthDrawArgs.area.set(0, 0, mGridSize.width(), mGridSize.height());
        mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 0);
        drawMonth(canvas, mMonthDrawArgs);

        if (mAnimationArgs.transition == MonthTransition.HORIZONTAL) {
            // draw previous month
            mMonthDrawArgs.area.set(-mGridSize.width(), 0, 0, mGridSize.height());
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, -1);
            drawMonth(canvas, mMonthDrawArgs);

            // draw next month
            mMonthDrawArgs.area.set(mGridSize.width(), 0, mGridSize.width()*2, mGridSize.height());
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 1);
            drawMonth(canvas, mMonthDrawArgs);
        }

        if (mAnimationArgs.transition == MonthTransition.VERTICAL) {
            // draw previous month
            mMonthDrawArgs.area.set(0, -mGridSize.height(), mGridSize.width(), 0);
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, -1);
            drawMonth(canvas, mMonthDrawArgs);

            // draw next month
            mMonthDrawArgs.area.set(0, mGridSize.height(), mGridSize.width(), mGridSize.height()*2);
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 1);
            drawMonth(canvas, mMonthDrawArgs);
        }
    }

    protected void drawMonth(Canvas canvas, MonthDrawArgs args) {
        for (int i = 0; i < args.month.getRowsCount(); i++) {
            mWeekDrawArgs.area.set(
                    args.area.left,
                    args.area.top + i * mDayCellSize.height(),
                    args.area.right,
                    args.area.top + i * mDayCellSize.height() + mDayCellSize.height());
            mWeekDrawArgs.month = args.month;
            mWeekDrawArgs.row = i;
            drawWeek(canvas, mWeekDrawArgs);
        }
    }

    protected void drawWeek(Canvas canvas, WeekDrawArgs args) {
        for (int i = 0; i < args.month.getColumnsCount(); i++) {
            mDayDrawArgs.area.set(
                    args.area.left + i * mDayCellSize.width(),
                    args.area.top,
                    args.area.left + i * mDayCellSize.width() + mDayCellSize.width(),
                    args.area.bottom);
            mDayDrawArgs.month = args.month;
            mDayDrawArgs.row = args.row;
            mDayDrawArgs.column = i;
            mDayDrawArgs.value = mMapDayToString.get(args.month.getDayAt(args.row, mDayDrawArgs.column));
            drawDay(canvas, mDayDrawArgs);
        }
    }

    protected void drawDay(Canvas canvas, DayDrawArgs args) {
//        mDrawHelper.paint.setColor(mRandom.nextInt());
        mDrawHelper.paint.setColor(Color.DKGRAY);
        canvas.drawRect(
                args.area.left + 1,
                args.area.top + 1,
                args.area.right - 1,
                args.area.bottom - 1,
                mDrawHelper.paint);

        mDrawHelper.paint.setAntiAlias(true);
        mDrawHelper.paint.setStyle(Paint.Style.FILL);
        mDrawHelper.paint.setColor(Color.WHITE);
        mDrawHelper.paint.setTextSize(mDayTextSize);
        mDrawHelper.measuredTextWidth = mDrawHelper.paint.measureText(args.value);
        canvas.drawText(
                args.value,
                args.area.centerX() - mDrawHelper.measuredTextWidth/2,
                args.area.centerY() + mDayTextSize/2 - 2, // трик-хуик, to make it really in center. getTextBounds not helps
                mDrawHelper.paint);
    }


    protected MonthDescriptor getMonthDescriptor(LocalDate month, int monthOffset) {
        if (monthOffset == 0) {
            return new MonthDescriptor(month.getYear(), month.getMonthOfYear() - 1, mFirstDayOfWeek);
        } else {
            return getMonthDescriptor(month.plusMonths(monthOffset), 0);
        }
    }

}