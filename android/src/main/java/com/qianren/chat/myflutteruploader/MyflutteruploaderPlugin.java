package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** MyflutteruploaderPlugin */
public class MyflutteruploaderPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private static final String TAG = "flutter_upload_task";
  private class LifeCycleObserver
          implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private final Activity thisActivity;

    LifeCycleObserver(Activity activity) {
      this.thisActivity = activity;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {}

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {}

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {}

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {}

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
      onActivityStopped(thisActivity);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
      onActivityDestroyed(thisActivity);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
      if (thisActivity == activity && activity.getApplicationContext() != null) {
        ((Application) activity.getApplicationContext())
                .unregisterActivityLifecycleCallbacks(
                        this); // Use getApplicationContext() to avoid casting failures
      }
    }

    @Override
    public void onActivityStopped(Activity activity) {
      if (thisActivity == activity) {
        delegate.saveStateBeforeResult();
      }
    }
  }

  static class UploadProgressObserver implements Observer<UploadProgress> {

    private final WeakReference<MyflutteruploaderPlugin> plugin;

    UploadProgressObserver(MyflutteruploaderPlugin plugin) {
      this.plugin = new WeakReference<>(plugin);
    }

    @Override
    public void onChanged(UploadProgress uploadProgress) {
      MyflutteruploaderPlugin plugin = this.plugin.get();

      if (plugin == null) {
        return;
      }

      String id = uploadProgress.getTaskId();
      int progress = uploadProgress.getProgress();
      int status = uploadProgress.getStatus();
      plugin.delegate.sendUpdateProgress(id, status, progress);
    }
  }

  @Nullable
  private UploadProgressObserver uploadProgressObserver;

  private Map<String, Boolean> completedTasks = new HashMap<>();
  private Gson gson = new Gson();

  static class UploadCompletedObserver implements Observer<List<WorkInfo>> {
    private final WeakReference<MyflutteruploaderPlugin> plugin;

    UploadCompletedObserver(MyflutteruploaderPlugin plugin) {
      this.plugin = new WeakReference<>(plugin);
    }

    @Override
    public void onChanged(List<WorkInfo> workInfoList) {
      MyflutteruploaderPlugin plugin = this.plugin.get();

      if (plugin == null) {
        return;
      }

      for (WorkInfo info : workInfoList) {
        String id = info.getId().toString();
        if (!plugin.completedTasks.containsKey(id)) {
          if (info.getState().isFinished()) {
            plugin.completedTasks.put(id, true);
            Data outputData = info.getOutputData();

            switch (info.getState()) {
              case FAILED:
                int failedStatus =
                        outputData.getInt(UploadWorker.EXTRA_STATUS, UploadStatus.FAILED);
                int statusCode = outputData.getInt(UploadWorker.EXTRA_STATUS_CODE, 500);
                String code = outputData.getString(UploadWorker.EXTRA_ERROR_CODE);
                String errorMessage = outputData.getString(UploadWorker.EXTRA_ERROR_MESSAGE);
                String[] details = outputData.getStringArray(UploadWorker.EXTRA_ERROR_DETAILS);
                plugin.delegate.sendFailed(id, failedStatus, statusCode, code, errorMessage, details);
                break;
              case CANCELLED:
                plugin.delegate.sendFailed(
                        id,
                        UploadStatus.CANCELED,
                        500,
                        "flutter_upload_cancelled",
                        "upload has been cancelled",
                        null);
                break;
              case SUCCEEDED:
                int status = outputData.getInt(UploadWorker.EXTRA_STATUS, UploadStatus.COMPLETE);
                Map<String, String> headers = null;
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                String headerJson = info.getOutputData().getString(UploadWorker.EXTRA_HEADERS);
                if (headerJson != null) {
                  headers = plugin.gson.fromJson(headerJson, type);
                }

                String response = info.getOutputData().getString(UploadWorker.EXTRA_RESPONSE);
                plugin.delegate.sendCompleted(id, status, response, headers);
                break;
            }
          }
        }
      }
    }
  }

  @Nullable private UploadCompletedObserver uploadCompletedObserver;

  private static final String CHANNEL = "com.qianren.chat.io/uploader";

  private MethodChannel channel;
  private MyflutteruploaderDelegate delegate;
  private FlutterPluginBinding pluginBinding;
  private ActivityPluginBinding activityBinding;
  private Application application;
  private Activity activity;
  // This is null when not using v2 embedding;
  private Lifecycle lifecycle;
  private LifeCycleObserver observer;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    pluginBinding = flutterPluginBinding;
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null) {
      // If a background flutter view tries to register the plugin, there will be no activity from the registrar,
      // we stop the registering process immediately because the ImagePicker requires an activity.
      return;
    }
    Activity activity = registrar.activity();
    Application application = null;
    if (registrar.context() != null) {
      application = (Application) (registrar.context().getApplicationContext());
    }
    MyflutteruploaderPlugin plugin = new MyflutteruploaderPlugin();
    plugin.setup(registrar.messenger(), application, activity, registrar, null);
  }

  // MethodChannel.Result wrapper that responds on the platform thread.
  private static class MethodResultWrapper implements MethodChannel.Result {
    private MethodChannel.Result methodResult;
    private Handler handler;

    MethodResultWrapper(MethodChannel.Result result) {
      methodResult = result;
      handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void success(final Object result) {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.success(result);
                }
              });
    }

    @Override
    public void error(
            final String errorCode, final String errorMessage, final Object errorDetails) {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.error(errorCode, errorMessage, errorDetails);
                }
              });
    }

    @Override
    public void notImplemented() {
      handler.post(
              new Runnable() {
                @Override
                public void run() {
                  methodResult.notImplemented();
                }
              });
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result rawResult) {
    if (activity == null) {
      rawResult.error("no_activity", "flutter_uploader plugin requires a foreground activity.", null);
      return;
    }

    MethodChannel.Result result = new MethodResultWrapper(rawResult);
    
    switch (call.method) {
      case "initialize":
        delegate.initialize(call, result);
        break;
      case "registerCallback":
        delegate.registerCallback(call,result);
        break;
      case "enqueue":
        delegate.enqueue(call, result);
        break;
      case "enqueueCompressVideoThenUpload":
        delegate.enqueueCompressVideoThenUpload(call,result);
        break;
      case "loadTasks":
        delegate.loadTasks(call,result);
        break;
      case "loadTasksWithRawQuery":
        delegate.loadTasksWithRawQuery(call, result);
        break;
      case "cancel":
        delegate.cancel(call, result);
        break;
      case "cancelAll":
        delegate.cancelAll(call, result);
        break;
      case "pause":
        delegate.pause(call, result);
        break;
      case "resume":
        delegate.resume(call, result);
        break;
      case "retry":
        delegate.retry(call, result);
        break;
      case "removeCompleted":
        delegate.removeCompleted(call,result);
        break;
      default:
        throw new IllegalArgumentException("Unknown method " + call.method);
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    pluginBinding = null;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    activityBinding = binding;
    setup(
            pluginBinding.getBinaryMessenger(),
            (Application) pluginBinding.getApplicationContext(),
            activityBinding.getActivity(),
            null,
            activityBinding);
    if (activity == MyflutteruploaderPlugin.this.activity) {
      uploadProgressObserver = new UploadProgressObserver(MyflutteruploaderPlugin.this);
      UploadProgressReporter.getInstance().observeForever(uploadProgressObserver);

      uploadCompletedObserver = new UploadCompletedObserver(MyflutteruploaderPlugin.this);
      WorkManager.getInstance(MyflutteruploaderPlugin.this.application)
              .getWorkInfosByTagLiveData(TAG)
              .observeForever(uploadCompletedObserver);
    }
  }

  @Override
  public void onDetachedFromActivity() {
    if (activity == MyflutteruploaderPlugin.this.activity) {
      if (uploadProgressObserver != null) {
        UploadProgressReporter.getInstance().removeObserver(uploadProgressObserver);
        uploadProgressObserver = null;
      }

      if (uploadCompletedObserver != null) {
        WorkManager.getInstance(MyflutteruploaderPlugin.this.activity)
                .getWorkInfosByTagLiveData(TAG)
                .removeObserver(uploadCompletedObserver);
        uploadCompletedObserver = null;
      }
    }
    tearDown();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  private void setup(
          final BinaryMessenger messenger,
          final Application application,
          final Activity activity,
          final PluginRegistry.Registrar registrar,
          final ActivityPluginBinding activityBinding) {
    this.activity = activity;
    this.application = application;
    channel = new MethodChannel(messenger, CHANNEL);
    this.delegate = constructDelegate(activity,application,channel);
    channel.setMethodCallHandler(this);
    observer = new LifeCycleObserver(activity);
    if (registrar != null) {
      // V1 embedding setup for activity listeners.
      application.registerActivityLifecycleCallbacks(observer);
      registrar.addActivityResultListener(delegate);
      registrar.addRequestPermissionsResultListener(delegate);
    } else {
      // V2 embedding setup for activity listeners.
      activityBinding.addActivityResultListener(delegate);
      activityBinding.addRequestPermissionsResultListener(delegate);
      //lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(activityBinding);
      //lifecycle.addObserver(observer);
    }
  }

  private void tearDown() {
    activityBinding.removeActivityResultListener(delegate);
    activityBinding.removeRequestPermissionsResultListener(delegate);
    activityBinding = null;
    //lifecycle.removeObserver(observer);
    lifecycle = null;
    delegate = null;
    channel.setMethodCallHandler(null);
    channel = null;
    application.unregisterActivityLifecycleCallbacks(observer);
    application = null;
  }

  private final MyflutteruploaderDelegate constructDelegate(final Activity setupActivity, Context context,MethodChannel flutterChannel) {
    return new MyflutteruploaderDelegate(setupActivity,context,flutterChannel);
  }
}
