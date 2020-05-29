import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:myflutteruploader_example/videoPage.dart';
import 'dart:async';
import 'package:path/path.dart' as pathTools;

import 'package:myflutteruploader/myflutteruploader.dart';

import 'UploadItem.dart';

const String uploadURL = "http://prod-upload.cqxzyjy.com/uploadPic";

class ImagePage extends StatefulWidget {
  @override
  _ImagePageState createState() => _ImagePageState();
}

class _ImagePageState extends State<ImagePage> {
  StreamSubscription _progressSubscription;
  StreamSubscription _resultSubscription;
  List<UploadItem> _uploadItemList = [];
  ScrollController _controller = ScrollController();

  void _prepare() async{
    List<UploadTask> tasks = await MyFlutterUploader.loadTasks();
    for(int i = 0; i < tasks.length; i++){
      UploadTask task = tasks[i];
      _uploadItemList.add(UploadItem(
        uploadurl: task.uploadurl,
        downloadurl: task.downloadurl,
        localPath:task.localePath,
        taskId:task.taskId,
        progress:task.progress,
        status:task.status,
      ));
    }
    setState(() {
      
    });
  }

  @override
  void initState() {
    super.initState();
    _prepare();
    _progressSubscription = MyFlutterUploader.progressController.stream.listen((progress) {
      print("progress: ${progress.progress} , status: ${progress.status}");
      UploadItem task;
      for(int i = 0; i < _uploadItemList.length; i++){
        if(_uploadItemList[i].taskId == progress.taskId){
          task = _uploadItemList[i];
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
      for(int i = 0; i < _uploadItemList.length; i++){
        if(_uploadItemList[i].taskId == result.taskId){
          task = _uploadItemList[i];
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
  void dispose() {
    super.dispose();
    _progressSubscription?.cancel();
    _resultSubscription?.cancel();
  }

  @override
  Widget build(BuildContext context) {
    Timer(Duration(milliseconds: 50), () => _controller.jumpTo(_controller.position.maxScrollExtent));
    return Scaffold(
      appBar: AppBar(
        title: const Text('Upload Image Page'),
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.video_library),
            onPressed: () {
              Navigator.push(context, new MaterialPageRoute(
                  builder: (BuildContext context) {
                    return VideoPage();
                  }));
            },
            tooltip: '完成',
          ),
        ],
      ),
      body: Center(
        child: Column(
          children: <Widget>[
            Expanded(
              child: ListView.separated(
                controller: _controller,
                padding: EdgeInsets.all(20.0),
                itemCount: _uploadItemList.length,
                itemBuilder: (BuildContext context, int index) {
                  final item = _uploadItemList.elementAt(index);
                  print("${item.taskId} - ${item.status}");
                  return ImageItem(index);
                },
                separatorBuilder: (context, index) {
                  return Divider(
                    color: Colors.black,
                  );
                },
              ),
            ),
            Container(
              height: 80,
            )
          ],
        )
      ),
      floatingActionButton: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: <Widget>[
          FloatingActionButton(
            onPressed: () async {
              List<File> _files = await FilePicker.getMultiFile(type: FileType.image);
              if(_files != null) {
                for (int i = 0; i < _files.length; i++) {
                  multiUpload(_files[i].path);
                }
              }
              setState(() {});
            },
            child: Icon(Icons.image),
          ),
          FloatingActionButton(
            onPressed: () async {
              await FilePicker.getMultiFile(type: FileType.video);
              setState(() {});
            },
            child: Icon(Icons.video_library),
          ),
          FloatingActionButton(
            onPressed: () async {
              await FilePicker.getMultiFile(type: FileType.any);
              setState(() {});
            },
            child: Icon(Icons.insert_drive_file),
          ), //////
        ],
      ),
    );
  }

  Widget ImageItem(int index) {
    UploadItem item = _uploadItemList[index];
    final progress = item.progress.toDouble() / 100;
    final widget = item.status == UploadTaskStatus.running
        ? LinearProgressIndicator(value: progress)
        : Container();

    final imageWidget = Image.file(
      File(_uploadItemList[index].localPath),
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
          imageWidget,
          widget,
          _buildActionForTask(item),
        ],
      ),
    );
  }

  Future<Null> multiUpload(String path) async{
    if(path == null){
      return null;
    }

    final String filename = pathTools.basename(path);
    final String savedDir = pathTools.dirname(path);

    final tag = "image upload ${_uploadItemList.length + 1}";

    var taskId = await MyFlutterUploader.enqueue(
      uploadurl: uploadURL,
      localePath: path,
      fieldname:"uploadfile",
      data: {"name": "john"},
      method: UploadMethod.POST,
      showNotification: true,
    );

    _uploadItemList.add(
        UploadItem(
          uploadurl: uploadURL,
          localPath: path,
          taskId: taskId,
          status: UploadTaskStatus.enqueued,
        ));

    setState(() {

    });
  }

  Widget _buildActionForTask(UploadItem task) {
    if (task.status == UploadTaskStatus.undefined) {
      return new Container();
    } else if (task.status == UploadTaskStatus.enqueued) {
      return new Container();
    } else if (task.status == UploadTaskStatus.running) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          task.status == UploadTaskStatus.running ? Text(task.status.description,
            style: new TextStyle(color: Colors.green),
          ) : Container(),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.pause,
              color: Colors.red,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          ),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.cancel,
              color: Colors.red,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          )
        ],
      );
    } else if (task.status == UploadTaskStatus.paused) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          task.status == UploadTaskStatus.running ? Text(task.status.description,
            style: new TextStyle(color: Colors.green),
          ) : Container(),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.play_arrow,
              color: Colors.green,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          ),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.cancel,
              color: Colors.red,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          )
        ],
      );
    } else if (task.status == UploadTaskStatus.complete) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          task.status == UploadTaskStatus.running ? Text(task.status.description,
            style: new TextStyle(color: Colors.green),
          ) : Container(),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.send,
              color: Colors.blue,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          ),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.cancel,
              color: Colors.red,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          )
        ],
      );
    } else if (task.status == UploadTaskStatus.canceled) {
      return new Text('Canceled', style: new TextStyle(color: Colors.red));
    } else if (task.status == UploadTaskStatus.failed) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          new Text('Failed', style: new TextStyle(color: Colors.red)),
          RawMaterialButton(
            onPressed: () {
              //_retryDownload(task);
            },
            child: Icon(
              Icons.refresh,
              color: Colors.green,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          ),
          RawMaterialButton(
            onPressed: () {
              //_delete(task);
            },
            child: Icon(
              Icons.cancel,
              color: Colors.red,
            ),
            shape: new CircleBorder(),
            constraints: new BoxConstraints(minHeight: 32.0, minWidth: 32.0),
          )
        ],
      );
    } else {
      return null;
    }
  }

  void _cancelDownload(UploadItem task) async {
    await MyFlutterUploader.cancel(taskId: task.taskId);
  }

  void _pauseDownload(UploadItem task) async {
    await MyFlutterUploader.pause(taskId: task.taskId);
  }

  void _resumeDownload(UploadItem task) async {
    String newTaskId = await MyFlutterUploader.resume(taskId: task.taskId);
    task.taskId = newTaskId;
  }

  void _retryDownload(UploadItem task) async {
    String newTaskId = await MyFlutterUploader.retry(taskId: task.taskId);
    task.taskId = newTaskId;
  }

  void _delete(UploadItem task) async {
    await MyFlutterUploader.remove(taskId: task.taskId);
    //await _prepare();
    setState(() {});
  }
}


