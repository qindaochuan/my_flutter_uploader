package com.qianren.chat.myflutteruploader;

public class CompressVideoProgress {
    private String compress_taskId;
    private int compress_status;
    private int compress_progress;
    private String upload_taskId;

    public CompressVideoProgress(String compress_taskId, int compress_status, int compress_progress, String upload_taskId) {
        this.compress_taskId = compress_taskId;
        this.compress_status = compress_status;
        this.compress_progress = compress_progress;
        this.upload_taskId = upload_taskId;
    }

    public CompressVideoProgress(String compress_taskId, int compress_status, int compress_progress) {
        this.compress_taskId = compress_taskId;
        this.compress_status = compress_status;
        this.compress_progress = compress_progress;
    }

    public String getCompress_taskId() {
        return compress_taskId;
    }

    public int getCompress_status() {
        return compress_status;
    }

    public int getCompress_progress() {
        return compress_progress;
    }

    public String getUpload_taskId() {
        return upload_taskId;
    }
}
