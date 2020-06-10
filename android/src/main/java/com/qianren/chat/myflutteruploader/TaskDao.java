package com.qianren.chat.myflutteruploader;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private TaskDbHelper dbHelper;

    final private String[] projection = new String[]{
            BaseColumns._ID,
            TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID,
            TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS,
            TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS,
            TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_URL,
            TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_RESPONSE,
            TaskContract.TaskEntry.COLUMN_NAME_LOCALE_PATH,
            TaskContract.TaskEntry.COLUMN_NAME_FILE_TYPE,
            TaskContract.TaskEntry.COLUMN_NAME_FIELD_NAME,
            TaskContract.TaskEntry.COLUMN_NAME_METHOD,
            TaskContract.TaskEntry.COLUMN_NAME_HEADERS,
            TaskContract.TaskEntry.COLUMN_NAME_DATA,
            TaskContract.TaskEntry.COLUMN_NAME_REQUEST_TIMEOUT_IN_SECONDS,
            TaskContract.TaskEntry.COLUMN_NAME_SHOW_NOTIFICATION,
            TaskContract.TaskEntry.COLUMN_NAME_BINARY_UPLOAD,
            TaskContract.TaskEntry.COLUMN_NAME_RESUMABLE,
            TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TIME_CREATED,
            TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID,
            TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_STATUS,
            TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PROGRESS,
            TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PATH,
            TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TIME_CREATED
    };

    public TaskDao(TaskDbHelper helper) {
        dbHelper = helper;
    }

    public void insertOrUpdateNewUploadTask(String taskId, int status, int progress, String uploadurl,
    String localePath, int fileType ,String fieldname,
    String method, String headers, String data, int requestTimeoutInSeconds, boolean showNotification, boolean binaryUpload){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID,taskId);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS,status);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS,progress);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_URL,uploadurl);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_RESPONSE,"");
        values.put(TaskContract.TaskEntry.COLUMN_NAME_LOCALE_PATH,localePath);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_FILE_TYPE,fileType);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_FIELD_NAME,fieldname);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_METHOD,method);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_HEADERS,headers);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DATA,data);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_REQUEST_TIMEOUT_IN_SECONDS,requestTimeoutInSeconds);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_SHOW_NOTIFICATION,showNotification ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_BINARY_UPLOAD,binaryUpload ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_RESUMABLE,0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TIME_CREATED,System.currentTimeMillis());

        db.beginTransaction();
        try {
            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void insertOrUpdateNewCompressVieoTask(String taskId, int status, int progress, String uploadurl,
                                            String localePath, int fileType ,String fieldname,
                                            String method, String headers, String data, int requestTimeoutInSeconds, boolean showNotification, boolean binaryUpload){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_URL,uploadurl);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_RESPONSE,"");
        values.put(TaskContract.TaskEntry.COLUMN_NAME_LOCALE_PATH,localePath);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_FILE_TYPE,fileType);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_FIELD_NAME,fieldname);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_METHOD,method);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_HEADERS,headers);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DATA,data);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_REQUEST_TIMEOUT_IN_SECONDS,requestTimeoutInSeconds);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_SHOW_NOTIFICATION,showNotification ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_BINARY_UPLOAD,binaryUpload ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_RESUMABLE,0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID,taskId);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_STATUS,status);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PROGRESS,progress);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TIME_CREATED,System.currentTimeMillis());

        db.beginTransaction();
        try {
            db.insertWithOnConflict(TaskContract.TaskEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public List<UploadTask> loadAllTasks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<UploadTask> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(parseCursor(cursor));
        }
        cursor.close();

        return result;
    }

    public List<UploadTask> loadTasksWithRawQuery(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        List<UploadTask> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(parseCursor(cursor));
        }
        cursor.close();

        return result;
    }

    public UploadTask loadTaskByUploadTaskId(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String whereClause = TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?";
        String[] whereArgs = new String[]{taskId};

        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                whereClause,
                whereArgs,
                null,
                null,
                BaseColumns._ID + " DESC",
                "1"
        );

        UploadTask result = null;
        while (cursor.moveToNext()) {
            result = parseCursor(cursor);
        }
        cursor.close();
        return result;
    }

    public UploadTask loadTaskByCompressTaskId(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String whereClause = TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID + " = ?";
        String[] whereArgs = new String[]{taskId};

        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                whereClause,
                whereArgs,
                null,
                null,
                BaseColumns._ID + " DESC",
                "1"
        );

        UploadTask result = null;
        while (cursor.moveToNext()) {
            result = parseCursor(cursor);
        }
        cursor.close();
        return result;
    }

    public void updateTask(String taskId, int status, int progress,String uploadResponse) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS, status);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS, progress);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_RESPONSE, uploadResponse);

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?", new String[]{taskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateUploadTask(String taskId, int status, int progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS, status);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS, progress);

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?", new String[]{taskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void startUploadTaskByCompoerssTask(String compressTaskId, String uploadTaskId, String compressPath){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID, uploadTaskId);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS, UploadStatus.ENQUEUED);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS, 0);

        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_STATUS, UploadStatus.COMPLETE);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PROGRESS, 100);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PATH, compressPath);

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID + " = ?", new String[]{compressTaskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateCompressTask(String taskId, int status, int progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_STATUS, status);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PROGRESS, progress);

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID + " = ?", new String[]{taskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateTask(String currentTaskId, String newTaskId, int status, int progress, boolean resumable) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID, newTaskId);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS, status);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS, progress);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_RESUMABLE, resumable ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TIME_CREATED, System.currentTimeMillis());

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?", new String[]{currentTaskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateTask(String taskId, boolean resumable) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_RESUMABLE, resumable ? 1 : 0);

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?", new String[]{taskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void updateTask(String taskId, String localePath, int fileType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_LOCALE_PATH, localePath);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_FILE_TYPE, fileType);

        db.beginTransaction();
        try {
            db.update(TaskContract.TaskEntry.TABLE_NAME, values, TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?", new String[]{taskId});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteTask(String taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            String whereClause = TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID + " = ?";
            String[] whereArgs = new String[]{taskId};
            db.delete(TaskContract.TaskEntry.TABLE_NAME, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    private UploadTask parseCursor(Cursor cursor) {
        int primaryId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
        String upload_taskId = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TASK_ID));
        int upload_status = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_STATUS));
        int upload_progress = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_PROGRESS));
        String uploadurl = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_URL));
        String upload_response = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_RESPONSE));
        String localePath = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_LOCALE_PATH));
        int fileType = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_FILE_TYPE));
        String fieldname = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_FIELD_NAME));
        String method = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_METHOD));
        String headers = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_HEADERS));
        String data = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DATA));
        int requestTimeoutInSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_REQUEST_TIMEOUT_IN_SECONDS));
        int showNotification = cursor.getShort(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_SHOW_NOTIFICATION));
        int binaryUpload = cursor.getShort(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_BINARY_UPLOAD));
        int resumable = cursor.getShort(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_RESUMABLE));
        long uploadTimeCreated = cursor.getLong(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_UPLOAD_TIME_CREATED));
        String compress_taskId = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TASK_ID));
        int compress_status = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_STATUS));
        int compress_progress = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PROGRESS));
        String compress_path = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_PATH));
        long compressTimeCreated = cursor.getLong(cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPRESS_TIME_CREATED));

        UploadTask uploadTask = new UploadTask();
        uploadTask.setPrimaryId(primaryId);
        uploadTask.setUpload_taskId(upload_taskId);
        uploadTask.setUpload_status(upload_status);
        uploadTask.setUpload_progress(upload_progress);
        uploadTask.setUploadurl(uploadurl);
        uploadTask.setUpload_response(upload_response);
        uploadTask.setLocalePath(localePath);
        uploadTask.setFileType(fileType);
        uploadTask.setFieldname(fieldname);
        uploadTask.setMethod(method);
        uploadTask.setHeaders(headers);
        uploadTask.setData(data);
        uploadTask.setRequestTimeoutInSeconds(requestTimeoutInSeconds);
        uploadTask.setShowNotification(showNotification == 1);
        uploadTask.setBinaryUpload(binaryUpload == 1);
        uploadTask.setResumable(resumable == 1);
        uploadTask.setUpload_timeCreated(uploadTimeCreated);
        uploadTask.setCompress_taskId(compress_taskId);
        uploadTask.setCompress_status(compress_status);
        uploadTask.setCompress_progress(compress_progress);
        uploadTask.setCompress_path(compress_path);
        uploadTask.setCompressTimeCreated(compressTimeCreated);

        return uploadTask;
    }
}
