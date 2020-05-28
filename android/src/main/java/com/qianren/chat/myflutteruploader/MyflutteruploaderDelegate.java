package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class MyflutteruploaderDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener{
    private static final String TAG = "flutter_upload_task";
    
    public static final String SHARED_PREFERENCES_KEY = "com.qianren.chat.io.uploader.pref";
    public static final String CALLBACK_DISPATCHER_HANDLE_KEY = "uploader_callback_dispatcher_handle_key";

    private final String[] validHttpMethods = new String[] {"POST", "PUT", "PATCH"};
    
    private Context context;
    private TaskDbHelper dbHelper;
    private TaskDao taskDao;
    private long callbackHandle;
    private MethodChannel flutterChannel;
    private int connectionTimeout = 3600;
    
    public MyflutteruploaderDelegate(Activity setupActivity,Context contex,MethodChannel flutterChannel) {
        this.context = contex;
        this.flutterChannel = flutterChannel;
        dbHelper = TaskDbHelper.getInstance(context);
        taskDao = new TaskDao(dbHelper);
    }

    void saveStateBeforeResult() {

    }

    private WorkRequest buildRequest(String uploadurl,String localePath, String fieldname, String method, String headers,
                                      String data, int requestTimeoutInSeconds, boolean showNotification, boolean binaryUpload, boolean resumable){
        Data.Builder dataBuilder =
                new Data.Builder()
                        .putString(UploadWorker.ARG_UPLOAD_URL, uploadurl)
                        .putString(UploadWorker.ARG_LOCALE_PATH, localePath)
                        .putString(UploadWorker.ARG_FIELD_NAME, fieldname)
                        .putString(UploadWorker.ARG_METHOD, method)
                        .putString(UploadWorker.ARG_HEADERS, headers)
                        .putString(UploadWorker.ARG_DATA,data)
                        .putInt(UploadWorker.ARG_REQUEST_TIMEOUT_INSECONDS, requestTimeoutInSeconds)
                        .putBoolean(UploadWorker.ARG_SHOW_NOTIFICATION, showNotification)
                        .putBoolean(UploadWorker.ARG_BINARY_UPLOAD, binaryUpload)
                        .putBoolean(UploadWorker.ARG_RESUMABLE,resumable);

        return new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setConstraints(
                        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.SECONDS)
                .setInputData(dataBuilder.build())
                .build();
    }
    
    public void sendUpdateProgress(String id, int status, int progress) {
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
         String uploadurl = call.argument("uploadurl");
         String localePath = call.argument("localePath");
         String fieldname = call.argument("fieldname");
         String method = call.argument("method");
         String headers = call.argument("headers");
         String data = call.argument("data");
         int requestTimeoutInSeconds = call.argument("requestTimeoutInSeconds");
         boolean showNotification = call.argument("showNotification");
         boolean binaryUpload = call.argument("binaryUpload");

         List<String> methods = Arrays.asList(validHttpMethods);

         if (method == null) {
             method = "POST";
         }

         if (!methods.contains(method.toUpperCase())) {
             result.error("invalid_method", "Method must be either POST | PUT | PATCH", null);
             return;
         }

         WorkRequest request = buildRequest(uploadurl,localePath, fieldname, method, headers,
                 data, requestTimeoutInSeconds, showNotification, binaryUpload, false);
         WorkManager.getInstance(context).enqueue(request);
         String taskId = request.getId().toString();
         result.success(taskId);
         sendUpdateProgress(taskId, UploadStatus.ENQUEUED, 0);
         taskDao.insertOrUpdateNewTask(taskId, UploadStatus.ENQUEUED, 0, uploadurl, localePath, fieldname,
                 method, headers, data, requestTimeoutInSeconds, showNotification, binaryUpload);
     }

    public void loadTasks(MethodCall call, MethodChannel.Result result){
        
    }

    public void loadTasksWithRawQuery(MethodCall call, MethodChannel.Result result){

    }

    public void cancel(MethodCall call, MethodChannel.Result result){
        String taskId = call.argument("task_id");
        WorkManager.getInstance(context).cancelWorkById(UUID.fromString(taskId));
        result.success(null);
    }

    public void cancelAll(MethodCall call, MethodChannel.Result result){
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG);
        result.success(null);
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

    public void sendFailed(
            String id, int status, int statusCode, String code, String message, String[] details) {

        Map<String, Object> args = new HashMap<>();
        args.put("task_id", id);
        args.put("status", status);
        args.put("statusCode", statusCode);
        args.put("code", code);
        args.put("message", message);
        args.put("details", details != null ? new ArrayList<>(Arrays.asList(details)) : null);
        flutterChannel.invokeMethod("uploadFailed", args);
    }

    public void sendCompleted(String id, int status, String response, Map headers) {
        Map<String, Object> args = new HashMap<>();
        args.put("task_id", id);
        args.put("status", status);
        args.put("statusCode", 200);
        args.put("message", response);
        args.put("headers", headers);
        flutterChannel.invokeMethod("uploadCompleted", args);
    }
}
