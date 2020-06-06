package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationManagerCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

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
         int fileType = call.argument("fileType");
         String method = call.argument("method");
         String headers = call.argument("headers");
         String data = call.argument("data");
         int requestTimeoutInSeconds = call.argument("requestTimeoutInSeconds");
         boolean showNotification = call.argument("showNotification");

         List<String> methods = Arrays.asList(validHttpMethods);

         if (method == null) {
             method = "POST";
         }

         if (!methods.contains(method.toUpperCase())) {
             result.error("invalid_method", "Method must be either POST | PUT | PATCH", null);
             return;
         }

         WorkRequest request = buildRequest(uploadurl,localePath, fieldname, method, headers,
                 data, requestTimeoutInSeconds, showNotification, false, false);
         WorkManager.getInstance(context).enqueue(request);
         String taskId = request.getId().toString();
         result.success(taskId);
         sendUpdateProgress(taskId, UploadStatus.ENQUEUED, 0);
         taskDao.insertOrUpdateNewTask(taskId, UploadStatus.ENQUEUED, 0, uploadurl, localePath, fileType,fieldname,
                 method, headers, data, requestTimeoutInSeconds, showNotification, false);
     }

    public void enqueueCompressVideoThenUpload(MethodCall call, MethodChannel.Result result){

    }

    public void loadTasks(MethodCall call, MethodChannel.Result result){
        List<UploadTask> tasks = taskDao.loadAllTasks();
        List<Map> array = new ArrayList<>();
        for (UploadTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("upload_taskId", task.getUpload_taskId());
            item.put("upload_status", task.getUpload_status());
            item.put("upload_progress", task.getUpload_progress());
            item.put("uploadurl", task.getUploadurl());
            item.put("downloadurl", task.getDownloadurl());
            item.put("localePath", task.getLocalePath());
            item.put("fileType", task.getFileType());
            item.put("fieldname", task.getFieldname());
            item.put("method", task.getMethod());
            item.put("headers", task.getHeaders());
            item.put("data", task.getData());
            item.put("requestTimeoutInSeconds", task.getRequestTimeoutInSeconds());
            item.put("showNotification", task.isShowNotification());
            item.put("binaryUpload", task.isBinaryUpload());
            item.put("resumable", task.isResumable());
            item.put("upload_timeCreated", task.getUpload_timeCreated());
            item.put("compress_taskId", task.getCompress_taskId());
            item.put("compress_status", task.getCompress_status());
            item.put("compress_progress", task.getCompress_progress());
            item.put("compress_Path", task.getCompress_path());
            item.put("compressTimeCreated", task.getCompressTimeCreated());
            array.add(item);
        }
        result.success(array);
    }

    public void loadTasksWithRawQuery(MethodCall call, MethodChannel.Result result){
        String query = call.argument("query");
        List<UploadTask> tasks = taskDao.loadTasksWithRawQuery(query);
        List<Map> array = new ArrayList<>();
        for (UploadTask task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("upload_taskId", task.getUpload_taskId());
            item.put("upload_status", task.getUpload_status());
            item.put("upload_progress", task.getUpload_progress());
            item.put("uploadurl", task.getUploadurl());
            item.put("downloadurl", task.getDownloadurl());
            item.put("localePath", task.getLocalePath());
            item.put("fileType", task.getFileType());
            item.put("fieldname", task.getFieldname());
            item.put("method", task.getMethod());
            item.put("headers", task.getHeaders());
            item.put("data", task.getData());
            item.put("requestTimeoutInSeconds", task.getRequestTimeoutInSeconds());
            item.put("showNotification", task.isShowNotification());
            item.put("binaryUpload", task.isBinaryUpload());
            item.put("resumable", task.isResumable());
            item.put("upload_timeCreated", task.getUpload_timeCreated());
            item.put("compress_taskId", task.getCompress_taskId());
            item.put("compress_status", task.getCompress_status());
            item.put("compress_progress", task.getCompress_progress());
            item.put("compress_Path", task.getCompress_path());
            item.put("compressTimeCreated", task.getCompressTimeCreated());
            array.add(item);
        }
        result.success(array);
    }

    public void cancel(MethodCall call, MethodChannel.Result result){
        String taskId = call.argument("task_id");
        UploadTask task = taskDao.loadTask(taskId);
        if(task.getUpload_status() == UploadStatus.FAILED){
            taskDao.deleteTask(taskId);
            result.success(null);
        } else if(task.getUpload_status() == UploadStatus.CANCELED){
            taskDao.deleteTask(taskId);
            result.success(null);
        } else if(task.getUpload_status() == UploadStatus.ENQUEUED){
            taskDao.deleteTask(taskId);
            result.success(null);
        } else {
            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(taskId));
            result.success(null);
        }
    }

    public void cancelAll(MethodCall call, MethodChannel.Result result){
        //WorkManager.getInstance(context).cancelAllWorkByTag(TAG);
        //result.success(null);
    }

    public void pause(MethodCall call, MethodChannel.Result result){

    }

    public void resume(MethodCall call, MethodChannel.Result result){

    }

    public void retry(MethodCall call, MethodChannel.Result result){
        String taskId = call.argument("task_id");
        UploadTask task = taskDao.loadTask(taskId);
        if (task != null) {
            if (task.getUpload_status() == UploadStatus.FAILED || task.getUpload_status() == UploadStatus.CANCELED) {
                WorkRequest request = buildRequest(task.getUploadurl(),task.getLocalePath(), task.getFieldname(), task.getMethod(), task.getHeaders(),
                        task.getData(), task.getRequestTimeoutInSeconds(), task.isShowNotification(), task.isBinaryUpload(), false);
                String newTaskId = request.getId().toString();
                result.success(newTaskId);
                sendUpdateProgress(newTaskId, UploadStatus.ENQUEUED, task.getUpload_progress());
                taskDao.updateTask(taskId, newTaskId, UploadStatus.ENQUEUED, task.getUpload_progress(), false);
                WorkManager.getInstance(context).enqueue(request);
            } else {
                result.error("invalid_status", "only failed and canceled task can be retried", null);
            }
        } else {
            result.error("invalid_task_id", "not found task corresponding to given task id", null);
        }
    }

    public void removeCompleted(MethodCall call, MethodChannel.Result result){
        String taskId = call.argument("task_id");
        UploadTask task = taskDao.loadTask(taskId);
        if (task != null) {
            if (task.getUpload_status() == UploadStatus.COMPLETE) {
                taskDao.deleteTask(taskId);
                NotificationManagerCompat.from(context).cancel(task.getPrimaryId());
                result.success(null);
            } else{
                result.error("invalid_task_status", "task must be completed", null);
            }
        } else {
            result.error("invalid_task_id", "not found task corresponding to given task id", null);
        }
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
