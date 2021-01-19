import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:globle_overlay/globle_overlay.dart';

void main() {
  const MethodChannel channel = MethodChannel('globle_overlay');

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
    expect(await GlobleOverlay.platformVersion, '42');
  });
}
