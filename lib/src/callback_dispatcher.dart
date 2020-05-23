import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'upload_task_status.dart';

void callbackDispatcher() {
  const MethodChannel backgroundChannel =
      MethodChannel('com.qianren.chat.io/uploader_background');

  WidgetsFlutterBinding.ensureInitialized();

  backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final List<dynamic> args = call.arguments;

    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));

    final String id = args[1];
    final int status = args[2];
    final int progress = args[3];

    callback(id, UploadTaskStatus(status), progress);
  });

  backgroundChannel.invokeMethod('didInitializeDispatcher');
}
