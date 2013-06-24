package net.alexoro.calendar;

import android.util.MonthDisplayHelper;
import org.joda.time.LocalDate;

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

    public LocalDate getLocalDate(int row, int column) {
        LocalDate r;
        int day = getDayAt(row, column);

        if (isWithinCurrentMonth(row, column)) {
            r = new LocalDate(getYear(), getMonth() + 1, day);
        } else {
            if (row < 2) {
                previousMonth();
                r = new LocalDate(getYear(), getMonth() + 1, day);
                nextMonth();
            } else {
                nextMonth();
                r = new LocalDate(getYear(), getMonth() + 1, day);
                previousMonth();
            }
        }
        return r;
    }


    //TODO bad code
    public int compareToDate(int row, int column, LocalDate date) {
        int year = getYear();
        int month = getMonth();
        int day = getDayAt(row, column);

        if (!isWithinCurrentMonth(row, column)) {
            if (row < 2) {
                previousMonth();
                year = getYear();
                month = getMonth();
                nextMonth();
            } else {
                nextMonth();
                year = getYear();
                month = getMonth();
                previousMonth();
            }
        }

        Integer source = year * 10000 + (month + 1) * 100 + day;
        Integer target = date.getYear() * 10000 + date.getMonthOfYear() * 100 + date.getDayOfMonth();
        return source.compareTo(target);
    }

}
