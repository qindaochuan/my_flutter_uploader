package com.qianren.chat.myflutteruploader;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;

public class CompressVideoProgressReporter extends LiveData<CompressVideoProgress> {
    private static CompressVideoProgressReporter _instance;

    @MainThread
    public static CompressVideoProgressReporter getInstance() {
        if (_instance == null) {
            _instance = new CompressVideoProgressReporter();
        }
        return _instance;
    }

    void notifyProgress(CompressVideoProgress progress) {
        postValue(progress);
    }
}
