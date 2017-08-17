package com.tanbaron_coffee.myphonecall;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ScheduleActivity extends Activity {
    public static List<Schedule> routines;
    public static List<Schedule> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        routines = new ArrayList<Schedule>();
        events = new ArrayList<Schedule>();

        routines.add(new Schedule());

        Toast.makeText(ScheduleActivity.this, routines.get(0).getDay().toString(), Toast.LENGTH_LONG).show();
    }

    public class Schedule{
        Calendar calendar;
        private String title;
        private String description;
        MainActivity.LocationData eventLocation;

        public Schedule(){calendar=new GregorianCalendar();}
        public Schedule(int year, int month, int day, int hours, int minute, int second){
            calendar=new GregorianCalendar(year,month,day,hours,minute, second);
        }

        public Integer getYear(){return calendar.get(Calendar.YEAR);}
        public Integer getMonth(){return calendar.get(Calendar.MONTH);}
        public Integer getDay(){return calendar.get(Calendar.DATE);}
        public Integer getHour(){return calendar.get(Calendar.HOUR);}
        public Integer getMinute(){return calendar.get(Calendar.MINUTE);}
        public Integer getSecond(){return calendar.get(Calendar.SECOND);}

        public void setTitle(String title){this.title=title;}
        public void setDescription(String desc){description=desc;}
    }

    public static class User{
        private String name;
        private MainActivity.LocationData currentLocation=null;
        private List<Schedule> routines=new ArrayList<Schedule>();;
        private List<Schedule> events=new ArrayList<Schedule>();;

        public void addRoutine(Schedule schedule){routines.add(schedule);}
        public void addEvents(Schedule schedule){events.add(schedule);}

        public String getName(){return name;}
        public MainActivity.LocationData getCurrentLocation(){return currentLocation;}
        public Boolean isRoutinesEmpty(){return routines.isEmpty();}
        public Boolean isEventsEmpty(){return events.isEmpty();}
        public Boolean isCurrentLocationEmpty(){return currentLocation==null;}

        public void setName(String name){this.name=name;}
        public void setCurrentLocation(MainActivity.LocationData location){currentLocation=location;}

    }
}