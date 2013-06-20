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

    public boolean isEqualWithDate(int row, int column, LocalDate date) {
        boolean r;
        int day = getDayAt(row, column);
        if (isWithinCurrentMonth(row, column)) {
            r = isEqualWithDate(getYear(), getMonth(), day, date);
        } else {
            if (row < 2) {
                previousMonth();
                r = isEqualWithDate(getYear(), getMonth(), day, date);
                nextMonth();
            } else {
                nextMonth();
                r = isEqualWithDate(getYear(), getMonth(), day, date);
                previousMonth();
            }
        }
        return r;
    }

    private boolean isEqualWithDate(int year, int month, int day, LocalDate date) {
        return year == date.getYear()
                && month == date.getMonthOfYear() - 1
                && day == date.getDayOfMonth();
    }

}
