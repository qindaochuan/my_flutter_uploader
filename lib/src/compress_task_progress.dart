import 'upload_task_status.dart';

class CompressTaskProgress {
  final String taskId;
  final int progress;
  final UploadTaskStatus status;

  CompressTaskProgress(this.taskId, this.progress, this.status);
}