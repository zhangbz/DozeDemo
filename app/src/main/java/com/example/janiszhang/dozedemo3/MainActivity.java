package com.example.janiszhang.dozedemo3;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private Button mTestButton1;
    private Button mTestButton2;
    private Button mShowState;
    private PowerManager mPm;
    private Button mAlarmButton;
    private Button mGetDataSaverStatusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mTestButton1 = (Button) findViewById(R.id.test1);
        mTestButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(intent);
            }
        });

        mTestButton2 = (Button) findViewById(R.id.test2);

        mTestButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:com.example.janiszhang.dozedemo3"));
                startActivity(intent);
            }
        });

        mShowState = (Button)  findViewById(R.id.showstates);
        mShowState.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if(mPm.isIgnoringBatteryOptimizations("com.example.janiszhang.dozedemo3")) {
                    Toast.makeText(MainActivity.this,"已加入白名单,忽略电量优化",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,"未加入白名单,未忽略电量优化",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAlarmButton = (Button) findViewById(R.id.alarmButton);
        mAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LongRunningService.class);
                startService(intent);
            }
        });

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Data Saver Changed");
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
        this.registerReceiver(broadcastReceiver,intentFilter);




        mGetDataSaverStatusButton = (Button) findViewById(R.id.data_saver_status);
        mGetDataSaverStatusButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Log.i("zhangbz", "onClick");
                Intent intent = new Intent();
                intent.setAction("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
// Checks if the device is on a metered network

                if (connMgr.isActiveNetworkMetered()) {
                    // Checks user’s Data Saver settings.
                    Log.i("zhangbz", "" + connMgr.getRestrictBackgroundStatus());
//                    switch (connMgr.getRestrictBackgroundStatus()) {
//                        case RESTRICT_BACKGROUND_STATUS_ENABLED:
//                            // Background data usage is blocked for this app. Wherever possible,
//                            // the app should also use less data in the foreground.
//
//                        case RESTRICT_BACKGROUND_STATUS_WHITELISTED:
//                            // The app is whitelisted. Wherever possible,
//                            // the app should use less data in the foreground and background.
//
//                        case RESTRICT_BACKGROUND_STATUS_DISABLED:
//                            // Data Saver is disabled. Since the device is connected to a
//                            // metered network, the app should use less data wherever possible.
//                    }
                } else {
                    // The device is not on a metered network.
                    // Use data as required to perform syncs, downloads, and updates.
                }
            }
        });

        scheduleJob(this);
    }


    public static final int MY_BACKGROUND_JOB = 0;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo job = new JobInfo.Builder(
                MY_BACKGROUND_JOB,
                new ComponentName(context, MyJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build();
        js.schedule(job);
    }
}
