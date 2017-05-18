package com.example.y1247.expectioncatcher.seriver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by y1247 on 2017/5/17.
 */

public class TestService extends Service {

    MyBinder myBinder = new MyBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("DSF", "onBind: ");
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("DSF", "onCreate: ");
    }

    public class MyBinder extends Binder {

        public void startDownload() {
            Log.d("TAG", "startDownload() executed");
            // 执行具体的下载任务
            throw new ClassCastException();
        }

    }
}
