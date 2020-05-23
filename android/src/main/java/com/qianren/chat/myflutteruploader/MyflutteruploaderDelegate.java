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
import java.util.concurrent.TimeUnit;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class MyflutteruploaderDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener{
    private static final String TAG = "flutter_upload_task";
    
    public static final String SHARED_PREFERENCES_KEY = "com.qianren.chat.io.uploader.pref";
    public static final String CALLBACK_DISPATCHER_HANDLE_KEY = "uploader_callback_dispatcher_handle_key";

    private int taskIdKey = 0;
    private final String[] validHttpMethods = new String[] {"POST", "PUT", "PATCH"};
    
    private Context context;
    private TaskDbHelper dbHelper;
    private TaskDao taskDao;
    private long callbackHandle;
    private MethodChannel flutterChannel;
    private int connectionTimeout = 3600;
    private Map<String, String> tasks = new HashMap<>();
    
    public MyflutteruploaderDelegate(Activity setupActivity,Context contex,MethodChannel flutterChannel) {
        this.context = contex;
        this.flutterChannel = flutterChannel;
        dbHelper = TaskDbHelper.getInstance(context);
        taskDao = new TaskDao(dbHelper);
    }

    void saveStateBeforeResult() {

    }

    private WorkRequest buildRequest(UploadTask task) {
        Gson gson = new Gson();

        Data.Builder dataBuilder =
                new Data.Builder()
                        .putString(UploadWorker.ARG_URL, task.getURL())
                        .putString(UploadWorker.ARG_METHOD, task.getMethod())
                        .putInt(UploadWorker.ARG_REQUEST_TIMEOUT, task.getTimeout())
                        .putBoolean(UploadWorker.ARG_SHOW_NOTIFICATION, task.canShowNotification())
                        .putBoolean(UploadWorker.ARG_BINARY_UPLOAD, task.isBinaryUpload())
                        .putString(UploadWorker.ARG_UPLOAD_REQUEST_TAG, task.getTag())
                        .putInt(UploadWorker.ARG_ID, task.getId());

        List<FileItem> files = task.getFiles();

        String fileItemsJson = gson.toJson(files);
        dataBuilder.putString(UploadWorker.ARG_FILES, fileItemsJson);

        if (task.getHeaders() != null) {
            String headersJson = gson.toJson(task.getHeaders());
            dataBuilder.putString(UploadWorker.ARG_HEADERS, headersJson);
        }

        if (task.getParameters() != null) {
            String parametersJson = gson.toJson(task.getParameters());
            dataBuilder.putString(UploadWorker.ARG_DATA, parametersJson);
        }

        return new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setConstraints(
                        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.SECONDS)
                .setInputData(dataBuilder.build())
                .build();
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
         taskIdKey++;
         String url = call.argument("url");
         String method = call.argument("method");
         List<Map<String, String>> files = call.argument("files");
         Map<String, String> parameters = call.argument("data");
         Map<String, String> headers = call.argument("headers");
         boolean showNotification = call.argument("show_notification");
         String tag = call.argument("tag");

         List<String> methods = Arrays.asList(validHttpMethods);

         if (method == null) {
             method = "POST";
         }

         if (!methods.contains(method.toUpperCase())) {
             result.error("invalid_method", "Method must be either POST | PUT | PATCH", null);
             return;
         }

         List<FileItem> items = new ArrayList<>();

         for (Map<String, String> file : files) {
             items.add(FileItem.fromJson(file));
         }

         WorkRequest request =
                 buildRequest(
                         new UploadTask(
                                 taskIdKey,
                                 url,
                                 method,
                                 items,
                                 headers,
                                 parameters,
                                 connectionTimeout,
                                 showNotification,
                                 false,
                                 tag));
         WorkManager.getInstance(context).enqueue(request);
         String taskId = request.getId().toString();
         if (!tasks.containsKey(taskId)) {
             tasks.put(taskId, tag);
         }
         result.success(taskId);
         sendUpdateProgress(taskId, UploadStatus.ENQUEUED, 0);
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
