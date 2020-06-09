import 'dart:async';
import 'dart:io';
import 'dart:ui';
import 'package:myflutteruploader/src/upload_task_type.dart';
import 'package:path/path.dart' as pathTools;

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'callback_dispatcher.dart';
import 'upload_exception.dart';
import 'upload_method.dart';
import 'upload_task.dart';
import 'upload_task_progress.dart';
import 'compress_task_progress.dart';
import 'upload_task_response.dart';
import 'upload_task_status.dart';

typedef void UploadCallback(String id, UploadTaskStatus status, int progress);

class MyFlutterUploader {
  static const _channel = const MethodChannel('com.qianren.chat.io/uploader');
  static bool _initialized = false;
  static StreamController<UploadTaskProgress> uploadProgressController =
      StreamController<UploadTaskProgress>.broadcast();
  static StreamController<CompressTaskProgress> compressProgressController =
  StreamController<CompressTaskProgress>.broadcast();
  static StreamController<UploadTaskResponse> responseController =
      StreamController<UploadTaskResponse>.broadcast();

  static Future<Null> initialize() async {
    assert(!_initialized,
        'FlutterUploader.initialize() must be called only once!');

    WidgetsFlutterBinding.ensureInitialized();

    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    await _channel
        .invokeMethod('initialize', <dynamic>[callback.toRawHandle()]);
    _initialized = true;

    _channel.setMethodCallHandler(_handleMethod);
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
  static Future<String> enqueue({
    @required String uploadurl,
    @required String localePath,
    @required UploadTaskType fileType,
    String fieldname = "uploadfile",
    UploadMethod method = UploadMethod.POST,
    Map<String, String> headers,
    Map<String, String> data,
    int requestTimeoutInSeconds = 3600,
    bool showNotification = true,
  }) async {
    assert(method != null);
    //assert(Directory(pathTools.basename(localePath)).existsSync());

    StringBuffer headerBuilder = StringBuffer();
    if (headers != null) {
      headerBuilder.write('{');
      headerBuilder.writeAll(
          headers.entries
              .map((entry) => '\"${entry.key}\": \"${entry.value}\"'),
          ',');
      headerBuilder.write('}');
    }

    StringBuffer dataBuilder = StringBuffer();
    if (data != null) {
      dataBuilder.write('{');
      dataBuilder.writeAll(
          data.entries.map((entry) => '\"${entry.key}\": \"${entry.value}\"'),
          ',');
      dataBuilder.write('}');
    }

    try {
      String taskId = await _channel.invokeMethod<String>('enqueue', {
        'uploadurl': uploadurl,
        'localePath': localePath,
        'fileType': fileType.value,
        'fieldname': fieldname,
        'method': describeEnum(method),
        'headers': headerBuilder.toString(),
        'data': dataBuilder.toString(),
        'requestTimeoutInSeconds': requestTimeoutInSeconds,
        'showNotification': showNotification,
      });
      print('Upload task is enqueued with id($taskId)');
      return taskId;
    } on PlatformException catch (e, stackTrace) {
      print('Upload task is failed with reason(${e.message})');
      return null;
    }
  }

  static Future<String> enqueueCompressVideoThenUpload({
    @required String uploadurl,
    @required String localePath,
    String fieldname = "uploadfile",
    UploadMethod method = UploadMethod.POST,
    Map<String, String> headers,
    Map<String, String> data,
    int requestTimeoutInSeconds = 3600,
    bool showNotification = true,
  }) async {
    assert(method != null);
    //assert(Directory(pathTools.basename(localePath)).existsSync());

    StringBuffer headerBuilder = StringBuffer();
    if (headers != null) {
      headerBuilder.write('{');
      headerBuilder.writeAll(
          headers.entries
              .map((entry) => '\"${entry.key}\": \"${entry.value}\"'),
          ',');
      headerBuilder.write('}');
    }

    StringBuffer dataBuilder = StringBuffer();
    if (data != null) {
      dataBuilder.write('{');
      dataBuilder.writeAll(
          data.entries.map((entry) => '\"${entry.key}\": \"${entry.value}\"'),
          ',');
      dataBuilder.write('}');
    }

    try {
      String taskId = await _channel.invokeMethod<String>('enqueueCompressVideoThenUpload', {
        'uploadurl': uploadurl,
        'localePath': localePath,
        'fileType': UploadTaskType.compressVideo.value,
        'fieldname': fieldname,
        'method': describeEnum(method),
        'headers': headerBuilder.toString(),
        'data': dataBuilder.toString(),
        'requestTimeoutInSeconds': requestTimeoutInSeconds,
        'showNotification': showNotification,
      });
      print('Upload task is enqueued with id($taskId)');
      return taskId;
    } on PlatformException catch (e, stackTrace) {
      print('Upload task is failed with reason(${e.message})');
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
                upload_taskId: item["upload_taskId"],
                upload_status: UploadTaskStatus(item['upload_status']),
                upload_progress: item['upload_progress'],
                uploadurl: item['uploadurl'],
                downloadurl: item['downloadurl'],
                localePath: item['localePath'],
                fileType:UploadTaskType(item['fileType']),
                fieldname: item['fieldname'],
                method: item['method'],
                headers: item['headers'],
                data: item['data'],
                requestTimeoutInSeconds: item['requestTimeoutInSeconds'],
                showNotification: item['showNotification'],
                binaryUpload: item['binaryUpload'],
                resumable: item['resumable'],
                upload_timeCreated: item['upload_timeCreated'],
                compress_taskId: item['compress_taskId'],
                compress_status: UploadTaskStatus(item['compress_status']),
                compress_progress: item['compress_progress'],
                compress_path: item['compress_path'],
                compressTimeCreated: item['compressTimeCreated'],
              ))
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
                upload_taskId: item["upload_taskId"],
                upload_status: UploadTaskStatus(item['upload_status']),
                upload_progress: item['upload_progress'],
                uploadurl: item['uploadurl'],
                downloadurl: item['downloadurl'],
                localePath: item['localePath'],
                fileType:UploadTaskType(item['fileType']),
                fieldname: item['fieldname'],
                method: item['method'],
                headers: item['headers'],
                data: item['data'],
                requestTimeoutInSeconds: item['requestTimeoutInSeconds'],
                showNotification: item['showNotification'],
                binaryUpload: item['binaryUpload'],
                resumable: item['resumable'],
                upload_timeCreated: item['upload_timeCreated'],
                compress_taskId: item['compress_taskId'],
                compress_status: UploadTaskStatus(item['compress_status']),
                compress_progress: item['compress_progress'],
                compress_path: item['compress_path'],
                compressTimeCreated: item['compressTimeCreated'],
            ))
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
  /// Delete a upload task from DB. If the given task is completed and send WebSock is Ok.
  ///
  /// **parameters:**
  ///
  /// * `taskId`: unique identifier of a download task
  ///
  static Future<Null> removeCompleted(
      {@required String taskId}) async {
    assert(_initialized, 'FlutterUploader.initialize() must be called first');

    try {
      return await _channel.invokeMethod('removeCompleted',
          {'task_id': taskId});
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

  static Future<Null> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "updateProgress":
        String id = call.arguments['task_id'];
        int status = call.arguments['status'];
        int uploadProgress = call.arguments['progress'];
        String tag = call.arguments["tag"];

        uploadProgressController?.sink?.add(UploadTaskProgress(
            id, uploadProgress, UploadTaskStatus.from(status), tag));

        break;
      case "compressProgress":
        String id = call.arguments['task_id'];
        int status = call.arguments['status'];
        int uploadProgress = call.arguments['progress'];

        compressProgressController?.sink?.add(CompressTaskProgress(
            id, uploadProgress, UploadTaskStatus.from(status)));
        break;
      case "uploadFailed":
        String id = call.arguments['task_id'];
        String message = call.arguments['message'];
        String code = call.arguments['code'];
        int status = call.arguments["status"];
        int statusCode = call.arguments["statusCode"];
        String tag = call.arguments["tag"];

        dynamic details = call.arguments['details'];
        StackTrace stackTrace;

        if (details != null && details.length > 0) {
          stackTrace =
              StackTrace.fromString(details.reduce((s, r) => "$r\n$s"));
        }

        responseController?.sink?.addError(
          UploadException(
            code: code,
            message: message,
            taskId: id,
            statusCode: statusCode,
            status: UploadTaskStatus.from(status),
            tag: tag,
          ),
          stackTrace,
        );
        break;
      case "uploadCompleted":
        String id = call.arguments['task_id'];
        Map headers = call.arguments["headers"];
        String message = call.arguments["message"];
        int status = call.arguments["status"];
        int statusCode = call.arguments["statusCode"];
        String tag = call.arguments["tag"];
        Map<String, String> h = headers?.map(
            (key, value) => MapEntry<String, String>(key, value as String));

        responseController?.sink?.add(UploadTaskResponse(
          taskId: id,
          status: UploadTaskStatus.from(status),
          statusCode: statusCode,
          headers: h,
          response: message,
          tag: tag,
        ));
        break;
      default:
        throw UnsupportedError("Unrecognized JSON message");
    }
  }
}
