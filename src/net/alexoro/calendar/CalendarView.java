package net.alexoro.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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

    public enum MonthTransition {
        VERTICAL,
        HORIZONTAL,
        NONE
    }

    public interface OnDateClickListener {
        void onClick(LocalDate date);
    }


    private static final int DAYS_IN_WEEK = 7;  // columns
    private static final int WEEKS_TO_SHOW = 6; // rows
    private static final int ACTION_MASK = 255; // MotionEvent.ACTION_MASK was introduce only in API #5

    static class MonthDrawHelper {
        public Rect area;
        public MonthDescriptor month;
    }

    static class WeekDrawHelper {
        public Rect area;
        public MonthDescriptor month;
        public int row;
    }

    static class DayDrawHelper {
        public Rect area;
        public MonthDescriptor month;
        public int row;
        public int column;
        public String value;

        public CellDrawInfo cellDrawInfo;
        public DayType dayType;

        public float textSize;
        public int textColor;
        public Bitmap background;

        public Paint cellBackgroundPaint;
        public Paint cellTextPaint;
        public float measuredTextWidth;
    }

    static enum DayType {
        TODAY,
        THIS_MONTH,
        NEIGHBOUR_MONTH
    }

    static class AnimationHelper {
        public boolean active;
        public Interpolator interpolator;
        public long startTime;
        public long duration;
        public MonthTransition transition;
        public int direction;
        public Bitmap previous;
        public Bitmap current;
        public Bitmap next;
        public Paint paint;
    }

    static class CellDrawInfo {
        public float textSize;
        public StateListDrawable drawable;
        public ColorStateList textColor;
    }


    private Rect mGridSize;
    private Rect mDayCellSize;
    private MonthTransition mMonthTransition;

    private int mFirstDayOfWeek;
    private LocalDate mToday;
    private LocalDate mMonthToShow;

    private MonthDrawHelper mMonthDrawHelper;
    private WeekDrawHelper mWeekDrawHelper;
    private DayDrawHelper mDayDrawHelper;
    private AnimationHelper mAnimationHelper;

    private Map<Integer, String> mMapDayToString;
    private long mTouchEventStartTime;
    private Cell mCurrentlyPressedCell;
    private OnDateClickListener mOnDateClickListener;

    private CellDrawInfo mThisMonthCellInfo;
    private CellDrawInfo mNeighbourMonthCellInfo;
    private CellDrawInfo mTodayCellInfo;


    //region Construction

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mMapDayToString = new HashMap<Integer, String>();
        for (int i = 1; i <= 31; i++) {
            mMapDayToString.put(i, String.valueOf(i));
        }
        mTouchEventStartTime = -1;
        mCurrentlyPressedCell = new Cell(-1, -1);
        mOnDateClickListener = null;

        initWithDefaults();
    }

    protected void initWithDefaults() {
        mGridSize = new Rect();
        mDayCellSize = new Rect(0, 0, 40, 40);
        mMonthTransition = MonthTransition.NONE;

        mFirstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
        mToday = new LocalDate();
        mMonthToShow = new LocalDate(mToday);

        mMonthDrawHelper = new MonthDrawHelper();
        mMonthDrawHelper.area = new Rect();
        mMonthDrawHelper.month = getMonthDescriptor(mToday, 0);

        mWeekDrawHelper = new WeekDrawHelper();
        mWeekDrawHelper.area = new Rect();
        mWeekDrawHelper.month = mMonthDrawHelper.month;
        mWeekDrawHelper.row = -1;

        mDayDrawHelper = new DayDrawHelper();
        mDayDrawHelper.area = new Rect();
        mDayDrawHelper.month = mMonthDrawHelper.month;
        mDayDrawHelper.row = -1;
        mDayDrawHelper.column = -1;
        mDayDrawHelper.cellBackgroundPaint = new Paint();
        mDayDrawHelper.cellTextPaint = new Paint();

        mAnimationHelper = new AnimationHelper();
        mAnimationHelper.interpolator = new AccelerateDecelerateInterpolator();
        mAnimationHelper.duration = 700;
        mAnimationHelper.paint = new Paint();
        mAnimationHelper.paint.setAntiAlias(true);
        mAnimationHelper.paint.setStyle(Paint.Style.FILL);

        mThisMonthCellInfo = new CellDrawInfo();
        mThisMonthCellInfo.textSize = 14f;
        mThisMonthCellInfo.drawable = (StateListDrawable) getResources().getDrawable(R.drawable.nac__bg_this_month);
        mThisMonthCellInfo.textColor = getResources().getColorStateList(R.color.nac__this_month);

        mNeighbourMonthCellInfo = new CellDrawInfo();
        mNeighbourMonthCellInfo.textSize = 14f;
        mNeighbourMonthCellInfo.drawable = (StateListDrawable) getResources().getDrawable(R.drawable.nac__bg_neighbour_month);
        mNeighbourMonthCellInfo.textColor = getResources().getColorStateList(R.color.nac__neighbour_month);

        mTodayCellInfo = new CellDrawInfo();
        mTodayCellInfo.textSize = 14f;
        mTodayCellInfo.drawable = (StateListDrawable) getResources().getDrawable(R.drawable.nac__bg_today);
        mTodayCellInfo.textColor = getResources().getColorStateList(R.color.nac__today);
    }

    //endregion


    // region Set & Get properties

    public void setMonthTransition(MonthTransition transition) {
        mMonthTransition = transition;
    }

    public MonthTransition getMonthTransition() {
        return mMonthTransition;
    }

    public OnDateClickListener getOnDateClickListener() {
        return mOnDateClickListener;
    }

    public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
        mOnDateClickListener = onDateClickListener;
    }

    //endregion


    //region Change data to show

    public void nextMonth() {
        if (mAnimationHelper.active) {
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
        if (mAnimationHelper.active) {
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

    //endregion


    //region Animation params

    protected void setupAnimation(int direction) {
        mAnimationHelper.active = true;
        mAnimationHelper.startTime = System.currentTimeMillis();
        mAnimationHelper.direction = direction;
        mAnimationHelper.transition = mMonthTransition;

        mMonthDrawHelper.area.set(mGridSize);
        mMonthDrawHelper.month = getMonthDescriptor(mMonthToShow, -1);
        mAnimationHelper.previous = createBitmapCacheAsGridSize();
        createBitmapCacheForMonth(mMonthDrawHelper, mAnimationHelper.previous);

        mMonthDrawHelper.area.set(mGridSize);
        mMonthDrawHelper.month = getMonthDescriptor(mMonthToShow, 0);
        mAnimationHelper.current = createBitmapCacheAsGridSize();
        createBitmapCacheForMonth(mMonthDrawHelper, mAnimationHelper.current);

        mMonthDrawHelper.area.set(mGridSize);
        mMonthDrawHelper.month = getMonthDescriptor(mMonthToShow, 1);
        mAnimationHelper.next = createBitmapCacheAsGridSize();
        createBitmapCacheForMonth(mMonthDrawHelper, mAnimationHelper.next);
    }

    protected void cleanAnimationBitmaps() {
        if (mAnimationHelper.previous != null) {
            mAnimationHelper.previous.recycle();
            mAnimationHelper.previous = null;
        }
        if (mAnimationHelper.current != null) {
            mAnimationHelper.current.recycle();
            mAnimationHelper.current = null;
        }
        if (mAnimationHelper.next != null) {
            mAnimationHelper.next.recycle();
            mAnimationHelper.next = null;
        }
    }

    protected Bitmap createBitmapCacheAsGridSize() {
        return Bitmap.createBitmap(mGridSize.width(), mGridSize.height(), Bitmap.Config.ARGB_8888);
    }

    protected void createBitmapCacheForMonth(MonthDrawHelper args, Bitmap result) {
        Canvas canvas = new Canvas(result);
        drawMonth(canvas, args);
    }

    //endregion


    //region View overrides

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // calculate size
        mGridSize.left = 0;
        mGridSize.right = mDayCellSize.width()*DAYS_IN_WEEK;
        mGridSize.top = 0;
        mGridSize.bottom = mDayCellSize.height() * WEEKS_TO_SHOW;

        // create a temp bitmap
        if (mDayDrawHelper.background != null) {
            mDayDrawHelper.background.recycle();
            mDayDrawHelper.background = null;
        }
        mDayDrawHelper.background = Bitmap.createBitmap(
                mDayCellSize.width(), mDayCellSize.height(), Bitmap.Config.ARGB_8888);

        setMeasuredDimension(mGridSize.width(), mGridSize.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimationHelper.active) {
            drawAnimationMonths(canvas);
        } else {
            drawMonths(canvas);
        }
    }

    //endregion


    //region Animation drawing

    protected void drawAnimationMonths(Canvas canvas) {
        // do animation via translation the canvas
        long animOffset = System.currentTimeMillis() - mAnimationHelper.startTime;
        float translate = mAnimationHelper.interpolator.getInterpolation((float)animOffset/ mAnimationHelper.duration);
        translate *= -mAnimationHelper.direction;

        if (animOffset < mAnimationHelper.duration) {
            if (mAnimationHelper.transition == MonthTransition.HORIZONTAL) {
                canvas.translate((int)(translate * mGridSize.width()), 0);
            } else {
                canvas.translate(0, (int)(translate * mGridSize.height()));
            }
            drawAnimationMonthsOnCanvas(canvas);
        } else {
            // set new current month
            if (mAnimationHelper.direction > 0) {
                mMonthToShow = mMonthToShow.plusMonths(1);
            } else {
                mMonthToShow = mMonthToShow.minusMonths(1);
            }

            // TODO We should re-draw canvas to avoid "black-blink". Currently it's a hotfix. Cause is unknown.
            cleanAnimationBitmaps();
            mMonthDrawHelper.area.set(mGridSize);
            mMonthDrawHelper.month = getMonthDescriptor(mMonthToShow, 0);
            mAnimationHelper.current = createBitmapCacheAsGridSize();
            createBitmapCacheForMonth(mMonthDrawHelper, mAnimationHelper.current);
            drawAnimationMonth(canvas, mMonthDrawHelper, mAnimationHelper.current);
            cleanAnimationBitmaps();

            mAnimationHelper.active = false;
        }
        invalidate();
    }

    protected void drawAnimationMonthsOnCanvas(Canvas canvas) {
        if (mAnimationHelper.transition == MonthTransition.HORIZONTAL) {
            // draw previous month
            mMonthDrawHelper.area.left = -mGridSize.width();
            mMonthDrawHelper.area.top = 0;
            drawAnimationMonth(canvas, mMonthDrawHelper, mAnimationHelper.previous);

            // draw next month
            mMonthDrawHelper.area.left = mGridSize.width();
            mMonthDrawHelper.area.top = 0;
            drawAnimationMonth(canvas, mMonthDrawHelper, mAnimationHelper.next);
        }

        if (mAnimationHelper.transition == MonthTransition.VERTICAL) {
            // draw previous month
            mMonthDrawHelper.area.left = 0;
            mMonthDrawHelper.area.top = -mGridSize.height();
            drawAnimationMonth(canvas, mMonthDrawHelper, mAnimationHelper.previous);

            // draw next month
            mMonthDrawHelper.area.left = 0;
            mMonthDrawHelper.area.top = mGridSize.height();
            drawAnimationMonth(canvas, mMonthDrawHelper, mAnimationHelper.next);
        }

        // draw current month
        mMonthDrawHelper.area.left = 0;
        mMonthDrawHelper.area.top = 0;
        drawAnimationMonth(canvas, mMonthDrawHelper, mAnimationHelper.current);
    }

    protected void drawAnimationMonth(Canvas canvas, MonthDrawHelper h, Bitmap cachedMonth) {
        canvas.drawBitmap(
                cachedMonth,
                h.area.left,
                h.area.top,
                mAnimationHelper.paint);
    }

    //endregion


    //region Draw static months

    protected void drawMonths(Canvas canvas) {
        // draw current month
        mMonthDrawHelper.area.set(0, 0, mGridSize.width(), mGridSize.height());
        mMonthDrawHelper.month = getMonthDescriptor(mMonthToShow, 0);
        drawMonth(canvas, mMonthDrawHelper);
    }

    protected void drawMonth(Canvas canvas, MonthDrawHelper h) {
        for (int i = 0; i < h.month.getRowsCount(); i++) {
            mWeekDrawHelper.area.set(
                    h.area.left,
                    h.area.top + i * mDayCellSize.height(),
                    h.area.right,
                    h.area.top + i * mDayCellSize.height() + mDayCellSize.height());
            mWeekDrawHelper.month = h.month;
            mWeekDrawHelper.row = i;
            drawWeek(canvas, mWeekDrawHelper);
        }
    }

    protected void drawWeek(Canvas canvas, WeekDrawHelper h) {
        for (int i = 0; i < h.month.getColumnsCount(); i++) {
            mDayDrawHelper.area.set(
                    h.area.left + i * mDayCellSize.width(),
                    h.area.top,
                    h.area.left + i * mDayCellSize.width() + mDayCellSize.width(),
                    h.area.bottom);
            mDayDrawHelper.month = h.month;
            mDayDrawHelper.row = h.row;
            mDayDrawHelper.column = i;
            mDayDrawHelper.value = mMapDayToString.get(h.month.getDayAt(h.row, mDayDrawHelper.column));
            drawDay(canvas, mDayDrawHelper);
        }
    }

    protected void drawDay(Canvas canvas, DayDrawHelper h) {
        dispatchCellInfo(h);

        canvas.drawBitmap(
                h.background,
                h.area.left,
                h.area.top,
                h.cellBackgroundPaint);

        h.cellTextPaint.setAntiAlias(true);
        h.cellTextPaint.setStyle(Paint.Style.FILL);
        h.cellTextPaint.setTextSize(h.textSize);
        h.cellTextPaint.setColor(h.textColor);
        h.measuredTextWidth = h.cellBackgroundPaint.measureText(h.value);
        canvas.drawText(
                h.value,
                h.area.centerX() - h.measuredTextWidth/2,
                h.area.centerY() + h.textSize/2 - 2, // трик-хуик, to make it really in current. getTextBounds not helps
                h.cellTextPaint);
    }

    protected void dispatchCellInfo(DayDrawHelper h) {
        boolean isPressed  = false;
        boolean isSelected = false;
        boolean isEnabled  = false;

        if (h.month.isEqualWithDate(h.row, h.column, mToday)) {
            h.dayType = DayType.TODAY;
        } else if (h.month.isWithinCurrentMonth(h.row, h.column)) {
            h.dayType = DayType.THIS_MONTH;
        } else {
            h.dayType = DayType.NEIGHBOUR_MONTH;
        }

        if (mDayDrawHelper.row == mCurrentlyPressedCell.row
                && mDayDrawHelper.column == mCurrentlyPressedCell.column) {
            isPressed = true;
        }

        switch (h.dayType) {
            case TODAY:
                h.cellDrawInfo = mTodayCellInfo;
                break;
            case THIS_MONTH:
                h.cellDrawInfo = mThisMonthCellInfo;
                break;
            case NEIGHBOUR_MONTH:
                h.cellDrawInfo = mNeighbourMonthCellInfo;
                break;
        }

        int[] states = getStatesAsSet(isPressed, isSelected, isEnabled);

        h.textSize = h.cellDrawInfo.textSize;
        h.textColor = getTextColorForState(h.cellDrawInfo.textColor, states);
        h.cellDrawInfo.drawable.setState(states);
        drawableToBitmap(h.cellDrawInfo.drawable, h.background);
    }

    //endregion


    //region Touch disptacher

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchEventStartTime = System.currentTimeMillis();
                onCellPressed(getCellForCoordinates(event.getX(), event.getY()));
                return true;
            case MotionEvent.ACTION_MOVE:
                onCellPressed(getCellForCoordinates(event.getX(), event.getY()));
                return true;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - mTouchEventStartTime < (long) ViewConfiguration.getLongPressTimeout()) {
                    mTouchEventStartTime = -1;
                    onClick(getDateForCoordinates(event.getX(), event.getY()));
                }
                onCellPressed(null);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    protected void onCellPressed(Cell cell) {
        if (cell == null) {
            mCurrentlyPressedCell.row = -1;
            mCurrentlyPressedCell.column = -1;
        } else {
            mCurrentlyPressedCell.row = cell.row;
            mCurrentlyPressedCell.column = cell.column;
        }
        invalidate();
    }

    protected void onClick(LocalDate date) {
        if (mOnDateClickListener != null) {
            mOnDateClickListener.onClick(date);
        }
    }

    //endregion


    //region Utils

    protected MonthDescriptor getMonthDescriptor(LocalDate month, int monthOffset) {
        if (monthOffset == 0) {
            return new MonthDescriptor(month.getYear(), month.getMonthOfYear() - 1, mFirstDayOfWeek);
        } else {
            return getMonthDescriptor(month.plusMonths(monthOffset), 0);
        }
    }

    protected Cell getCellForCoordinates(float x, float y) {
        return new Cell(
                (int) y / mDayCellSize.height(),
                (int) x / mDayCellSize.width());
    }

    protected LocalDate getDateForCoordinates(float x, float y) {
        Cell cell = getCellForCoordinates(x, y);
        return new MonthDescriptor(mMonthToShow.getYear(), mMonthToShow.getMonthOfYear() - 1, mFirstDayOfWeek)
                .getLocalDate(cell.row, cell.column);
    }

    protected int[] getStatesAsSet(boolean isPressed, boolean isSelected, boolean isEnabled) {
        int size = 0;
        if (isPressed) size++;
        if (isSelected) size++;
        if (isEnabled) size++;
        int[] r = new int[size];
        int offset = 0;

        if (isPressed) {
            r[offset++] = android.R.attr.state_pressed;
        }
        if (isSelected) {
            r[offset++] = android.R.attr.state_selected;
        }
        if (isEnabled) {
            r[offset] = android.R.attr.state_enabled;
        }

        return r;
    }

    protected int getTextColorForState(ColorStateList list, int[] states) {
        return list.getColorForState(
                states,
                list.getDefaultColor());
    }

    protected void drawableToBitmap(Drawable drawable, Bitmap target) {
        Canvas canvas = new Canvas(target);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
    }

    //endregion

}