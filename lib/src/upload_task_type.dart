///
/// A class defines a set of possible type of a upload task
///
class UploadTaskType {
  final int _value;

  const UploadTaskType(int value) : _value = value;

  int get value => _value;

  get hashCode => _value;

  operator ==(status) => status._value == this._value;

  toString() => 'UploadTaskStatus($_value)';

  static UploadTaskType from(int value) => UploadTaskType(value);

  static const undefined = const UploadTaskType(0);
  static const image = const UploadTaskType(1);
  static const video = const UploadTaskType(2);
  static const file = const UploadTaskType(3);

  String get description {
    if (value == null) return "Undefined";
    switch (value) {
      case 1:
        return "Image";
      case 2:
        return "Video";
      case 3:
        return "File";
      default:
        return "Undefined";
    }
  }
}