package com.example.y1247.expectioncatcher.application;

import android.app.Application;

import com.example.y1247.expectioncatcher.catcher.MyUncaughtExceptionHandler;

/**
 * Created by y1247 on 2017/5/11.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MyUncaughtExceptionHandler caught = MyUncaughtExceptionHandler.getINSTANCE();
        caught.init(this);
    }
}
