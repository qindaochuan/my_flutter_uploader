package com.qianren.chat.myflutteruploader;

import android.app.Activity;
import android.content.Intent;

import io.flutter.plugin.common.PluginRegistry;

public class MyflutteruploaderDelegate implements PluginRegistry.ActivityResultListener, PluginRegistry.RequestPermissionsResultListener{
    public MyflutteruploaderDelegate(Activity setupActivity) {
    }

    void saveStateBeforeResult() {

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        return false;
    }
}
