package net.alexoro.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
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
    private static final int NUMBER_OF_SUPPORTED_COLOR_STATES = 3;
    private static final int ACTION_MASK = 255; // MotionEvent.ACTION_MASK was introduce only in API #5

    static class MonthDrawArgs {
        public Rect area;
        public MonthDescriptor month;
    }

    static class WeekDrawArgs {
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
        public int[] textColorStates;

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

    static class AnimationArgs {
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
        public Bitmap defaultBackgroundBitmap;
        public Bitmap pressedBackgroundBitmap;
    }


    private Rect mGridSize;
    private Rect mDayCellSize;
    private MonthTransition mMonthTransition;

    private int mFirstDayOfWeek;
    private LocalDate mToday;
    private LocalDate mMonthToShow;

    private MonthDrawArgs mMonthDrawArgs;
    private WeekDrawArgs mWeekDrawArgs;
    private DayDrawHelper mDayDrawHelper;
    private AnimationArgs mAnimationArgs;

    private Map<Integer, String> mMapDayToString;
    private long mTouchEventStartTime;
    private Cell mCurrentlyPressedCell;
    private OnDateClickListener mOnDateClickListener;

    private CellDrawInfo mThisMonthCellInfo;
    private CellDrawInfo mNeighbourMonthCellInfo;
    private CellDrawInfo mTodayCellInfo;


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

        mMonthDrawArgs = new MonthDrawArgs();
        mMonthDrawArgs.area = new Rect();
        mMonthDrawArgs.month = getMonthDescriptor(mToday, 0);

        mWeekDrawArgs = new WeekDrawArgs();
        mWeekDrawArgs.area = new Rect();
        mWeekDrawArgs.month = mMonthDrawArgs.month;
        mWeekDrawArgs.row = -1;

        mDayDrawHelper = new DayDrawHelper();
        mDayDrawHelper.area = new Rect();
        mDayDrawHelper.month = mMonthDrawArgs.month;
        mDayDrawHelper.row = -1;
        mDayDrawHelper.column = -1;
        mDayDrawHelper.textColorStates = new int[NUMBER_OF_SUPPORTED_COLOR_STATES];
        mDayDrawHelper.cellBackgroundPaint = new Paint();
        mDayDrawHelper.cellTextPaint = new Paint();

        mAnimationArgs = new AnimationArgs();
        mAnimationArgs.interpolator = new AccelerateDecelerateInterpolator();
        mAnimationArgs.duration = 700;
        mAnimationArgs.paint = new Paint();
        mAnimationArgs.paint.setAntiAlias(true);
        mAnimationArgs.paint.setStyle(Paint.Style.FILL);

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

        mMonthDrawArgs.area.set(mGridSize);
        mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, -1);
        mAnimationArgs.previous = createBitmapAsGridSize();
        createBitmapCacheForMonth(mMonthDrawArgs, mAnimationArgs.previous);

        mMonthDrawArgs.area.set(mGridSize);
        mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 0);
        mAnimationArgs.current = createBitmapAsGridSize();
        createBitmapCacheForMonth(mMonthDrawArgs, mAnimationArgs.current);

        mMonthDrawArgs.area.set(mGridSize);
        mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 1);
        mAnimationArgs.next = createBitmapAsGridSize();
        createBitmapCacheForMonth(mMonthDrawArgs, mAnimationArgs.next);
    }

    protected void cleanAnimationBitmaps() {
        if (mAnimationArgs.previous != null) {
            mAnimationArgs.previous.recycle();
            mAnimationArgs.previous = null;
        }
        if (mAnimationArgs.current != null) {
            mAnimationArgs.current.recycle();
            mAnimationArgs.current = null;
        }
        if (mAnimationArgs.next != null) {
            mAnimationArgs.next.recycle();
            mAnimationArgs.next = null;
        }
    }

    protected Bitmap createBitmapAsGridSize() {
        return Bitmap.createBitmap(mGridSize.width(), mGridSize.height(), Bitmap.Config.ARGB_8888);
    }

    protected void createBitmapCacheForMonth(MonthDrawArgs args, Bitmap result) {
        Canvas canvas = new Canvas(result);
        drawMonth(canvas, args);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // calculate size
        mGridSize.left = 0;
        mGridSize.right = mDayCellSize.width()*DAYS_IN_WEEK;
        mGridSize.top = 0;
        mGridSize.bottom = mDayCellSize.height() * WEEKS_TO_SHOW;

        // create cache for background drawables
        createBackgroundDrawablesCache();

        setMeasuredDimension(mGridSize.width(), mGridSize.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimationArgs.active) {
            drawAnimationMonths(canvas);
        } else {
            drawMonths(canvas);
        }
    }

    protected void drawAnimationMonths(Canvas canvas) {
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
            drawAnimationMonthsOnCanvas(canvas);
        } else {
            // set new current month
            if (mAnimationArgs.direction > 0) {
                mMonthToShow = mMonthToShow.plusMonths(1);
            } else {
                mMonthToShow = mMonthToShow.minusMonths(1);
            }

            // TODO we should redraw canvas to avoid "black-blink". Currently, it's a hotfix
            cleanAnimationBitmaps();
            mMonthDrawArgs.area.set(mGridSize);
            mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 0);
            mAnimationArgs.current = createBitmapAsGridSize();
            createBitmapCacheForMonth(mMonthDrawArgs, mAnimationArgs.current);
            drawAnimationMonth(canvas, mMonthDrawArgs, mAnimationArgs.current);
            cleanAnimationBitmaps();

            mAnimationArgs.active = false;
        }
        invalidate();
    }

    protected void drawAnimationMonthsOnCanvas(Canvas canvas) {
        if (mAnimationArgs.transition == MonthTransition.HORIZONTAL) {
            // draw previous month
            mMonthDrawArgs.area.left = -mGridSize.width();
            mMonthDrawArgs.area.top = 0;
            drawAnimationMonth(canvas, mMonthDrawArgs, mAnimationArgs.previous);

            // draw next month
            mMonthDrawArgs.area.left = mGridSize.width();
            mMonthDrawArgs.area.top = 0;
            drawAnimationMonth(canvas, mMonthDrawArgs, mAnimationArgs.next);
        }

        if (mAnimationArgs.transition == MonthTransition.VERTICAL) {
            // draw previous month
            mMonthDrawArgs.area.left = 0;
            mMonthDrawArgs.area.top = -mGridSize.height();
            drawAnimationMonth(canvas, mMonthDrawArgs, mAnimationArgs.previous);

            // draw next month
            mMonthDrawArgs.area.left = 0;
            mMonthDrawArgs.area.top = mGridSize.height();
            drawAnimationMonth(canvas, mMonthDrawArgs, mAnimationArgs.next);
        }

        // draw current month
        mMonthDrawArgs.area.left = 0;
        mMonthDrawArgs.area.top = 0;
        drawAnimationMonth(canvas, mMonthDrawArgs, mAnimationArgs.current);
    }

    protected void drawAnimationMonth(Canvas canvas, MonthDrawArgs args, Bitmap cachedMonth) {
        canvas.drawBitmap(
                cachedMonth,
                args.area.left,
                args.area.top,
                mAnimationArgs.paint);
    }


    protected void drawMonths(Canvas canvas) {
        // draw current month
        mMonthDrawArgs.area.set(0, 0, mGridSize.width(), mGridSize.height());
        mMonthDrawArgs.month = getMonthDescriptor(mMonthToShow, 0);
        drawMonth(canvas, mMonthDrawArgs);
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
            mDayDrawHelper.area.set(
                    args.area.left + i * mDayCellSize.width(),
                    args.area.top,
                    args.area.left + i * mDayCellSize.width() + mDayCellSize.width(),
                    args.area.bottom);
            mDayDrawHelper.month = args.month;
            mDayDrawHelper.row = args.row;
            mDayDrawHelper.column = i;
            mDayDrawHelper.value = mMapDayToString.get(args.month.getDayAt(args.row, mDayDrawHelper.column));
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

        h.textColorStates[0] = isPressed  ? android.R.attr.state_pressed  : 0;
        h.textColorStates[1] = isSelected ? android.R.attr.state_selected : 0;
        h.textColorStates[2] = isEnabled  ? android.R.attr.state_enabled  : 0;
        h.textColor = getTextColorForState(h.cellDrawInfo.textColor, h.textColorStates);

        if (isPressed) {
            h.textSize = h.cellDrawInfo.textSize;
            h.background = h.cellDrawInfo.pressedBackgroundBitmap;
        } else {
            h.textSize = h.cellDrawInfo.textSize;
            h.background = h.cellDrawInfo.defaultBackgroundBitmap;
        }
    }


    // ============================================


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


    // ============================================


    protected MonthDescriptor getMonthDescriptor(LocalDate month, int monthOffset) {
        if (monthOffset == 0) {
            return new MonthDescriptor(month.getYear(), month.getMonthOfYear() - 1, mFirstDayOfWeek);
        } else {
            return getMonthDescriptor(month.plusMonths(monthOffset), 0);
        }
    }

    protected LocalDate getDateForCoordinates(float x, float y) {
        Cell cell = getCellForCoordinates(x, y);
        return new MonthDescriptor(mMonthToShow.getYear(), mMonthToShow.getMonthOfYear() - 1, mFirstDayOfWeek)
                .getLocalDate(cell.row, cell.column);
    }

    protected Cell getCellForCoordinates(float x, float y) {
        return new Cell(
                (int) y / mDayCellSize.height(),
                (int) x / mDayCellSize.width());
    }

    protected Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                mDayCellSize.width(),
                mDayCellSize.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    //TODO implement it via cache Map<int[], Bitmap> with lazy instantiate
    //NOTICE int[] can not be used as map key
    protected void createBackgroundDrawablesCache() {
        mThisMonthCellInfo.drawable.setState(new int[] {});
        mThisMonthCellInfo.defaultBackgroundBitmap = drawableToBitmap(mThisMonthCellInfo.drawable);
        mThisMonthCellInfo.drawable.setState(new int[] {android.R.attr.state_pressed});
        mThisMonthCellInfo.pressedBackgroundBitmap = drawableToBitmap(mThisMonthCellInfo.drawable);

        mNeighbourMonthCellInfo.drawable.setState(new int[] {});
        mNeighbourMonthCellInfo.defaultBackgroundBitmap = drawableToBitmap(mNeighbourMonthCellInfo.drawable);
        mNeighbourMonthCellInfo.drawable.setState(new int[] {android.R.attr.state_pressed});
        mNeighbourMonthCellInfo.pressedBackgroundBitmap = drawableToBitmap(mNeighbourMonthCellInfo.drawable);

        mTodayCellInfo.drawable.setState(new int[] {});
        mTodayCellInfo.defaultBackgroundBitmap = drawableToBitmap(mTodayCellInfo.drawable);
        mTodayCellInfo.drawable.setState(new int[] {android.R.attr.state_pressed});
        mTodayCellInfo.pressedBackgroundBitmap = drawableToBitmap(mTodayCellInfo.drawable);
    }

    protected int getTextColorForState(ColorStateList list, int[] states) {
        return list.getColorForState(
                states,
                list.getDefaultColor());
    }

}