package com.qianren.chat.myflutteruploader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadWorker extends Worker implements CountProgressListener {
  public static final String ARG_UPLOAD_URL = "uploadurl";
  public static final String ARG_LOCALE_PATH = "localePath";
  public static final String ARG_FIELD_NAME = "fieldname";
  public static final String ARG_METHOD = "method";
  public static final String ARG_HEADERS = "headers";
  public static final String ARG_DATA = "data";
  public static final String ARG_REQUEST_TIMEOUT_INSECONDS = "requestTimeoutInSeconds";
  public static final String ARG_SHOW_NOTIFICATION = "showNotification";
  public static final String ARG_BINARY_UPLOAD = "binaryUpload";
  public static final String ARG_RESUMABLE = "resumable";

  public static final String EXTRA_STATUS_CODE = "statusCode";
  public static final String EXTRA_STATUS = "status";
  public static final String EXTRA_ERROR_MESSAGE = "errorMessage";
  public static final String EXTRA_ERROR_CODE = "errorCode";
  public static final String EXTRA_ERROR_DETAILS = "errorDetails";
  public static final String EXTRA_RESPONSE = "response";
  public static final String EXTRA_ID = "id";
  public static final String EXTRA_HEADERS = "headers";
  private static final String TAG = UploadWorker.class.getSimpleName();
  private static final String CHANNEL_ID = "FLUTTER_UPLOADER_NOTIFICATION";
  private static final int UPDATE_STEP = 0;
  private static final int DEFAULT_ERROR_STATUS_CODE = 500;

  private NotificationCompat.Builder builder;
  private boolean showNotification;
  private String msgStarted, msgInProgress, msgCanceled, msgFailed, msgComplete;
  private int lastProgress = 0;
  private int lastNotificationProgress = 0;
  private String tag;
  private int primaryId;
  private Call call;
  private boolean isCancelled = false;

  private TaskDbHelper dbHelper;
  private TaskDao taskDao;

  public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {
    Context context = getApplicationContext();
    dbHelper = TaskDbHelper.getInstance(context);
    taskDao = new TaskDao(dbHelper);

    String uploadurl = getInputData().getString(ARG_UPLOAD_URL);
    String localePath = getInputData().getString(ARG_LOCALE_PATH);
    String fieldname = getInputData().getString(ARG_FIELD_NAME);
    String method = getInputData().getString(ARG_METHOD);
    String headers = getInputData().getString(ARG_HEADERS);
    String data = getInputData().getString(ARG_DATA);
    int requestTimeoutInSeconds = getInputData().getInt(ARG_REQUEST_TIMEOUT_INSECONDS,3600);
    showNotification = getInputData().getBoolean(ARG_SHOW_NOTIFICATION,false);
    boolean binaryUpload = getInputData().getBoolean(ARG_BINARY_UPLOAD,false);
    boolean resumable = getInputData().getBoolean(ARG_RESUMABLE,false);

    if (tag == null) {
      tag = getId().toString();
    }

    int statusCode = 200;
    Resources res = getApplicationContext().getResources();
    msgStarted = res.getString(R.string.flutter_uploader_notification_started);
    msgInProgress = res.getString(R.string.flutter_uploader_notification_in_progress);
    msgCanceled = res.getString(R.string.flutter_uploader_notification_canceled);
    msgFailed = res.getString(R.string.flutter_uploader_notification_failed);
    msgComplete = res.getString(R.string.flutter_uploader_notification_complete);

    Log.d(TAG, "UploadWorker{uploadurl=" + uploadurl + ",localePath =" + localePath + ",header=" + headers + ",isResume=" + resumable);

    UploadTask task = taskDao.loadTask(getId().toString());
    primaryId = task.primaryId;

    buildNotification(context);

    updateNotification(context, localePath, UploadStatus.RUNNING, task.progress, null);
    taskDao.updateTask(getId().toString(), UploadStatus.RUNNING, 0);

    try {
//      Map<String, String> headers = null;
//      Map<String, String> parameters = null;
//      List<FileItem> files = new ArrayList<>();
//      Gson gson = new Gson();
//      Type type = new TypeToken<Map<String, String>>() {}.getType();
//      Type fileItemType = new TypeToken<List<FileItem>>() {}.getType();
//
//      if (headersJson != null) {
//        headers = gson.fromJson(headersJson, type);
//      }
//
//      if (parametersJson != null) {
//        parameters = gson.fromJson(parametersJson, type);
//      }
//
//      if (filesJson != null) {
//        files = gson.fromJson(filesJson, fileItemType);
//      }
      Gson gson = new Gson();

      final RequestBody innerRequestBody;

      if (binaryUpload) {
        File file = new File(localePath);

        if (!file.exists()) {
          Log.d(TAG, "File does not exists -> file:" + localePath);
          taskDao.updateTask(getId().toString(), UploadStatus.FAILED, 0);
          return Result.failure(
              createOutputErrorData(
                  UploadStatus.FAILED,
                  DEFAULT_ERROR_STATUS_CODE,
                  "invalid_files",
                  "There are no items to upload",
                  null));
        }

        //String mimeType = GetMimeType(localePath);
        //MediaType contentType = MediaType.parse(mimeType);
        //innerRequestBody = RequestBody.create(file, contentType);
        innerRequestBody = RequestBody.create(file, MediaType.parse("multipart/form-data"));
      } else {
        File file = new File(localePath);
        String fileNameStr = file.getName();
        Type dataMapType = new TypeToken<Map<String, String>>() {}.getType();;
        Map<String, String> dataMap = gson.fromJson(data,dataMapType);
        MultipartBody.Builder formRequestBuilder = prepareRequest(dataMap, null);
        //String mimeType = GetMimeType(localePath);
        //MediaType contentType = MediaType.parse(mimeType);
        RequestBody fileBody = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        //RequestBody fileBody = RequestBody.create(file,MediaType.parse("application/octet-stream"));
        formRequestBuilder.addFormDataPart(fieldname,fileNameStr , fileBody);

        innerRequestBody = formRequestBuilder.build();
      }

      RequestBody requestBody = new CountingRequestBody(innerRequestBody, getId().toString(), this);
      Request.Builder requestBuilder = new Request.Builder();

      Type headersMapType = new TypeToken<Map<String, String>>() {}.getType();;
      Map<String, String> headersDataMap = gson.fromJson(data,headersMapType);
      if (headersDataMap != null) {

        for (String key : headersDataMap.keySet()) {

          String header = headersDataMap.get(key);

          if (header != null && !header.isEmpty()) {
            requestBuilder = requestBuilder.addHeader(key, header);
          }
        }
      }

      if (!URLUtil.isValidUrl(uploadurl)) {
        return Result.failure(
            createOutputErrorData(
                UploadStatus.FAILED,
                DEFAULT_ERROR_STATUS_CODE,
                "invalid_url",
                "url is not a valid url",
                null));
      }

      requestBuilder.addHeader("Accept", "application/json; charset=utf-8");

      Request request;

      switch (method.toUpperCase()) {
        case "PUT":
          request = requestBuilder.url(uploadurl).put(requestBody).build();
          break;
        case "PATCH":
          request = requestBuilder.url(uploadurl).patch(requestBody).build();

          break;
        default:
          request = requestBuilder.url(uploadurl).post(requestBody).build();

          break;
      }

      buildNotification(getApplicationContext());

      Log.d(TAG, "Start uploading for " + tag);

      OkHttpClient client =
          new OkHttpClient.Builder()
              .connectTimeout((long) requestTimeoutInSeconds, TimeUnit.SECONDS)
              .writeTimeout((long) requestTimeoutInSeconds, TimeUnit.SECONDS)
              .readTimeout((long) requestTimeoutInSeconds, TimeUnit.SECONDS)
              .build();

      call = client.newCall(request);
      Response response = call.execute();
      String responseString = response.body().string();
      statusCode = response.code();
      Headers rheaders = response.headers();
      Map<String, String> outputHeaders = new HashMap<>();

      boolean hasJsonResponse = true;

      String responseContentType = rheaders.get("content-type");

      if (responseContentType != null && responseContentType.contains("json")) {
        hasJsonResponse = true;
      } else {
        hasJsonResponse = false;
      }

      for (String name : rheaders.names()) {
        outputHeaders.put(name, rheaders.get(name));
      }

      String responseHeaders = gson.toJson(outputHeaders);

      Log.d(TAG, "Response: " + responseString);
      Log.d(TAG, "Response header: " + responseHeaders);

      if (!response.isSuccessful()) {
        taskDao.updateTask(getId().toString(), UploadStatus.FAILED, 0);
        if (showNotification) {
          updateNotification(context, tag, UploadStatus.FAILED, 0, null);
        }
        return Result.failure(
            createOutputErrorData(
                UploadStatus.FAILED,
                statusCode,
                "upload_error",
                hasJsonResponse ? responseString : null,
                null));
      }

      Data.Builder builder =
          new Data.Builder()
              .putString(EXTRA_ID, getId().toString())
              .putInt(EXTRA_STATUS, UploadStatus.COMPLETE)
              .putInt(EXTRA_STATUS_CODE, statusCode)
              .putString(EXTRA_HEADERS, responseHeaders);

      if (hasJsonResponse) {
        builder.putString(EXTRA_RESPONSE, responseString);
      }

      Data outputData = builder.build();
      Type uploadResponseType = new TypeToken<UploadResponse>() {}.getType();
      UploadResponse uploadResponsegsonGson = gson.fromJson(responseString,uploadResponseType);
      taskDao.updateTask(getId().toString(), UploadStatus.COMPLETE, 0,responseString);
      if (showNotification) {
        updateNotification(context, tag, UploadStatus.COMPLETE, 0, null);
      }

      return Result.success(outputData);

    } catch (JsonIOException ex) {
      taskDao.updateTask(getId().toString(), UploadStatus.FAILED, 0);
      return handleException(context, ex, "json_error");
    } catch (UnknownHostException ex) {
      taskDao.updateTask(getId().toString(), UploadStatus.FAILED, 0);
      return handleException(context, ex, "unknown_host");
    } catch (IOException ex) {
      taskDao.updateTask(getId().toString(), UploadStatus.FAILED, 0);
      return handleException(context, ex, "io_error");
    } catch (Exception ex) {
      taskDao.updateTask(getId().toString(), UploadStatus.FAILED, 0);
      return handleException(context, ex, "upload error");
    } finally {
      call = null;
    }
  }

  private Result handleException(Context context, Exception ex, String code) {

    ex.printStackTrace();

    int finalStatus = isCancelled ? UploadStatus.CANCELED : UploadStatus.FAILED;
    String finalCode = isCancelled ? "upload_cancelled" : code;

    if (showNotification) {
      updateNotification(context, tag, finalStatus, 0, null);
    }

    return Result.failure(
        createOutputErrorData(
            finalStatus,
            500,
            finalCode,
            ex.toString(),
            getStacktraceAsStringList(ex.getStackTrace())));
  }

  private String GetMimeType(String url) {
    String type = "*/*";
    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    try {
      if (extension != null && !extension.isEmpty()) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
      }
    } catch (Exception ex) {
      Log.d(TAG, "UploadWorker - GetMimeType", ex);
    }

    return type;
  }

  private MultipartBody.Builder prepareRequest(Map<String, String> parameters, String boundary) {

    MultipartBody.Builder requestBodyBuilder =
        boundary != null && !boundary.isEmpty()
            ? new MultipartBody.Builder(boundary)
            : new MultipartBody.Builder();

    requestBodyBuilder.setType(MultipartBody.FORM);

    if (parameters == null) return requestBodyBuilder;

    for (String key : parameters.keySet()) {
      String parameter = parameters.get(key);
      if (parameter != null) {
        requestBodyBuilder.addFormDataPart(key, parameter);
      }
    }

    return requestBodyBuilder;
  }

  private void sendUpdateProcessEvent(Context context, int status, int progress) {
    UploadProgressReporter.getInstance()
        .notifyProgress(new UploadProgress(getId().toString(), status, progress));
  }

  private Data createOutputErrorData(
      int status, int statusCode, String code, String message, String[] details) {
    return new Data.Builder()
        .putInt(UploadWorker.EXTRA_STATUS_CODE, statusCode)
        .putInt(UploadWorker.EXTRA_STATUS, status)
        .putString(UploadWorker.EXTRA_ERROR_CODE, code)
        .putString(UploadWorker.EXTRA_ERROR_MESSAGE, message)
        .putStringArray(UploadWorker.EXTRA_ERROR_DETAILS, details)
        .build();
  }

  @Override
  public void OnProgress(String taskId, long bytesWritten, long contentLength) {
    double p = ((double) bytesWritten / (double) contentLength) * 100;
    int progress = (int) Math.round(p);
    boolean running = isRunning(progress, lastProgress, UPDATE_STEP);
    Log.d(
        TAG,
        "taskId: "
            + getId().toString()
            + ", bytesWritten: "
            + bytesWritten
            + ", contentLength: "
            + contentLength
            + ", progress: "
            + progress
            + ", lastProgress: "
            + lastProgress);
    if (running) {

      Context context = getApplicationContext();
      sendUpdateProcessEvent(context, UploadStatus.RUNNING, progress);
      boolean shouldSendNotification = isRunning(progress, lastNotificationProgress, 10);
      if (showNotification && shouldSendNotification) {
        updateNotification(context, tag, UploadStatus.RUNNING, progress, null);
        lastNotificationProgress = progress;
      }

      lastProgress = progress;
    }
  }

  @Override
  public void onStopped() {
    super.onStopped();
    Log.d(TAG, "UploadWorker - Stopped");
    try {
      isCancelled = true;
      if (call != null && !call.isCanceled()) {
        call.cancel();
      }
    } catch (Exception ex) {
      Log.d(TAG, "Upload Request cancelled", ex);
    }
  }

  @Override
  public void OnError(String taskId, String code, String message) {
    Log.d(
        TAG,
        "Failed to upload - taskId: "
            + getId().toString()
            + ", code: "
            + code
            + ", error: "
            + message);
    sendUpdateProcessEvent(getApplicationContext(), UploadStatus.FAILED, -1);
  }

  private void buildNotification(Context context) {
    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Create the NotificationChannel, but only on API 26+ because
      // the NotificationChannel class is new and not in the support library

      CharSequence name = context.getApplicationInfo().loadLabel(context.getPackageManager());
      int importance = NotificationManager.IMPORTANCE_DEFAULT;
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setSound(null, null);

      // Add the channel
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

      if (notificationManager != null) {
        notificationManager.createNotificationChannel(channel);
      }
    }

    // Create the notification
    builder =
        new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_upload)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
  }

  private void updateNotification(
      Context context, String title, int status, int progress, PendingIntent intent) {
    builder.setContentTitle(title);
    builder.setContentIntent(intent);

    boolean shouldUpdate = false;

    if (status == UploadStatus.RUNNING) {
      shouldUpdate = true;
      builder.setOngoing(true);
      builder
          .setContentText(progress == 0 ? msgStarted : msgInProgress)
          .setProgress(100, progress, progress == 0);
    } else if (status == UploadStatus.CANCELED) {
      shouldUpdate = true;
      builder.setOngoing(false);
      builder.setContentText(msgCanceled).setProgress(0, 0, false);
    } else if (status == UploadStatus.FAILED) {
      shouldUpdate = true;
      builder.setOngoing(false);
      builder.setContentText(msgFailed).setProgress(0, 0, false);
    } else if (status == UploadStatus.COMPLETE) {
      shouldUpdate = true;
      builder.setOngoing(false);
      builder.setContentText(msgComplete).setProgress(0, 0, false);
    }

    // Show the notification
    if (showNotification && shouldUpdate) {
      NotificationManagerCompat.from(context)
          .notify(getId().toString(), primaryId, builder.build());
    }
  }

  private boolean isRunning(int currentProgress, int previousProgress, int step) {
    int prev = previousProgress + step;
    return (currentProgress == 0 || currentProgress > prev || currentProgress >= 100)
        && currentProgress != previousProgress;
  }

  private String[] getStacktraceAsStringList(StackTraceElement[] stacktrace) {
    List<String> output = new ArrayList<>();

    if (stacktrace == null || stacktrace.length == 0) {
      return null;
    }

    for (StackTraceElement stackTraceElement : stacktrace) {
      output.add(stackTraceElement.toString());
    }

    return output.toArray(new String[0]);
  }
}
