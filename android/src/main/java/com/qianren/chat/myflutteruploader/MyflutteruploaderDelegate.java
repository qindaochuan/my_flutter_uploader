package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class MyflutteruploaderDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener{
    public static final String SHARED_PREFERENCES_KEY = "com.qianren.chat.io.uploader.pref";
    public static final String CALLBACK_DISPATCHER_HANDLE_KEY = "uploader_callback_dispatcher_handle_key";
    
    private Context context;
    private TaskDbHelper dbHelper;
    private TaskDao taskDao;
    private long callbackHandle;
    private MethodChannel flutterChannel;
    
    public MyflutteruploaderDelegate(Activity setupActivity,Context contex,MethodChannel flutterChannel) {
        dbHelper = TaskDbHelper.getInstance(context);
        taskDao = new TaskDao(dbHelper);
    }

    void saveStateBeforeResult() {

    }
    private void sendUpdateProgress(String id, int status, int progress) {
        Map<String, Object> args = new HashMap<>();
        args.put("task_id", id);
        args.put("status", status);
        args.put("progress", progress);
        flutterChannel.invokeMethod("updateProgress", args);
    }
    
    public void initialize(MethodCall call, MethodChannel.Result result){
        List args = (List) call.arguments;
        long callbackHandle = Long.parseLong(args.get(0).toString());

        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        pref.edit().putLong(CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle).apply();

        result.success(null);
    }

    public void registerCallback(MethodCall call, MethodChannel.Result result){
        List args = (List) call.arguments;
        callbackHandle = Long.parseLong(args.get(0).toString());
        result.success(null);
    }

     public void enqueue(MethodCall call, MethodChannel.Result result){
        
     }

    public void loadTasks(MethodCall call, MethodChannel.Result result){
        
    }

    public void loadTasksWithRawQuery(MethodCall call, MethodChannel.Result result){

    }

    public void cancel(MethodCall call, MethodChannel.Result result){

    }

    public void cancelAll(MethodCall call, MethodChannel.Result result){

    }

    public void pause(MethodCall call, MethodChannel.Result result){

    }

    public void resume(MethodCall call, MethodChannel.Result result){

    }

    public void retry(MethodCall call, MethodChannel.Result result){

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        return false;
    }
}
