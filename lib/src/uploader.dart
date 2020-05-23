import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'callback_dispatcher.dart';
import 'file_item.dart';
import 'upload_method.dart';
import 'upload_task.dart';
import 'upload_task_status.dart';

typedef void UploadCallback(String id, UploadTaskStatus status, int progress);

class MyFlutterUploader{
  static const _channel = const MethodChannel('com.qianren.chat.io/uploader');
  static bool _initialized = false;

  static Future<Null> initialize() async {
    assert(!_initialized,
    'FlutterUploader.initialize() must be called only once!');

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
      String taskId = await _channel.invokeMethod<String>('enqueue', {
        'url': url,
        'method': describeEnum(method),
        'files': f,
        'headers': headers,
        'data': data,
        'show_notification': showNotification,
        'tag': tag
      });
      print('Download task is enqueued with id($taskId)');
      return taskId;
    } on PlatformException catch (e, stackTrace) {
      print('Download task is failed with reason(${e.message})');
      return null;
    }
  }

  ///
  /// Load all tasks from Sqlite database
  ///
  /// **return:**
  ///
  /// A list of [UploadTask] objects
  ///
  static Future<List<UploadTask>> loadTasks() async {
    assert(_initialized, 'FlutterDownloader.initialize() must be called first');

    try {
      List<dynamic> result = await _channel.invokeMethod('loadTasks');
      return result
          .map((item) => new UploadTask(
          taskId: item['task_id'],
          status: UploadTaskStatus(item['status']),
          progress: item['progress'],
          url: item['url'],
          filename: item['file_name'],
          savedDir: item['saved_dir']))
          .toList();
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Load tasks from Sqlite database with SQL statements
  ///
  /// **parameters:**
  ///
  /// * `query`: SQL statement. Note that the plugin will parse loaded data from
  /// database into [UploadTask] object, in order to make it work, you should
  /// load tasks with all fields from database. In other words, using `SELECT *`
  /// statement.
  ///
  /// **return:**
  ///
  /// A list of [UploadTask] objects
  ///
  /// **example:**
  ///
  /// ```dart
  /// FlutterUploader.loadTasksWithRawQuery(query: 'SELECT * FROM task WHERE status=3');
  /// ```
  ///
  static Future<List<UploadTask>> loadTasksWithRawQuery(
      {@required String query}) async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      List<dynamic> result = await _channel
          .invokeMethod('loadTasksWithRawQuery', {'query': query});
      print('Loaded tasks: $result');
      return result
          .map((item) => new UploadTask(
          taskId: item['task_id'],
          status: UploadTaskStatus(item['status']),
          progress: item['progress'],
          url: item['url'],
          filename: item['file_name'],
          savedDir: item['saved_dir']))
          .toList();
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Cancel a given upload task
  ///
  /// **parameters:**
  ///
  /// * `taskId`: unique identifier of the upload task
  ///
  static Future<Null> cancel({@required String taskId}) async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      return await _channel.invokeMethod('cancel', {'task_id': taskId});
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Cancel all enqueued and running upload tasks
  ///
  static Future<Null> cancelAll() async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      return await _channel.invokeMethod('cancelAll');
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Pause a running upload task
  ///
  /// **parameters:**
  ///
  /// * `taskId`: unique identifier of a running upload task
  ///
  static Future<Null> pause({@required String taskId}) async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      return await _channel.invokeMethod('pause', {'task_id': taskId});
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Resume a paused upload task
  ///
  /// **parameters:**
  ///
  /// * `taskId`: unique identifier of a paused upload task
  ///
  /// **return:**
  ///
  /// An unique identifier of a new upload task that is created to continue
  /// the partial upload progress
  ///
  static Future<String> resume({
    @required String taskId,
    bool requiresStorageNotLow = true,
  }) async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      return await _channel.invokeMethod('resume', {
        'task_id': taskId,
        'requires_storage_not_low': requiresStorageNotLow,
      });
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Retry a failed upload task
  ///
  /// **parameters:**
  ///
  /// * `taskId`: unique identifier of a failed upload task
  ///
  /// **return:**
  ///
  /// An unique identifier of a new upload task that is created to start the
  /// failed upload progress from the beginning
  ///
  static Future<String> retry({
    @required String taskId,
    bool requiresStorageNotLow = true,
  }) async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      return await _channel.invokeMethod('retry', {
        'task_id': taskId,
        'requires_storage_not_low': requiresStorageNotLow,
      });
    } on PlatformException catch (e) {
      print(e.message);
      return null;
    }
  }

  ///
  /// Register a callback to track status and progress of upload task
  ///
  /// **parameters:**
  ///
  /// * `callback`: a top-level or static function of [UploadCallback] type
  /// which is called whenever the status or progress value of a upload task
  /// has been changed.
  ///
  /// **Note:**
  ///
  /// Your UI is rendered in the main isolate, while upload events come from a
  /// background isolate (in other words, codes in `callback` are run in the
  /// background isolate), so you have to handle the communication between two
  /// isolates.
  ///
  /// **Example:**
  ///
  /// {@tool sample}
  ///
  /// ```dart
  ///
  /// ReceivePort _port = ReceivePort();
  ///
  /// @override
  /// void initState() {
  ///   super.initState();
  ///
  ///   IsolateNameServer.registerPortWithName(_port.sendPort, 'uploader_send_port');
  ///   _port.listen((dynamic data) {
  ///      String id = data[0];
  ///      UploadTaskStatus status = data[1];
  ///      int progress = data[2];
  ///      setState((){ });
  ///   });
  ///
  ///   FlutterUploader.registerCallback(uploadCallback);
  ///
  /// }
  ///
  /// static void uploadCallback(String id, UploadTaskStatus status, int progress) {
  ///   final SendPort send = IsolateNameServer.lookupPortByName('uploader_send_port');
  ///   send.send([id, status, progress]);
  /// }
  ///
  /// ```
  ///
  /// {@end-tool}
  ///
  static registerCallback(UploadCallback callback) {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    final callbackHandle = PluginUtilities.getCallbackHandle(callback);
    assert(callbackHandle != null,
    'callback must be a top-level or a static function');
    _channel.invokeMethod(
        'registerCallback', <dynamic>[callbackHandle.toRawHandle()]);
  }
}