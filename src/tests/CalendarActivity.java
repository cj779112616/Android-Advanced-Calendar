package tests;

import android.app.Activity;
import android.os.Bundle;
import net.alexoro.calendar.R;

/**
 * User: UAS
 * Date: 19.06.13
 * Time: 23:25
 */
public class CalendarActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tests_calendar);
    }

}