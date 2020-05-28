package com.qianren.chat.myflutteruploader;

public class UploadResponse {
    private int code;
    private String msg;
    private UploadResponseData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UploadResponseData getData() {
        return data;
    }

    public void setData(UploadResponseData data) {
        this.data = data;
    }
}
