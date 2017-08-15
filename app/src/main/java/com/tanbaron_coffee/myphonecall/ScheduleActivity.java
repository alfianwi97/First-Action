package com.tanbaron_coffee.myphonecall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.GregorianCalendar;

public class ScheduleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        GregorianCalendar c=new GregorianCalendar();
        //c.setTime(new Date());
        Toast.makeText(ScheduleActivity.this, c.getTime().toString(), Toast.LENGTH_LONG).show();
    }

    public class Schedule extends GregorianCalendar{
        private String title;
        private String description;
        MainActivity.LocationData eventLocation;

    }
}