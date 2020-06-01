import 'package:myflutteruploader/myflutteruploader.dart';

class UploadItem {
  String localPath;
  String uploadurl;
  String downloadurl;
  String taskId;
  int progress;
  UploadTaskStatus status;
  UploadTaskType fileType;

  UploadItem({
    this.localPath,
    this.uploadurl,
    this.downloadurl,
    this.taskId,
    this.progress = 0,
    this.status = UploadTaskStatus.undefined,
    this.fileType = UploadTaskType.undefined,
  });

  bool isCompleted() =>
      this.status == UploadTaskStatus.canceled ||
          this.status == UploadTaskStatus.complete ||
          this.status == UploadTaskStatus.failed;
}