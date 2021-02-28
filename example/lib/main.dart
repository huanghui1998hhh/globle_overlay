import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:globle_overlay/globle_overlay.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool _a;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      await GlobleOverlay.checkAppPermission();
      platformVersion = await GlobleOverlay.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            Container(
              width: double.infinity,
              height: 200,
              child: Center(
                child: Text('Running on: $_platformVersion  $_a'),
              ),
            ),
            RaisedButton(onPressed: () async {
              await Future.delayed(Duration(seconds: 3), () async {
                print(await GlobleOverlay.topApp);
              });
            }),
            RaisedButton(
                child: Text('开启浮窗'),
                onPressed: () async {
                  await GlobleOverlay.openOverlay(argument: "你好");
                }),
            RaisedButton(
                child: Text('关闭浮窗'),
                onPressed: () async {
                  await GlobleOverlay.closeOverlay();
                }),
            RaisedButton(
                child: Text('开启浮窗权限'),
                onPressed: () async {
                  await GlobleOverlay.checkPermission();
                }),
            RaisedButton(
                child: Text('开启权限'),
                onPressed: () async {
                  await GlobleOverlay.checkAppPermission();
                }),
            RaisedButton(
                child: Text('开始监听'),
                onPressed: () async {
                  await GlobleOverlay.startListen("你好");
                }),
            RaisedButton(
                child: Text('关闭监听'),
                onPressed: () async {
                  await GlobleOverlay.endListen();
                }),
          ],
        ),
      ),
    );
  }
}
