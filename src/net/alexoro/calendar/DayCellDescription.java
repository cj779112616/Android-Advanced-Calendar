package net.alexoro.calendar;

import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;

/**
 * User: UAS
 * Date: 23.06.13
 * Time: 0:09
 */
class DayCellDescription {

    public static class DayStyle {
        public String name;
        public float textSize;
        public ColorStateList textColor;
        public StateListDrawable drawable;
    }

    public int year;
    public int month;
    public int day;
    boolean isEnabled;
    boolean isSelected;
    boolean isPressed;
    public DayStyle dayStyle;

}