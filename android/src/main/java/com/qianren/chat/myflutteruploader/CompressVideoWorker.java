package com.qianren.chat.myflutteruploader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterRunArguments;
import vn.hunghd.flutterdownloader2.VideoCompress;

public class CompressVideoWorker extends Worker implements MethodChannel.MethodCallHandler{
    private static final String TAG = MyflutteruploaderDelegate.TAG;
    public static String LOGTAG = CompressVideoWorker.class.getSimpleName();
    public static final String ARG_LOCALE_PATH = "localePath";
    public static final String ARG_UPLOAD_CALLBACK_HANDLE = "upload_callback_handle";
    public static final String ARG_COMPRESS_CALLBACK_HANDLE = "compress_callback_handle";

    private TaskDbHelper dbHelper;
    private TaskDao taskDao;

    private static final AtomicBoolean isolateStarted = new AtomicBoolean(false);
    private static final ArrayDeque<List> isolateQueue = new ArrayDeque<>();
    private static FlutterNativeView backgroundFlutterView;
    private MethodChannel compressBackgroundChannel;

    public CompressVideoWorker(@NonNull final Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                startCompressBackgroundIsolate(context);
            }
        });
    }

    private void startCompressBackgroundIsolate(Context context){
        synchronized (isolateStarted) {
            if (backgroundFlutterView == null) {
                SharedPreferences pref = context.getSharedPreferences(MyflutteruploaderDelegate.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                long callbackHandle = pref.getLong(MyflutteruploaderDelegate.COMPRESS_CALLBACK_DISPATCHER_HANDLE_KEY, 0);

                FlutterMain.ensureInitializationComplete(context, null);

                FlutterCallbackInformation callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle);
                if (callbackInfo == null) {
                    Log.e(TAG, "Fatal: failed to find compressCallback");
                    return;
                }

                backgroundFlutterView = new FlutterNativeView(getApplicationContext(), true);

                /// backward compatibility with V1 embedding
                if (getApplicationContext() instanceof PluginRegistry.PluginRegistrantCallback) {
                    PluginRegistry.PluginRegistrantCallback pluginRegistrantCallback = (PluginRegistry.PluginRegistrantCallback) getApplicationContext();
                    PluginRegistry registry = backgroundFlutterView.getPluginRegistry();
                    pluginRegistrantCallback.registerWith(registry);
                }

                FlutterRunArguments args = new FlutterRunArguments();
                args.bundlePath = FlutterMain.findAppBundlePath(context);
                args.entrypoint = callbackInfo.callbackName;
                args.libraryPath = callbackInfo.callbackLibraryPath;

                backgroundFlutterView.runFromBundle(args);
            }
        }

        compressBackgroundChannel = new MethodChannel(backgroundFlutterView, "com.qianren.chat.io/uploader_compress_background");
        compressBackgroundChannel.setMethodCallHandler(this);
    }

    private WorkRequest buildRequest(String uploadurl,String localePath, String fieldname, String method, String headers,
                                     String data, int requestTimeoutInSeconds, boolean showNotification, boolean binaryUpload,
                                     boolean resumable, Long uploadCallbackHandl){
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
                        .putBoolean(UploadWorker.ARG_RESUMABLE,resumable)
                        .putLong(UploadWorker.ARG_UPLOAD_CALLBACK_HANDLE,uploadCallbackHandl);

        return new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setConstraints(
                        new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .addTag(TAG)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.SECONDS)
                .setInputData(dataBuilder.build())
                .build();
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if (call.method.equals("didInitializeDispatcher")) {
            synchronized (isolateStarted) {
                while (!isolateQueue.isEmpty()) {
                    compressBackgroundChannel.invokeMethod("", isolateQueue.remove());
                }
                isolateStarted.set(true);
                result.success(null);
            }
        } else {
            result.notImplemented();
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        final Context context = getApplicationContext();
        dbHelper = TaskDbHelper.getInstance(context);
        taskDao = new TaskDao(dbHelper);

        String localePath = getInputData().getString(ARG_LOCALE_PATH);
        final Long uploadCallbackHandle = getInputData().getLong(ARG_UPLOAD_CALLBACK_HANDLE,0);

        final String destPath = MyflutteruploaderDelegate.videoCompressDir + "/" + new File(localePath).getName();

        final String compressTaskId = this.getId().toString();

        VideoCompress.VideoCompressTask task = VideoCompress.compressVideoMedium(localePath, destPath, new VideoCompress.CompressListener() {

            @Override
            public void onStart() {
                sendCompressVideoPrecessEvent(context, UploadStatus.RUNNING, 0,null);
                taskDao.updateCompressTask(compressTaskId, UploadStatus.RUNNING,0);
            }

            @Override
            public void onSuccess() {
                UploadTask task = taskDao.loadTaskByCompressTaskId(compressTaskId);
                WorkRequest request = buildRequest(task.getUploadurl(),destPath, task.getFieldname(), task.getMethod(), task.getHeaders(),
                        task.getData(), task.getRequestTimeoutInSeconds(), task.isShowNotification(), task.isBinaryUpload(),
                        task.isResumable(), uploadCallbackHandle);
                WorkManager.getInstance(context).enqueue(request);
                String uploadTaskId = request.getId().toString();
                taskDao.startUploadTaskByCompoerssTask(compressTaskId,uploadTaskId,destPath);
                sendCompressVideoPrecessEvent(context, UploadStatus.COMPLETE, 100, uploadTaskId);
            }

            @Override
            public void onFail() {
                sendCompressVideoPrecessEvent(context, UploadStatus.FAILED,0,null);
                taskDao.updateCompressTask(compressTaskId, UploadStatus.FAILED,0);
            }

            @Override
            public void onProgress(float percent) {
                //Log.v(LOGTAG,new Float(percent).toString());
                sendCompressVideoPrecessEvent(context, UploadStatus.RUNNING,(int)percent,null);
                taskDao.updateCompressTask(compressTaskId, UploadStatus.RUNNING,(int)percent);
            }
        });

        try {
            if(task.get()){
                return Result.success();
            }else{
                return Result.failure();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Result.failure();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    private void sendCompressVideoPrecessEvent(Context context, int status, int progress,String uploadTaskId) {
//        CompressVideoProgressReporter.getInstance()
//                .notifyProgress(new CompressVideoProgress(getId().toString(), status, progress,uploadTaskId));
    final List<Object> args = new ArrayList<>();
    long callbackHandle = getInputData().getLong(ARG_COMPRESS_CALLBACK_HANDLE, 0);
    args.add(callbackHandle);
    args.add(getId().toString());
    args.add(status);
    args.add(progress);
    args.add(uploadTaskId);

    synchronized (isolateStarted) {
        if (!isolateStarted.get()) {
            isolateQueue.add(args);
        } else {
            new Handler(getApplicationContext().getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //System.out.println("sendCompressVideoPrecessEvent: " + args);
                    compressBackgroundChannel.invokeMethod("", args);
                }
            });
        }
    }
    }

//    private void sendCompressVideoPrecessEvent(Context context, int status, int progress) {
//        CompressVideoProgressReporter.getInstance()
//                .notifyProgress(new CompressVideoProgress(getId().toString(), status, progress));
//    }
}
