package net.alexoro.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.*;
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
class CalendarGridView extends View {

    private static final int WEEKS_TO_SHOW = 6; // rows
    private static final int DAYS_IN_WEEK = 7;  // columns
    private static final int ACTION_MASK = 255; // MotionEvent.ACTION_MASK was introduce only in API #5

    static class MonthHelper {
        /**
         * Area to draw in
         */
        public Rect area;

        /**
         *  rows = week, columns = days
         *  Size: WEEKS_TO_SHOW:DAYS_IN_WEEK
         */
        public DayCellDescription[][] month;
    }

    static class WeekHelper {
        public Rect area;
        public DayCellDescription[][] month;
        int row;
    }

    static class DayHelper {
        public Cell cell;
        public Rect area;
        public Bitmap background;
        public Paint cellBackgroundPaint;
        public Paint cellTextPaint;
        public float measuredTextWidth;
    }

    static class AnimationHelper {
        public boolean active;
        public DayCellDescription[][] month;
        public Interpolator interpolator;
        public long startTime;
        public long duration;
        public MonthTransition transition;
        public int direction;
        public Rect area;
        public Paint paint;
    }


    private Rect mGridSize;
    private Rect mDayCellSize;
    private MonthTransition mMonthTransition;

    private int mFirstDayOfWeek;
    private LocalDate mToday;
    private LocalDate mMonthToShow;
    private Pair<LocalDate, LocalDate> mEnabledRange;
    private Pair<LocalDate, LocalDate> mSelectedRange;

    private MonthDescriptor mCurrentMonthDescriptor;
    private DayCellDescription[][] mCurrentMonth;

    private MonthHelper mMonthHelper;
    private WeekHelper mWeekHelper;
    private DayHelper mDayHelper;
    private AnimationHelper mAnimationHelper;

    private Map<Integer, String> mMapDayToString;
    private long mTouchEventStartTime;
    private Cell mCurrentlyPressedCell;
    private OnDateClickListener mOnDateClickListener;
    private OnDateChangedListener mOnDateChangedListener;

    private DayCellDescription.DayStyle mTodayCellInfo;
    private DayCellDescription.DayStyle mThisMonthCellInfo;
    private DayCellDescription.DayStyle mNeighbourMonthCellInfo;
    private int mCellSpacing;


    //region Construction

    public CalendarGridView(Context context) {
        this(context, null);
    }

    public CalendarGridView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CalendarGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mMapDayToString = new HashMap<Integer, String>();
        for (int i = 1; i <= 31; i++) {
            mMapDayToString.put(i, String.valueOf(i));
        }
        mTouchEventStartTime = -1;
        mCurrentlyPressedCell = null;
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
        mEnabledRange = null;
        mSelectedRange = null;

        //region Helpers init
        mMonthHelper = new MonthHelper();
        mMonthHelper.area = new Rect();
        mMonthHelper.month = mCurrentMonth;

        mWeekHelper = new WeekHelper();
        mWeekHelper.area = new Rect();
        mWeekHelper.month = mMonthHelper.month;
        mWeekHelper.row = -1;

        mDayHelper = new DayHelper();
        mDayHelper.cell = new Cell(-1, -1);
        mDayHelper.area = new Rect();
        mDayHelper.background = null;
        mDayHelper.cellBackgroundPaint = new Paint();
        mDayHelper.cellTextPaint = new Paint();
        mDayHelper.cellTextPaint.setAntiAlias(true);
        mDayHelper.cellTextPaint.setStyle(Paint.Style.FILL);
        mDayHelper.measuredTextWidth = -1f;
        //endregion

        mAnimationHelper = new AnimationHelper();
        mAnimationHelper.interpolator = new AccelerateDecelerateInterpolator();
        mAnimationHelper.duration = 700;
        mAnimationHelper.area = new Rect();
        mAnimationHelper.paint = new Paint();
        mAnimationHelper.paint.setAntiAlias(true);
        mAnimationHelper.paint.setStyle(Paint.Style.FILL);


