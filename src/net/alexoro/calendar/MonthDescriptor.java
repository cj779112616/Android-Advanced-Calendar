package net.alexoro.calendar;

import android.util.MonthDisplayHelper;

/**
 * User: alex.sorokin@realweb.ru
 * Date: 20.06.13
 * Time: 15:39
 */
class MonthDescriptor extends MonthDisplayHelper {

    public MonthDescriptor(int year, int month, int weekStartDay) {
        super(year, month, weekStartDay);
    }

    public MonthDescriptor(int year, int month) {
        super(year, month);
    }

    public int getRowsCount() {
        return 6;
    }

    public int getColumnsCount() {
        return 7;
    }

}
