package com.qianren.chat.myflutteruploader;

import android.content.Context;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import vn.hunghd.flutterdownloader2.VideoCompress;

public class CompressVideoWorker extends Worker {
    private static final String TAG = MyflutteruploaderDelegate.TAG;
    public static String LOGTAG = CompressVideoWorker.class.getSimpleName();
    public static final String ARG_LOCALE_PATH = "localePath";

    private TaskDbHelper dbHelper;
    private TaskDao taskDao;
    public CompressVideoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
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

    @NonNull
    @Override
    public Result doWork() {
        final Context context = getApplicationContext();
        dbHelper = TaskDbHelper.getInstance(context);
        taskDao = new TaskDao(dbHelper);

        String localePath = getInputData().getString(ARG_LOCALE_PATH);

        final String destPath = MyflutteruploaderDelegate.videoCompressDir + "/" + new File(localePath).getName();

        final String compressTaskId = this.getId().toString();

        VideoCompress.VideoCompressTask task = VideoCompress.compressVideoMedium(localePath, destPath, new VideoCompress.CompressListener() {

            @Override
            public void onStart() {
                sendCompressVideoPrecessEvent(context, UploadStatus.RUNNING, 0);
                taskDao.updateCompressTask(compressTaskId, UploadStatus.RUNNING,0);
            }

            @Override
            public void onSuccess() {
                UploadTask task = taskDao.loadTaskByCompressTaskId(compressTaskId);
                WorkRequest request = buildRequest(task.getUploadurl(),destPath, task.getFieldname(), task.getMethod(), task.getHeaders(),
                        task.getData(), task.getRequestTimeoutInSeconds(), task.isShowNotification(), task.isBinaryUpload(), task.isResumable());
                WorkManager.getInstance(context).enqueue(request);
                String uploadTaskId = request.getId().toString();
                taskDao.startUploadTaskByCompoerssTask(compressTaskId,uploadTaskId,destPath);
                sendCompressVideoPrecessEvent(context, UploadStatus.COMPLETE, 100, uploadTaskId);
            }

            @Override
            public void onFail() {
                sendCompressVideoPrecessEvent(context, UploadStatus.FAILED,0);
                taskDao.updateCompressTask(compressTaskId, UploadStatus.FAILED,0);
            }

            @Override
            public void onProgress(float percent) {
                //Log.v(LOGTAG,new Float(percent).toString());
                sendCompressVideoPrecessEvent(context, UploadStatus.RUNNING,(int)percent);
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
        CompressVideoProgressReporter.getInstance()
                .notifyProgress(new CompressVideoProgress(getId().toString(), status, progress,uploadTaskId));
    }

    private void sendCompressVideoPrecessEvent(Context context, int status, int progress) {
        CompressVideoProgressReporter.getInstance()
                .notifyProgress(new CompressVideoProgress(getId().toString(), status, progress));
    }
}