        //region styles from xml
        mTodayCellInfo = new DayCellDescription.DayStyle();
        mTodayCellInfo.name = "Today";
        mTodayCellInfo.textSize = 14f;
        mTodayCellInfo.drawable = (StateListDrawable) getResources().getDrawable(R.drawable.nac__bg_today);
        mTodayCellInfo.textColor = getResources().getColorStateList(R.color.nac__today);

        mThisMonthCellInfo = new DayCellDescription.DayStyle();
        mThisMonthCellInfo.name = "ThisMonth";
        mThisMonthCellInfo.textSize = 14f;
        mThisMonthCellInfo.drawable = (StateListDrawable) getResources().getDrawable(R.drawable.nac__bg_this_month);
        mThisMonthCellInfo.textColor = getResources().getColorStateList(R.color.nac__this_month);

        mNeighbourMonthCellInfo = new DayCellDescription.DayStyle();
        mNeighbourMonthCellInfo.name = "NeighbourMonth";
        mNeighbourMonthCellInfo.textSize = 14f;
        mNeighbourMonthCellInfo.drawable = (StateListDrawable) getResources().getDrawable(R.drawable.nac__bg_neighbour_month);
        mNeighbourMonthCellInfo.textColor = getResources().getColorStateList(R.color.nac__neighbour_month);

        mCellSpacing = 2;
        //endregion

