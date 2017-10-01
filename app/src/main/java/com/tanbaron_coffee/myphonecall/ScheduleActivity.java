package com.tanbaron_coffee.myphonecall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.tanbaron_coffee.myphonecall.MainActivity.user;
///////////////////////////////////////////NOTE/////////////////////////////////////////////////////
// This module is still under construction
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ScheduleActivity extends Activity {
    public static List<Schedule> routines;
    public static List<Schedule> events;
    private Button buttonLogin;
    private Button buttonMap;
    private Button buttonUrgentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        buttonMap = (Button) findViewById(R.id.btnMap);
        buttonLogin = (Button) findViewById(R.id.btnLogin);
        buttonUrgentCall = (Button) findViewById(R.id.btnCall);

        routines = new ArrayList<Schedule>();
        events = new ArrayList<Schedule>();

        routines.add(new Schedule());

//        Toast.makeText(ScheduleActivity.this, routines.get(0).getDay().toString(), Toast.LENGTH_LONG).show();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ScheduleActivity.this, AccountActivity.class));
            }
        });
        buttonUrgentCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scheduleIntent = new Intent(getApplicationContext(), ScheduleActivity.class);
                startActivity(scheduleIntent);
            }
        });
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.isCurrentLocationEmpty()){
                    Toast.makeText(ScheduleActivity.this, "Failed to get location", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(mapIntent);
            }
        });
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