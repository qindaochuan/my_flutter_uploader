package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

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
    public void onActivityStarted(Activity activity) {}

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
  }

  @Override
  public void onDetachedFromActivity() {
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
    //application.unregisterActivityLifecycleCallbacks(observer);
    application = null;
  }

  private final MyflutteruploaderDelegate constructDelegate(final Activity setupActivity, Context context,MethodChannel flutterChannel) {
    return new MyflutteruploaderDelegate(setupActivity,context,flutterChannel);
  }
}
