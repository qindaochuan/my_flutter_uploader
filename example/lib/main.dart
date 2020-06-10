import 'package:flutter/material.dart';

import 'package:my_flutter_uploader/myflutteruploader.dart';

import 'imagePage.dart';
//import 'videoPage.dart';

void main() async{
  await MyFlutterUploader.initialize();
  runApp(MaterialApp(
    debugShowCheckedModeBanner: false,
    home: ImagePage(),
  ));
}