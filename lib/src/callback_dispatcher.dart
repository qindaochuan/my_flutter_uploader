import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'upload_task_status.dart';

void uploadCallbackDispatcher() {
  const MethodChannel uploadBackgroundChannel =
      MethodChannel('com.qianren.chat.io/uploader_upload_background');

  WidgetsFlutterBinding.ensureInitialized();

  uploadBackgroundChannel.setMethodCallHandler((MethodCall call) async {
    final List<dynamic> args = call.arguments;

    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));

    final String id = args[1];
    final int status = args[2];
    final int progress = args[3];
    final String response = args[4];

    callback(id, UploadTaskStatus(status), progress, response);
  });

  uploadBackgroundChannel.invokeMethod('didInitializeDispatcher');
}

void compressCallbackDispatcher() {
  const MethodChannel compressBackgroundChannel =
  MethodChannel('com.qianren.chat.io/uploader_compress_background');

  WidgetsFlutterBinding.ensureInitialized();

  compressBackgroundChannel.setMethodCallHandler((MethodCall call) async {
    final List<dynamic> args = call.arguments;

    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));

    final String compress_taskId = args[1];
    final int compress_status = args[2];
    final int compress_progress = args[3];
    final String upload_taskId = args[4];

    callback(compress_taskId, UploadTaskStatus(compress_status), compress_progress, upload_taskId);
  });

  compressBackgroundChannel.invokeMethod('didInitializeDispatcher');
}
