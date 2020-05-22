import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:myflutteruploader/myflutteruploader.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<File> _files = List<File>();

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: ListView.builder(
            itemCount: _files.length,
            itemBuilder: (BuildContext context, int index) {
              return ImageItem(index);
            },
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            _files = await FilePicker.getMultiFile(type: FileType.image);
            setState(() {});
          },
          tooltip: 'Increment',
          child: Icon(Icons.add),
        ), //
      ),
    );
  }

  Widget ImageItem(int index) {
    UploadItem item = UploadItem();
    item = item.copyWith(progress: 50, status: UploadTaskStatus.running);
    final progress = item.progress.toDouble() / 100;
    final widget = item.status == UploadTaskStatus.running
        ? LinearProgressIndicator(value: progress)
        : Container();
    final buttonWidget = item.status == UploadTaskStatus.running
        ? Container(
      height: 50,
      width: 50,
      child: IconButton(
        icon: Icon(Icons.cancel),
        onPressed: () {
          //onCancel(item.id);
        },
      ),
    )
        : Container();

    final imageWidget = Image.file(
      _files[index],
      width: 150,
      height: 150,
    );

    return Container(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Container(
            height: 20.0,
          ),
          Row(
            children: <Widget>[
              imageWidget,
              buttonWidget,
            ],
          ),
          Container(
            height: 5.0,
          ),
          item.status == UploadTaskStatus.running ? Text(item.status.description) : Container(),
          Container(
            height: 5.0,
          ),
          widget
        ],
      ),
    );
  }
}

class UploadItem {
  final String id;
  final String tag;
  final MediaType type;
  final int progress;
  final UploadTaskStatus status;

  UploadItem({
    this.id,
    this.tag,
    this.type,
    this.progress = 0,
    this.status = UploadTaskStatus.undefined,
  });

  UploadItem copyWith({UploadTaskStatus status, int progress}) => UploadItem(
      id: this.id,
      tag: this.tag,
      type: this.type,
      status: status ?? this.status,
      progress: progress ?? this.progress);

  bool isCompleted() =>
      this.status == UploadTaskStatus.canceled ||
          this.status == UploadTaskStatus.complete ||
          this.status == UploadTaskStatus.failed;
}

enum MediaType { Image, Video }
