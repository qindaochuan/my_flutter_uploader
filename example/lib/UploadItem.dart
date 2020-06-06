import 'package:myflutteruploader/myflutteruploader.dart';

class UploadItem {
  String localPath;
  String uploadurl;
  String downloadurl;
  String upload_taskId;
  UploadTaskStatus upload_status;
  int upload_progress;
  UploadTaskType fileType;

  UploadItem({
    this.localPath,
    this.uploadurl,
    this.downloadurl,
    this.upload_taskId,
    this.upload_progress = 0,
    this.upload_status = UploadTaskStatus.undefined,
    this.fileType = UploadTaskType.undefined,
  });

  bool isCompleted() =>
      this.upload_status == UploadTaskStatus.canceled ||
          this.upload_status == UploadTaskStatus.complete ||
          this.upload_status == UploadTaskStatus.failed;
}