        show(mMonthToShow);
    }

    //endregion


    // region Set & Get properties

    public void setDayCellSize(int width, int height) {
        // TODO Here might be a bug when this method is called during the animation
        mDayCellSize.set(0, 0, width, height);
        requestLayout();
        invalidate();
    }

    public int getDayCellWidth() {
        return mDayCellSize.width();
    }

    public int getDayCellHeight() {
        return mDayCellSize.height();
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

    public OnDateChangedListener getOnDateChangedListener() {
        return mOnDateChangedListener;
    }

    public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        mOnDateChangedListener = onDateChangedListener;
    }

    public void setEnabledRange(LocalDate startIncluding, LocalDate endIncluding) {
        mEnabledRange = new Pair<LocalDate, LocalDate>(
                new LocalDate(startIncluding),
                new LocalDate(endIncluding));
        updateEnabledSelectedMonthParams();
        invalidate();
    }

    public LocalDate getEnabledRangeStart() {
        if (mEnabledRange != null) {
            return mEnabledRange.first;
        }
        return null;
    }

    public LocalDate getEnabledRangeEnd() {
        if (mEnabledRange != null) {
            return mEnabledRange.second;
        }
        return null;
    }

    public void setSelectedRange(LocalDate startIncluding, LocalDate endIncluding) {
        //TODO bad code
        if (startIncluding == null && endIncluding == null) {
            mSelectedRange = null;
        } else {
            mSelectedRange = new Pair<LocalDate, LocalDate>(
                    new LocalDate(startIncluding),
                    new LocalDate(endIncluding));
        }
        updateEnabledSelectedMonthParams();
        invalidate();
    }

    public LocalDate getSelectedRangeStart() {
        if (mSelectedRange != null) {
            return mSelectedRange.first;
        }
        return null;
    }

    public LocalDate getSelectedRangeEnd() {
        if (mSelectedRange != null) {
            return mSelectedRange.second;
        }
        return null;
    }

    //endregion


    //region Change data to show

    public void nextMonth() {
        if (mAnimationHelper.active) {
            return;
        }
        if (mMonthTransition == MonthTransition.NONE) {
            show(mMonthToShow.plusMonths(1));
        } else {
            setupAnimation(1);
            invalidate();
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onChanged(mMonthToShow.plusMonths(1));
            }
        }
    }

    public void previousMonth() {
        if (mAnimationHelper.active) {
            return;
        }
        if (mMonthTransition == MonthTransition.NONE) {
            show(mMonthToShow.minusMonths(1));
        } else {
            setupAnimation(-1);
            invalidate();
            if (mOnDateChangedListener != null) {
                mOnDateChangedListener.onChanged(mMonthToShow.minusMonths(1));
            }
        }
    }

    public void show(LocalDate month) {
        mMonthToShow = new LocalDate(month);
        mCurrentMonthDescriptor = new MonthDescriptor(mMonthToShow.getYear(),
                mMonthToShow.getMonthOfYear() - 1, mFirstDayOfWeek);
        mCurrentMonth = createDefaultDayCellDescriptions(mCurrentMonthDescriptor);
        invalidate();
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onChanged(mMonthToShow);
        }
    }

    //endregion


    //region Animation

    protected void setupAnimation(int direction) {
        MonthDescriptor md = new MonthDescriptor(mMonthToShow.getYear(),
                mMonthToShow.getMonthOfYear() - 1, mFirstDayOfWeek);
        if (direction < 0) {
            md.previousMonth();
        } else {
            md.nextMonth();
        }
        mAnimationHelper.month = createDefaultDayCellDescriptions(md);

        mAnimationHelper.active = true;
        mAnimationHelper.startTime = System.currentTimeMillis();
        mAnimationHelper.direction = direction;
        mAnimationHelper.transition = mMonthTransition;
    }

    //endregion


    //region View overrides

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // calculate size
        mGridSize.left = 0;
        mGridSize.right = mDayCellSize.width() * DAYS_IN_WEEK + mCellSpacing * (DAYS_IN_WEEK - 1);
        mGridSize.top = 0;
        mGridSize.bottom = mDayCellSize.height() * WEEKS_TO_SHOW + mCellSpacing * (WEEKS_TO_SHOW - 1);

        // create a temp bitmap
        if (mDayHelper.background != null) {
            mDayHelper.background.recycle();
            mDayHelper.background = null;
        }
        mDayHelper.background = Bitmap.createBitmap(
                mDayCellSize.width(), mDayCellSize.height(), Bitmap.Config.ARGB_8888);

        setMeasuredDimension(mGridSize.width(), mGridSize.height());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimationHelper.active) {
            drawAnimationMonths(canvas);
        } else {
            drawCurrentMonth(canvas);
        }
    }

    protected void drawAnimationMonths(Canvas canvas) {
        // do animation via translation the canvas
        long animOffset = System.currentTimeMillis() - mAnimationHelper.startTime;
        float translate = mAnimationHelper.interpolator.getInterpolation((float)animOffset/ mAnimationHelper.duration);
        translate *= -mAnimationHelper.direction;

        if (animOffset < mAnimationHelper.duration) {
            //TODO There is a bug at the end of animation because of fake mCellSpacing
            if (mAnimationHelper.transition == MonthTransition.HORIZONTAL) {
                canvas.translate((int)(translate * (mGridSize.width() + mCellSpacing) ), 0);
            } else {
                canvas.translate(0, (int)(translate * (mGridSize.height() + mCellSpacing)));
            }
            drawAnimationNeighbourMonth(canvas);
            drawCurrentMonth(canvas);
            invalidate();
        } else {
            // set new current month
            if (mAnimationHelper.direction > 0) {
                mMonthToShow = mMonthToShow.plusMonths(1);
            } else {
                mMonthToShow = mMonthToShow.minusMonths(1);
            }
            mAnimationHelper.active = false;

            // The re-draw must be used to avoid "black-blink" of view
            // The cause is that show() is slowly operation (~200ms) and must be not called
            // while the canvas is empty
            // TODO It is better to calculate all before the animation
            mMonthHelper.area.left = 0;
            mMonthHelper.area.top = 0;
            mMonthHelper.month = mAnimationHelper.month;
            drawMonth(canvas, mMonthHelper);

            show(mMonthToShow);
            invalidate();
        }
    }

    protected void drawAnimationNeighbourMonth(Canvas canvas) {
        //TODO There is a bug at the end of animation because of fake mCellSpacing
        if (mAnimationHelper.transition == MonthTransition.HORIZONTAL) {
            mMonthHelper.area.left = (mGridSize.width() + mCellSpacing) * mAnimationHelper.direction;
            mMonthHelper.area.top = 0;
        }
        //TODO There is a bug at the end of animation because of fake mCellSpacing
        if (mAnimationHelper.transition == MonthTransition.VERTICAL) {
            mMonthHelper.area.left = 0;
            mMonthHelper.area.top = (mGridSize.height() + mCellSpacing) * mAnimationHelper.direction;
        }
        mMonthHelper.month = mAnimationHelper.month;
        drawMonth(canvas, mMonthHelper);
    }

    protected void drawCurrentMonth(Canvas canvas) {
        mMonthHelper.area.set(0, 0, mGridSize.width(), mGridSize.height());
        mMonthHelper.month = mCurrentMonth;
        drawMonth(canvas, mMonthHelper);
    }

    //endregion


    //region Draw static months

    protected void drawMonth(Canvas canvas, MonthHelper h) {
        for (int i = 0; i < WEEKS_TO_SHOW; i++) {
            mWeekHelper.area.set(
                    h.area.left,
                    h.area.top + i * mDayCellSize.height() + i * mCellSpacing,
                    h.area.right,
                    h.area.top + i * mDayCellSize.height() + i * mCellSpacing + mDayCellSize.height());
            mWeekHelper.month = h.month;
            mWeekHelper.row = i;
            drawWeek(canvas, mWeekHelper);
        }
    }

    protected void drawWeek(Canvas canvas, WeekHelper h) {
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            mDayHelper.cell.update(h.row, i);
            mDayHelper.area.set(
                    h.area.left + i * mDayCellSize.width() + i * mCellSpacing,
                    h.area.top,
                    h.area.left + i * mDayCellSize.width() + i * mCellSpacing + mDayCellSize.width(),
                    h.area.bottom);
            drawDay(canvas, h.month[h.row][i], mDayHelper);
        }
    }

    protected void drawDay(Canvas canvas, DayCellDescription d, DayHelper h) {
        // get background
        int[] states = getStatesAsSet(
                d.isEnabled,
                d.isSelected,
                d.isPressed);

        d.dayStyle.drawable.setState(states);
        drawableToBitmap(d.dayStyle.drawable, h.background);

        canvas.drawBitmap(
                h.background,
                h.area.left,
                h.area.top,
                h.cellBackgroundPaint);

        String value = mMapDayToString.get(d.day);
        h.cellTextPaint.setTextSize(d.dayStyle.textSize);
        h.cellTextPaint.setColor(getTextColorForState(d.dayStyle.textColor, states));
        h.measuredTextWidth = h.cellBackgroundPaint.measureText(value);
        canvas.drawText(
                value,
                h.area.centerX() - h.measuredTextWidth/2,
                h.area.centerY() + d.dayStyle.textSize/2 - 2, // трик-хуик, to make it really in current. getTextBounds not helps
                h.cellTextPaint);
    }

    //endregion


    //region Touch dispatcher

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchEventStartTime = System.currentTimeMillis();
                onDayCellPressed(getDayCellForCoordinates(event.getX(), event.getY()));
                return true;
            case MotionEvent.ACTION_MOVE:
                onDayCellPressed(getDayCellForCoordinates(event.getX(), event.getY()));
                return true;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - mTouchEventStartTime < (long) ViewConfiguration.getLongPressTimeout()) {
                    mTouchEventStartTime = -1;
                    Cell cell = getDayCellForCoordinates(event.getX(), event.getY());
                    if (cell != null) {
                        onClick(cell);
                    }
                }
                onDayCellPressed(null);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    protected void onDayCellPressed(Cell cell) {
        if (mCurrentlyPressedCell != null) {
            mCurrentMonth[mCurrentlyPressedCell.row][mCurrentlyPressedCell.column].isPressed = false;
        }
        if (cell == null) {
            mCurrentlyPressedCell = null;
        } else {
            mCurrentlyPressedCell = cell;
            mCurrentMonth[cell.row][cell.column].isPressed = true;
        }
        invalidate();
    }

    protected void onClick(Cell cell) {
        if (mOnDateClickListener != null
                && mCurrentMonth[cell.row][cell.column].isEnabled) {
            mOnDateClickListener.onClick(new LocalDate(
                    mCurrentMonth[cell.row][cell.column].year,
                    mCurrentMonth[cell.row][cell.column].month + 1,
                    mCurrentMonth[cell.row][cell.column].day));
        }
    }

    //endregion


    //region Utils

    protected boolean isDayEnabled(MonthDescriptor md, int row, int column) {
        return mEnabledRange == null
                || md.compareToDate(row, column, mEnabledRange.first) >= 0
                && md.compareToDate(row, column, mEnabledRange.second) <= 0;
    }

    protected boolean isDaySelected(MonthDescriptor md, int row, int column) {
        return mSelectedRange != null
                && md.compareToDate(row, column, mSelectedRange.first) >= 0
                && md.compareToDate(row, column, mSelectedRange.second) <= 0;
    }

    protected boolean isDayPressed(int row, int column) {
        return mCurrentlyPressedCell != null
                && row == mCurrentlyPressedCell.row
                && column == mCurrentlyPressedCell.column;
    }

    protected Cell getDayCellForCoordinates(float x, float y) {
        if (x > mGridSize.left && x < mGridSize.right
                && y > mGridSize.top && y < mGridSize.bottom) {
            float cX = 0, cY = 0;
            int row = -1, col = -1; // it will be always incremented at least ones

            while (cY < y) {
                cY += mDayCellSize.height() + mCellSpacing;
                row++;
            }
            while (cX < x) {
                cX += mDayCellSize.width() + mCellSpacing;
                col++;
            }

            return new Cell(row, col);
        } else {
            return null;
        }
    }

    protected int[] getStatesAsSet(boolean isEnabled, boolean isSelected, boolean isPressed) {
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


    //region CellDescription generator and updater

    protected DayCellDescription[][] createDefaultDayCellDescriptions(MonthDescriptor mdh) {
        DayCellDescription[][] r = new DayCellDescription[WEEKS_TO_SHOW][DAYS_IN_WEEK];

        DayCellDescription c;
        int day;
        for (int row = 0; row < WEEKS_TO_SHOW; row++) {
            for (int col = 0; col < DAYS_IN_WEEK; col++) {
                day = mdh.getDayAt(row, col);
                c = new DayCellDescription();
                if (day < 32 && row < 1 && day > 10) { // it is previous month
                    mdh.previousMonth();
                    c.year = mdh.getYear();
                    c.month = mdh.getMonth();
                    c.day = day;
                    mdh.nextMonth();
                } else if (day < 10 && row > 3) { // it is next month
                    mdh.nextMonth();
                    c.year = mdh.getYear();
                    c.month = mdh.getMonth();
                    c.day = day;
                    mdh.previousMonth();
                } else { // it is this month
                    c.year = mdh.getYear();
                    c.month = mdh.getMonth();
                    c.day = day;
                }
                c.isEnabled = isDayEnabled(mdh, row, col);
                c.isSelected = isDaySelected(mdh, row, col);
                c.isPressed = isDayPressed(row, col);

                if (mdh.compareToDate(row, col, mToday) == 0) {
                    c.dayStyle = mTodayCellInfo;
                } else if (mdh.isWithinCurrentMonth(row, col)) {
                    c.dayStyle = mThisMonthCellInfo;
                } else {
                    c.dayStyle = mNeighbourMonthCellInfo;
                }

                r[row][col] = c;
            }
        }

        return r;
    }

    protected void updateEnabledSelectedMonthParams() {
        for (int row = 0; row < WEEKS_TO_SHOW; row++) {
            for (int col = 0; col < DAYS_IN_WEEK; col++) {
                mCurrentMonth[row][col].isEnabled = isDayEnabled(mCurrentMonthDescriptor, row, col);
                mCurrentMonth[row][col].isSelected = isDaySelected(mCurrentMonthDescriptor, row, col);
                mCurrentMonth[row][col].isPressed = isDayPressed(row, col);
            }
        }
    }

    //endregion


}