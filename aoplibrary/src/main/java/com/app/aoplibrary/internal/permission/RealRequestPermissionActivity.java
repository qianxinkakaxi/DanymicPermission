package com.app.aoplibrary.internal.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.app.aoplibrary.R;

public class RealRequestPermissionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_request_permission);
        String[] permissions = getIntent().getStringArrayExtra("permissions");
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    public static void requestPermissionsWithInstance(Context context, String[] value) {
        Intent intent = new Intent(context, RealRequestPermissionActivity.class);
        intent.putExtra("permissions", value);
        context.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Intent intent = new Intent(Constants.PERMISSION_REQUEST_RESULT_BROADCAST);
        intent.putExtra(Constants.REQUEST_CODE, requestCode);
        intent.putExtra(Constants.PERMISSIONS, permissions);
        intent.putExtra(Constants.GRANT_RESULTS, grantResults);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }
}
