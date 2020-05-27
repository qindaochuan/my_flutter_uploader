import 'package:myflutteruploader/myflutteruploader.dart';

class UploadItem {
  String localPath;
  String id;
  String tag;
  MediaType type;
  int progress;
  UploadTaskStatus status;

  UploadItem({
    this.localPath,
    this.id,
    this.tag,
    this.type,
    this.progress = 0,
    this.status = UploadTaskStatus.undefined,
  });

  bool isCompleted() =>
      this.status == UploadTaskStatus.canceled ||
          this.status == UploadTaskStatus.complete ||
          this.status == UploadTaskStatus.failed;
}

enum MediaType { Image, Video }