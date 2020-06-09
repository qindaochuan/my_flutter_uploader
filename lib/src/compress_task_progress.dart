import 'upload_task_status.dart';

class CompressTaskProgress {
  final String compress_taskId;
  final UploadTaskStatus compress_status;
  final int compress_progress;
  final String upload_taskId;

  CompressTaskProgress(this.compress_taskId, this.compress_status, this.compress_progress, this.upload_taskId);
}