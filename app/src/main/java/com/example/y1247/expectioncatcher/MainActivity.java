package com.example.y1247.expectioncatcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.y1247.expectioncatcher.seriver.TestService;

import java.io.IOException;
import java.io.InterruptedIOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView mTextMessage;

    private TestService.MyBinder myBinder;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (TestService.MyBinder) service;
            Log.i(TAG, "onServiceConnected: ");
            myBinder.startDownload();
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
//                    onClick();
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
//                    onClick();
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    onClick();
                    return true;
            }
            return false;
        }

    };

    private void onClick(){
        boolean flag = true;
//        while(true){
//            if(!flag) break;
//        }
        Intent bindIntent = new Intent(this, TestService.class);
        startService(bindIntent);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
//        throw new ClassCastException();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
