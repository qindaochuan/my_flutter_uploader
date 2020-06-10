import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
//import 'package:myflutteruploader/myflutteruploader.dart';

void main() {
  const MethodChannel channel = MethodChannel('myflutteruploader');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    //expect(await Myflutteruploader.platformVersion, '42');
  });
}
