package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.content.Intent;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class MyflutteruploaderDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener{
    public MyflutteruploaderDelegate(Activity setupActivity) {
    }

    void saveStateBeforeResult() {

    }

    public void initialize(MethodCall methodCall, MethodChannel.Result result){
        
    }

    public void registerCallback(MethodCall methodCall, MethodChannel.Result result){
        
    }

     public void enqueue(MethodCall methodCall, MethodChannel.Result result){
        
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
