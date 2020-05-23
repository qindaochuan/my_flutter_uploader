import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'callback_dispatcher.dart';
import 'file_item.dart';
import 'upload_method.dart';
import 'upload_task_status.dart';

typedef void UploadCallback(String id, UploadTaskStatus status, int progress);

class MyFlutterUploader{
  static const _channel = const MethodChannel('com.qianren.chat.io/uploader');
  static bool _initialized = false;

  static Future<Null> initialize() async {
    assert(!_initialized,
    'FlutterDownloader.initialize() must be called only once!');

    WidgetsFlutterBinding.ensureInitialized();

    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    await _channel
        .invokeMethod('initialize', <dynamic>[callback.toRawHandle()]);
    _initialized = true;
    return null;
  }

  /// Create a new multipart/form-data upload task
  ///
  /// **parameters:**
  ///
  /// * `url`: upload link
  /// * `files`: files to be uploaded
  /// * `method`: HTTP method to use for upload (POST,PUT,PATCH)
  /// * `headers`: HTTP headers
  /// * `data`: additional data to be uploaded together with file
  /// * `showNotification`: sets `true` to show a notification displaying
  /// upload progress and success or failure of upload task (Android only), otherwise will disable
  /// this feature. The default value is `false`
  /// * `tag`: name of the upload request (only used on Android)
  /// **return:**
  ///
  /// an unique identifier of the new upload task
  ///
  Future<String> enqueue({
    @required String url,
    @required List<FileItem> files,
    UploadMethod method = UploadMethod.POST,
    Map<String, String> headers,
    Map<String, String> data,
    bool showNotification = false,
    String tag,
  }) async {
    assert(method != null);

    List f = files != null && files.length > 0
        ? files.map((f) => f.toJson()).toList()
        : [];

    try {
      return await _channel.invokeMethod<String>('enqueue', {
        'url': url,
        'method': describeEnum(method),
        'files': f,
        'headers': headers,
        'data': data,
        'show_notification': showNotification,
        'tag': tag
      });
    } on PlatformException catch (e, stackTrace) {

      return null;
    }
  }


}