package net.alexoro.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    }

    static class AnimationArgs {
        private Interpolator interpolator;
        private long startTime;
        private long endDime;
    }

    private Rect mGridSize;
    private Rect mDayCellSize;

    private int mFirstDayOfWeek;
    private LocalDate mToday;
    private LocalDate mMonthToShow;

    private MonthTransition mMonthTransition;
    private Random mRandom;

    private DrawHelper mDrawHelper;
    private MonthDrawArgs mMonthDrawArgs;
    private WeekDrawArgs mWeekDrawArgs;
    private DayDrawArgs mDayDrawArgs;
    private AnimationArgs mAnimationArgs;

    private Map<Integer, String> mMapDayToString;


    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initWithDefaults();
        setupAnimation();
    }

    protected void initWithDefaults() {
        mMapDayToString = new HashMap<Integer, String>();
        for (int i = 1; i <= 31; i++) {
            mMapDayToString.put(i, String.valueOf(i));
        }

        mGridSize = new Rect();
        mDayCellSize = new Rect(0, 0, 40, 40);

        mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
        mToday = new LocalDate();
        mMonthToShow = new LocalDate(mToday);
        mMonthTransition = MonthTransition.HORIZONTAL;
        mRandom = new Random(System.currentTimeMillis());

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
    }

    protected void setupAnimation() {
        mAnimationArgs = new AnimationArgs();
        mAnimationArgs.interpolator = new LinearInterpolator();
        mAnimationArgs.startTime = System.currentTimeMillis();
        mAnimationArgs.endDime = System.currentTimeMillis() + 5000;
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
        executeTranslateAnimation(canvas);

        // draw the monthes
        drawMonthes(canvas);
    }

    protected void executeTranslateAnimation(Canvas canvas) {
        // do animation via translation the canvas
        long animDuration = mAnimationArgs.endDime - mAnimationArgs.startTime;
        long animOffset = System.currentTimeMillis() - mAnimationArgs.startTime;
        float translate = mAnimationArgs.interpolator.getInterpolation((float)animOffset/animDuration);
        if (translate > 1f) {
            translate = 1f;
        }

        if (animOffset < animDuration) {
            canvas.translate((int)(translate * mGridSize.width()), 0);
            invalidate();
        } else {
            mMonthToShow = mMonthToShow.minusMonths(1);
        }
    }

    protected void drawMonthes(Canvas canvas) {
        // draw current month
        mMonthDrawArgs.area.set(0, 0, mGridSize.width(), mGridSize.height());
        mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 0);
        drawMonth(canvas, mMonthDrawArgs);

        if (mMonthTransition == MonthTransition.HORIZONTAL) {
            // draw previous month
            mMonthDrawArgs.area.set(-mGridSize.width(), 0, 0, mGridSize.height());
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, -1);
            drawMonth(canvas, mMonthDrawArgs);

            // draw next month
            mMonthDrawArgs.area.set(mGridSize.width(), mGridSize.width()*2, 0, mGridSize.height());
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 1);
            drawMonth(canvas, mMonthDrawArgs);
        }

        if (mMonthTransition == MonthTransition.VERTICAL) {
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
            drawDay(canvas, mDayDrawArgs);
        }
    }

    protected void drawDay(Canvas canvas, DayDrawArgs args) {
        mDrawHelper.paint.setColor(mRandom.nextInt());
        canvas.drawRect(args.area, mDrawHelper.paint);
        mDrawHelper.paint.setColor(Color.WHITE);
        int day = args.month.getDayAt(args.row, args.column);
        canvas.drawText(
                mMapDayToString.get(args.month.getDayAt(args.row, args.column)),
                args.area.centerX(),
                args.area.centerY(),
                mDrawHelper.paint);
    }


    protected MonthDescriptor getMonthDescriptor(LocalDate month, int monthOffset) {
        if (monthOffset == 0) {
            return new MonthDescriptor(month.getYear(), month.getMonthOfYear() - 1, mFirstDayOfWeek);
        } else {
            if (monthOffset > 0) {
                return getMonthDescriptor(month.plusMonths(monthOffset), 0);
            } else {
                return getMonthDescriptor(month.minusMonths(monthOffset), 0);
            }
        }
    }


    /* Animations
        private Interpolator mIn;
        private long mAnimStart;
        private long mAnimEnd;

        long animDuration = mAnimEnd - mAnimStart;
        long animOffset = System.currentTimeMillis() - mAnimStart;
        float translate = mIn.getInterpolation((float)animOffset/animDuration);
        if (translate > 1f) {
            translate = 1f;
        }
        mTranslateX = (int)(translate * mGridWidth);
        canvas.translate(mTranslateX, 0);

        if (animOffset < animDuration) {
            invalidate();
        }
     */


}