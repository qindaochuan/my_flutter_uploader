import 'package:myflutteruploader/myflutteruploader.dart';

class UploadItem {
  String localPath;
  String uploadurl;
  String downloadurl;
  String taskId;
  int progress;
  UploadTaskStatus status;

  UploadItem({
    this.localPath,
    this.uploadurl,
    this.downloadurl,
    this.taskId,
    this.progress = 0,
    this.status = UploadTaskStatus.undefined,
  });

  bool isCompleted() =>
      this.status == UploadTaskStatus.canceled ||
          this.status == UploadTaskStatus.complete ||
          this.status == UploadTaskStatus.failed;
}