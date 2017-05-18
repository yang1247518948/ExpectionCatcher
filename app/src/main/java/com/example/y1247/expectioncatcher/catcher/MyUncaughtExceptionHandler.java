package com.example.y1247.expectioncatcher.catcher;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.support.compat.BuildConfig;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by y1247 on 2017/5/11.
 */

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "MyUncaughtException";

    public static boolean ISPRINTSTACK = false;

    //上下文
    private Context context;

    //默认异常处理
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    //设备信息
    private Map<String,String> infos = new LinkedHashMap<String,String>();

    //单例实例
    private static MyUncaughtExceptionHandler INSTANCE;

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    private String nameString;

    /**
     * 单例私有构造
     */
    private MyUncaughtExceptionHandler(){}

    public static MyUncaughtExceptionHandler getINSTANCE(){
        if (INSTANCE==null){
            INSTANCE = new MyUncaughtExceptionHandler();
        }
        return INSTANCE;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!processException(e) && defaultExceptionHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            defaultExceptionHandler.uncaughtException(t, e);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Log.e(TAG, "error : ", e);
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 初始化抓取类
     * @param context 应用上下文
     */
    public void init(Context context){
        this.context = context;
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        //下面用于获取用户名
        nameString = "获取了的用户信息";
        Log.i(TAG, "init: " );
    }

    /**
     * 异常处理
     * @param e 异常
     * @return 处理成功则返回true
     */
    private boolean processException(Throwable e){
        if(e==null){
            return false;
        }
//        if(ISPRINTSTACK){
//            e.printStackTrace();
//        }
        Log.i(TAG, "processException: ");
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, "很抱歉,程序出现异常,正在收集日志，即将退出", Toast.LENGTH_LONG)
                        .show();
                Looper.loop();
            }
        }.start();


        getDeviceInfo();
        String path = writeLogToDevice(e);
        if(path!=null){
            sendToService(path);
        }

        return true;
    }

    /**
     * 获取设备信息
     */
    private void getDeviceInfo(){
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            infos.put("IMEI", tm.getDeviceId());
            infos.put("DeviceSoftwareVersion",tm.getDeviceSoftwareVersion());
            infos.put("SimSerialNumber",tm.getSimSerialNumber());
            infos.put("SubscriberId",tm.getSubscriberId());
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
//                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }


    }

    /**
     * 将崩溃信息写入到存储中
     * @param ex
     */
    private String writeLogToDevice(Throwable ex){
        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String,String> entry:infos.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            Log.i(TAG, "writeLogToDevice: "+ key+"---"+value);
            sb.append(key+"="+value+"\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = nameString + "-" + time + "-" + timestamp
                    + ".log";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path =  Environment.getExternalStorageDirectory().getPath()+"/Android/data/" + BuildConfig.APPLICATION_ID+"/";
                File dir = new File(path);
                Log.i(TAG, "writeLogToDevice: "+path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
                Log.i(TAG, "writeLogToDevice: " + path);
                return path;
            }
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 上传待传目录中所有log到服务器中
     * 待完善
     */
    private void sendToService(String path){

    }
}
