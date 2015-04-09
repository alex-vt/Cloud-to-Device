package com.oleksiykovtun.cloudtodevice.android;

import android.app.Activity;
import android.os.CountDownTimer;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * MainActivity
 */
public class MainActivity extends Activity {

    final public String APP_KEY = "APP_KEY";
    final public String APP_SECRET = "APP_SECRET";

    private CountDownTimer timer;
    private TextView statusTextView;
    private TextView logTextView;
    private Button buttonStart;
    private Button buttonStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((ViewPager) findViewById(R.id.view_pager)).setOffscreenPageLimit(2);
        ((ViewPager) findViewById(R.id.view_pager)).setAdapter(new TabsPagerAdapter(this,
                R.id.tab_status, R.id.tab_log));
        statusTextView = (TextView) findViewById(R.id.status);
        logTextView = (TextView) findViewById(R.id.log);

        Preferences.set(this, Preferences.APP_KEY, APP_KEY);
        Preferences.set(this, Preferences.APP_SECRET, APP_SECRET);
        // todo string to xml and ui settings
        Preferences.set(this, Preferences.STATUS, "Ready.");
        Preferences.set(this, Preferences.BACKUP_INTERVAL_SECONDS, 240);
        Preferences.set(this, Preferences.UI_UPDATE_INTERVAL_MILLISECONDS, 1000);

        setupButtons();
        runUpdateTimer();
        CloudApi.authenticate(this);
    }

    public void setupButtons() {
        buttonStart = (Button) findViewById(R.id.button_start);
        buttonStop = (Button) findViewById(R.id.button_stop);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int backupIntervalMilliseconds = 1000 * Preferences.getInt(getApplicationContext(),
                        Preferences.BACKUP_INTERVAL_SECONDS);
                BackupScheduler.startRepeated(getApplicationContext(), backupIntervalMilliseconds);
                Preferences.set(getApplicationContext(), Preferences.AUTO_START, true);
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupScheduler.stop(getApplicationContext());
                Preferences.set(getApplicationContext(), Preferences.AUTO_START, false);
            }
        });
        final Button buttonClearLog = (Button) findViewById(R.id.button_clear_log);
        buttonClearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.clear(getApplicationContext(), Preferences.LOG);
            }
        });
        final Button buttonUnlink = (Button) findViewById(R.id.button_unlink);
        buttonUnlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.clear(getApplicationContext(), Preferences.CURSOR);
                Preferences.clear(getApplicationContext(), Preferences.TOKEN);
                Preferences.set(getApplicationContext(), Preferences.AUTO_START, false);
                BackupScheduler.stop(getApplicationContext());
                finish();
            }
        });
    }

    private void runUpdateTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(Integer.MAX_VALUE, Preferences.getInt(getApplicationContext(),
                Preferences.UI_UPDATE_INTERVAL_MILLISECONDS)) {

            public void onTick(long millisUntilFinished) {
                setTextIfUpdated(statusTextView, Preferences.STATUS);
                setTextIfUpdated(logTextView, Preferences.LOG);
                setButtonsServiceRunning(buttonStart, buttonStop,
                        BackupScheduler.isScheduled(getApplicationContext()));
            }

            // todo extract to AsyncTask
            private void setTextIfUpdated(TextView textView, String tag) {
                String newText = Preferences.get(getApplicationContext(), tag);
                if (! textView.getText().equals(newText)) {
                    textView.setText(newText);
                }
            }

            public void onFinish() { }

        }.start();
    }

    private void setButtonsServiceRunning(Button buttonStart, Button buttonStop, boolean running) {
        buttonStart.setEnabled(! running);
        buttonStop.setEnabled(running);
    }

    protected void onResume() {
        super.onResume();
        if (CloudApi.isAuthenticationSuccessful(this)) {
            CloudApi.finalizeAuthentication(this);
        }
    }

}
