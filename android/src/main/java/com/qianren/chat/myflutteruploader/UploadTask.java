package com.qianren.chat.myflutteruploader;

public class UploadTask {
  int primaryId;
  String taskId;
  int status;
  int progress;
  String uploadurl;
  String downloadurl;
  String localePath;
  int fileType;
  String fieldname;
  String method;
  String headers;
  String data;
  int requestTimeoutInSeconds;
  boolean showNotification;
  boolean binaryUpload;
  boolean resumable;
  long timeCreated;

  public UploadTask(int primaryId, String taskId, int status, int progress, String uploadurl, String downloadurl, String localePath,
                    int fileType, String fieldname, String method, String headers, String data, int requestTimeoutInSeconds,
                    boolean showNotification, boolean binaryUpload, boolean resumable, long timeCreated) {
    this.primaryId = primaryId;
    this.taskId = taskId;
    this.status = status;
    this.progress = progress;
    this.uploadurl = uploadurl;
    this.downloadurl = downloadurl;
    this.localePath = localePath;
    this.fileType = fileType;
    this.fieldname = fieldname;
    this.method = method;
    this.headers = headers;
    this.data = data;
    this.requestTimeoutInSeconds = requestTimeoutInSeconds;
    this.showNotification = showNotification;
    this.binaryUpload = binaryUpload;
    this.resumable = resumable;
    this.timeCreated = timeCreated;
  }

  @Override
  public String toString() {
    return "UploadTask{" +
            "primaryId=" + primaryId +
            ", taskId='" + taskId + '\'' +
            ", status=" + status +
            ", progress=" + progress +
            ", uploadurl='" + uploadurl + '\'' +
            ", downloadurl='" + downloadurl + '\'' +
            ", localePath='" + localePath + '\'' +
            ", fileType=" + fileType +
            ", fieldname='" + fieldname + '\'' +
            ", method='" + method + '\'' +
            ", headers='" + headers + '\'' +
            ", data='" + data + '\'' +
            ", requestTimeoutInSeconds=" + requestTimeoutInSeconds +
            ", showNotification=" + showNotification +
            ", binaryUpload=" + binaryUpload +
            ", resumable=" + resumable +
            ", timeCreated=" + timeCreated +
            '}';
  }
}

