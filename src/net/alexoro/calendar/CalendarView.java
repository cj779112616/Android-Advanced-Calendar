package net.alexoro.calendar;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.MonthDisplayHelper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

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


    private int mCellWidth;
    private int mCellHeight;

    private int mGridWidth;
    private int mGridHeight;

    private Paint mPaint;

    private int mTranslate = 0;

    private android.view.animation.Interpolator mIn;
    private long mAnimStart;
    private long mAnimEnd;

    private MonthDisplayHelper mMonthDisplayHelper;


    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        mIn = new AccelerateInterpolator();

        initWithDefaults();

        /*getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTranslate = -50;
            }
        }, 5000);*/
        mTranslate = 0;
        mAnimStart = System.currentTimeMillis();
        mAnimEnd = System.currentTimeMillis() + 2500;

        mMonthDisplayHelper = new MonthDisplayHelper(2013, 5);
    }

    private void initWithDefaults() {
        mCellWidth = mCellHeight = 64;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mGridWidth = mCellWidth*DAYS_IN_WEEK;
        mGridHeight = mCellHeight * WEEKS_TO_SHOW;
        setMeasuredDimension(mGridWidth, mGridHeight);
    }

    int mTranslateX = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*long animDuration = mAnimEnd - mAnimStart;
        long animOffset = System.currentTimeMillis() - mAnimStart;
        float translate = mIn.getInterpolation((float)animOffset/animDuration);
        if (translate > 1f) {
            translate = 1f;
        }
        mTranslateX = (int)(translate * mGridWidth);
        canvas.translate(mTranslateX, 0);*/

        canvas.drawColor(Color.GREEN);

        mPaint.setColor(Color.RED);
        canvas.drawRect(-mGridWidth, 0, 0, mGridHeight, mPaint);

        mPaint.setColor(Color.BLUE);
        canvas.drawRect(mGridWidth, 0, mGridHeight*2, mGridHeight, mPaint);

        /*if (animOffset < animDuration) {
            invalidate();
        }*/

        for (int i = 0; i < WEEKS_TO_SHOW; i++) {
            drawWeek(canvas, mMonthDisplayHelper, i);
        }

    }

    private Rect mRect = new Rect();

    protected void drawWeek(Canvas canvas, MonthDisplayHelper mdh, int row) {
        mRect.top = row * mCellHeight;
        mRect.bottom = mRect.top + mCellHeight;
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            mRect.left = i * mCellWidth;
            mRect.right = mRect.left + mCellWidth;
            drawDay(canvas, mRect, mdh.getDayAt(row, i));
        }
    }

    private Random mRandom = new Random(System.currentTimeMillis());

    protected void drawDay(Canvas canvas, Rect rect, int day) {
        mPaint.setColor(mRandom.nextInt());
        canvas.drawRect(rect, mPaint);
        mPaint.setColor(Color.WHITE);
        canvas.drawText(String.valueOf(day), rect.centerX(), rect.centerY(), mPaint);
    }


}