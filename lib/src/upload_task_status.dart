///
/// A class defines a set of possible statuses of a upload task
///
class UploadTaskStatus {
  final int _value;

  const UploadTaskStatus(int value) : _value = value;

  int get value => _value;

  get hashCode => _value;

  operator ==(status) => status._value == this._value;

  toString() => 'DownloadTaskStatus($_value)';

  static UploadTaskStatus from(int value) => UploadTaskStatus(value);

  static const undefined = const UploadTaskStatus(0);
  static const enqueued = const UploadTaskStatus(1);
  static const running = const UploadTaskStatus(2);
  static const complete = const UploadTaskStatus(3);
  static const failed = const UploadTaskStatus(4);
  static const canceled = const UploadTaskStatus(5);
  static const paused = const UploadTaskStatus(6);

  String get description {
    if (value == null) return "Undefined";
    switch (value) {
      case 1:
        return "Enqueued";
      case 2:
        return "Running";
      case 3:
        return "Completed";
      case 4:
        return "Failed";
      case 5:
        return "Cancelled";
      default:
        return "Undefined";
    }
  }
}
