import 'dart:convert';

import 'package:myflutteruploader/myflutteruploader.dart';

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
  final String upload_taskId;
  final UploadTaskStatus upload_status;
  final int upload_progress;
  final String uploadurl;
  final String downloadurl;
  final String localePath;
  final UploadTaskType fileType;
  final String fieldname;
  final String method;
  final String headers;
  final String data;
  final int requestTimeoutInSeconds;
  final bool showNotification;
  final bool binaryUpload;
  final bool resumable;
  final int upload_timeCreated;
  final String compress_taskId;
  final UploadTaskStatus compress_status;
  final int compress_progress;
  final String compress_path;
  final int compressTimeCreated;

  UploadTask({
    this.upload_taskId,
    this.upload_status,
    this.upload_progress,
    this.uploadurl,
    this.downloadurl,
    this.localePath,
    this.fileType,
    this.fieldname,
    this.method,
    this.headers,
    this.data,
    this.requestTimeoutInSeconds,
    this.showNotification,
    this.binaryUpload,
    this.resumable,
    this.upload_timeCreated,
    this.compress_taskId,
    this.compress_status,
    this.compress_progress,
    this.compress_path,
    this.compressTimeCreated
  });
}
