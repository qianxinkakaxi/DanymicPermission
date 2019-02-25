package com.app.aoplibrary.internal.permission;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

public class PermissionUtil {


    public static boolean hasPermissions(Context context, @NonNull String... perms) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (context == null) {
            throw new IllegalArgumentException("Can't check permissions for null context");
        }
        for (String perm : perms) {
            if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(Context context, String[] value, ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.PERMISSION_REQUEST_RESULT_BROADCAST);
        LocalReceiver localReceiver = new LocalReceiver(onRequestPermissionsResultCallback);
        //注册本地接收器
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        RealRequestPermissionActivity.requestPermissionsWithInstance(context, value);
    }

    private static class LocalReceiver extends BroadcastReceiver {
        private ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

        public LocalReceiver(ActivityCompat.OnRequestPermissionsResultCallback permissionCallbacks) {
            this.onRequestPermissionsResultCallback = permissionCallbacks;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            onRequestPermissionsResultCallback.onRequestPermissionsResult(intent.getIntExtra(Constants.REQUEST_CODE, -1),
                    intent.getStringArrayExtra(Constants.PERMISSIONS),
                    intent.getIntArrayExtra(Constants.GRANT_RESULTS));
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    }

  // @PermissionDenied(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public static boolean requestPermissionsResult(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != 0) {
                return false;
            }
        }
        return true;
    }
}
