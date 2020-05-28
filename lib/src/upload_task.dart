import 'dart:convert';

import 'package:myflutteruploader/myflutteruploader.dart';

import 'file_item.dart';
import 'upload_task_status.dart';

///
/// A model class encapsulates all task information according to data in Sqlite
/// database.
///
/// * [taskId] the unique identifier of a upload task
/// * [status] the latest status of a upload task
/// * [progress] the latest progress value of a upload task
/// * [data] additional data to be sent together with file
///
class UploadTask {
  final String taskId;
  final status;
  final int progress;
  final String uploadurl;
  final String downloadurl;
  final String localePath;
  final String fieldname;
  final String method;
  final String headers;
  final String data;
  final int requestTimeoutInSeconds;
  final bool showNotification;
  final bool binaryUpload;
  final String mimeType;
  final bool resumable;
  final int timeCreated;

  UploadTask({
      this.taskId,
      this.status,
      this.progress,
      this.uploadurl,
      this.downloadurl,
      this.localePath,
      this.fieldname,
      this.method,
      this.headers,
      this.data,
      this.requestTimeoutInSeconds,
      this.showNotification,
      this.binaryUpload,
      this.mimeType,
      this.resumable,
      this.timeCreated});
}
