import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'dart:async';
import 'package:path/path.dart' as pathTools;

import 'package:flutter/services.dart';
import 'package:myflutteruploader/myflutteruploader.dart';

const String uploadURL = "http://prod-upload.cqxzyjy.com/uploadPic";

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StreamSubscription _progressSubscription;
  StreamSubscription _resultSubscription;
  List<UploadItem> _tasks = [];
  ScrollController _controller = ScrollController();

  @override
  void initState() {
    super.initState();
    MyFlutterUploader.initialize();
    _progressSubscription = MyFlutterUploader.progressController.stream.listen((progress) {
      print("progress: ${progress.progress} , status: ${progress.status}");
      UploadItem task;
      for(int i = 0; i < _tasks.length; i++){
        if(_tasks[i].id == progress.taskId){
          task = _tasks[i];
          break;
        }
      }
      if (task == null) return;
      if (task.isCompleted()) return;
      setState(() {
        task.progress = progress.progress;
        task.status = progress.status;
      });
    });

    _resultSubscription = MyFlutterUploader.responseController.stream.listen((result) {
      print(
          "id: ${result.taskId}, status: ${result.status}, response: ${result.response}, statusCode: ${result.statusCode}, headers: ${result.headers}");

      UploadItem task;
      for(int i = 0; i < _tasks.length; i++){
        if(_tasks[i].id == result.taskId){
          task = _tasks[i];
          break;
        }
      }
      if (task == null) return;

      setState(() {
        task.status = result.status;
      });
    }, onError: (ex, stacktrace) {
      print("exception: $ex");
      print("stacktrace: $stacktrace" ?? "no stacktrace");
    });
  }

  @override
  Widget build(BuildContext context) {
    Timer(Duration(milliseconds: 50), () => _controller.jumpTo(_controller.position.maxScrollExtent));
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: ListView.separated(
            controller: _controller,
            padding: EdgeInsets.all(20.0),
            itemCount: _tasks.length,
            itemBuilder: (BuildContext context, int index) {
              final item = _tasks.elementAt(index);
              print("${item.tag} - ${item.status}");
              return ImageItem(index);
            },
            separatorBuilder: (context, index) {
              return Divider(
                color: Colors.black,
              );
            },
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            List<File> _files = await FilePicker.getMultiFile(type: FileType.image);
            for(int i = 0; i < _files.length; i++){
              multieUpload(_files[i].path);
            }
            setState(() {});
          },
          tooltip: 'Increment',
          child: Icon(Icons.add),
        ), //
      ),
    );
  }

  Widget ImageItem(int index) {
    UploadItem item = _tasks[index];
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
      File(_tasks[index].localPath),
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

  Future<Null> multieUpload(String path) async{
    if(path == null){
      return null;
    }

    final String filename = pathTools.basename(path);
    final String savedDir = pathTools.dirname(path);

    final tag = "image upload ${_tasks.length + 1}";
    var url = uploadURL;
    var fileItem = FileItem(
      filename: filename,
      savedDir: savedDir,
      fieldname: "uploadfile",
    );

    var taskId = await MyFlutterUploader.enqueue(
      url: url,
      data: {"name": "john"},
      files: [fileItem],
      method: UploadMethod.POST,
      showNotification: true,
    );

    _tasks.add(
        UploadItem(
          localPath: path,
          id: taskId,
          tag: tag,
          type: MediaType.Video,
          status: UploadTaskStatus.enqueued,
        ));

    setState(() {

    });
  }
}

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
