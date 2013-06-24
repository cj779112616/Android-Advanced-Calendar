package net.alexoro.calendar;

import org.joda.time.LocalDate;

/**
 * User: UAS
 * Date: 23.06.13
 * Time: 4:38
 */
public interface OnDateChangedListener {
    void onChanged(LocalDate month);
}
