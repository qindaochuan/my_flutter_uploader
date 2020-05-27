package com.qianren.chat.myflutteruploader;

public class UploadTask {
  int primaryId;
  String taskId;
  int status;
  int progress;
  String uploadurl;
  String downloadurl;
  String localePath;
  String fieldname;
  String method;
  String headers;
  String data;
  int requestTimeoutInSeconds;
  boolean showNotification;
  boolean binaryUpload;
  String mimeType;
  boolean resumable;
  long timeCreated;

  public UploadTask(int primaryId, String taskId, int status, int progress, String uploadurl, String downloadurl, String localePath,
                    String fieldname, String method, String headers, String data, int requestTimeoutInSeconds, boolean showNotification,
                    boolean binaryUpload, String mimeType, boolean resumable, long timeCreated) {
    this.primaryId = primaryId;
    this.taskId = taskId;
    this.status = status;
    this.progress = progress;
    this.uploadurl = uploadurl;
    this.downloadurl = downloadurl;
    this.localePath = localePath;
    this.fieldname = fieldname;
    this.method = method;
    this.headers = headers;
    this.data = data;
    this.requestTimeoutInSeconds = requestTimeoutInSeconds;
    this.showNotification = showNotification;
    this.binaryUpload = binaryUpload;
    this.mimeType = mimeType;
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
            ", fieldname='" + fieldname + '\'' +
            ", method='" + method + '\'' +
            ", headers='" + headers + '\'' +
            ", data='" + data + '\'' +
            ", requestTimeoutInSeconds=" + requestTimeoutInSeconds +
            ", showNotification=" + showNotification +
            ", binaryUpload=" + binaryUpload +
            ", mimeType='" + mimeType + '\'' +
            ", resumable=" + resumable +
            ", timeCreated=" + timeCreated +
            '}';
  }
}

