package com.app.aoplibrary.internal.permission;

import android.support.annotation.NonNull;

/**
 * 需要自己实现权限回调，需要在checkPermission的类里面实现这个类
 */
public interface AOPPermissionResultCallback {

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                    @NonNull int[] grantResults);

}
