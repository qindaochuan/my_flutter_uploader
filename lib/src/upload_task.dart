import 'dart:convert';

import 'upload_task_type.dart';

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
  String upload_taskId;
  UploadTaskStatus upload_status = UploadTaskStatus.undefined;
  int upload_progress = 0;
  String uploadurl;
  String downloadurl;
  String localePath;
  UploadTaskType fileType;
  String fieldname;
  String method;
  String headers;
  String data;
  int requestTimeoutInSeconds;
  bool showNotification;
  bool binaryUpload;
  bool resumable;
  int upload_timeCreated;
  String compress_taskId;
  UploadTaskStatus compress_status = UploadTaskStatus.undefined;
  int compress_progress = 0;
  String compress_path;
  int compressTimeCreated;

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

  bool isCompleted() =>
      this.upload_status == UploadTaskStatus.canceled ||
          this.upload_status == UploadTaskStatus.complete ||
          this.upload_status == UploadTaskStatus.failed;
}
