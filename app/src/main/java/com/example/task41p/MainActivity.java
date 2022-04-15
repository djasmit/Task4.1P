package com.example.task41p;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintStream;

public class MainActivity extends AppCompatActivity {
    String lastTime, LASTTIME = "LAST TIME";
    String lastTask, LASTTASK = "LAST TASK";
    String TIMESTOPPED = "TIME STOPPED";
    String TIMERRUNNING = "TIME RUNNING";
    String TIMERPAUSED = "TIME PAUSED";

    long timeWhenStopped = 0;
    boolean timerRunning, timerPaused;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //import shared preference values
        getSharedPref();
        handleRotation(savedInstanceState);
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //if timer wasn't paused we need store its final time here before restarting
        if (timerRunning && !timerPaused) {
            Chronometer timer = findViewById(R.id.timer);
            timeWhenStopped = SystemClock.elapsedRealtime() - timer.getBase();
        }

        //we need to carry information about if the timer is running, paused, and when it stopped
        outState.putBoolean(TIMERRUNNING, timerRunning);
        outState.putBoolean(TIMERPAUSED, timerPaused);
        outState.putLong(TIMESTOPPED, timeWhenStopped);
    }

    public void startClick(View view) {

        //we need a task entered before we can attempt to run anything
        EditText taskText = findViewById(R.id.nameInput);
        String inputString = taskText.getText().toString().trim();
        if (inputString.trim().length() == 0 || inputString == "" || inputString == null) {
            Toast.makeText(MainActivity.this, R.string.no_input_error, Toast.LENGTH_SHORT).show();
            return;
        }

        //don't want this button being hit when already running
        if (timerRunning && !timerPaused) {
            Toast.makeText(MainActivity.this, R.string.isRunningError, Toast.LENGTH_SHORT).show();
            return; //insert toast later
        }

        //calculate our starting time and go
        Chronometer timer = findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
        timer.start();

        //make sure we know timer is running but not paused
        timerRunning = true;
        timerPaused = false;
    }

    public void pauseClick(View view) {
        Chronometer timer = findViewById(R.id.timer);

        //don't want to hit this if timer isn't running or already paused
        if (!timerRunning || timerPaused) {
            Toast.makeText(MainActivity.this, R.string.isPausedError, Toast.LENGTH_SHORT).show();
            return; //insert toast later
        }

        //calculate the time when we paused and stop the timer
        timeWhenStopped = SystemClock.elapsedRealtime() - timer.getBase();
        timer.stop();

        //make sure we know timer is paused
        timerPaused = true;
    }

    //stop button ends the timer and saves the task and time for next time app is opened
    public void stopClick(View view) {
        //don't want to hit this if timer is neither running nor paused
        if (!(timerRunning || timerPaused)) {
            Toast.makeText(MainActivity.this, R.string.isPausedError, Toast.LENGTH_SHORT).show();
            return;
        }

        Chronometer timer = findViewById(R.id.timer);
        EditText nameInput = findViewById(R.id.nameInput);
        TextView lastTimeText = findViewById(R.id.lastTimeText);

        //save time and last task for next time
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        lastTask = nameInput.getText().toString();
        lastTime = timer.getText().toString();
        editor.putString(LASTTASK, lastTask);
        editor.putString(LASTTIME, lastTime);
        lastTimeText.setText("You spent " + lastTime + " on " + lastTask + " last time.");

        //note to self: save hours worth of trouble-shooting by not forgetting to include this step
        editor.apply();

        //stop timer and reset, make sure we know timer is neither running nor paused
        timer.stop();
        timeWhenStopped = 0;
        timerRunning = false;
        timerPaused = false;
        nameInput.setText(null);
        timer.setBase(SystemClock.elapsedRealtime());
    }

    //imports data from saved shared preferences
    private void getSharedPref() {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        lastTime = sharedPref.getString(LASTTIME, "00:00");
        lastTask = sharedPref.getString(LASTTASK, "...");
    }

    //get saved state values for if the timer was running, paused, and what time we stopped on
    private void handleRotation(Bundle savedInstanceState) {
        TextView lastTimeText = findViewById(R.id.lastTimeText);
        if (savedInstanceState != null) {
            timerRunning = savedInstanceState.getBoolean(TIMERRUNNING);
            timerPaused = savedInstanceState.getBoolean(TIMERPAUSED);
            timeWhenStopped = savedInstanceState.getLong(TIMESTOPPED);
        }
        lastTimeText.setText("You spent " + lastTime + " on " + lastTask + " last time.");

        //if our timer is running we need to reset the base so it knows where to start
        if (timerRunning) {
            Chronometer timer = findViewById(R.id.timer);

            //calculate time we were at last
            timer.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);

            //if timer was paused before, pause it again, otherwise start it again
            if (timerPaused) { timer.stop(); }
            else { timer.start(); }
        }
    }

}