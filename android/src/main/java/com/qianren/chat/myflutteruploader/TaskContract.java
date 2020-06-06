package com.qianren.chat.myflutteruploader;

import android.provider.BaseColumns;

public class TaskContract {

    private TaskContract() {}

    public static class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "task";
        public static final String COLUMN_NAME_UPLOAD_TASK_ID = "upload_task_id";
        public static final String COLUMN_NAME_UPLOAD_STATUS = "upload_status";
        public static final String COLUMN_NAME_UPLOAD_PROGRESS = "upload_progress";
        public static final String COLUMN_NAME_UPLOAD_URL = "upload_url";
        public static final String COLUMN_NAME_UPLOAD_RESPONSE = "upload_response";
        public static final String COLUMN_NAME_LOCALE_PATH = "localePath";
        public static final String COLUMN_NAME_FILE_TYPE = "file_type";
        public static final String COLUMN_NAME_FIELD_NAME = "field_name";
        public static final String COLUMN_NAME_METHOD = "method";
        public static final String COLUMN_NAME_HEADERS = "headers";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_REQUEST_TIMEOUT_IN_SECONDS = "request_timeout_in_seconds";
        public static final String COLUMN_NAME_SHOW_NOTIFICATION = "show_notification";
        public static final String COLUMN_NAME_BINARY_UPLOAD = "binary_upload";
        public static final String COLUMN_NAME_RESUMABLE = "resumable";
        public static final String COLUMN_NAME_UPLOAD_TIME_CREATED = "upload_time_created";
        public static final String COLUMN_NAME_COMPRESS_TASK_ID = "compress_task_id";
        public static final String COLUMN_NAME_COMPRESS_STATUS = "compress_status";
        public static final String COLUMN_NAME_COMPRESS_PROGRESS = "compress_progress";
        public static final String COLUMN_NAME_COMPRESS_PATH = "compress_path";
        public static final String COLUMN_NAME_COMPRESS_TIME_CREATED = "compress_time_created";
    }

}
