package com.app.permissiontestapplication;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.app.annotationprocessor.annotation.DynamicPermission;
import com.app.annotationprocessor.annotation.PermissionDenied;
import com.app.aoplibrary.annotation.CheckPermission;
import com.app.permissiontestapplication.R;

@DynamicPermission
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSecond();
            }
        });
    }

    @CheckPermission(value = {Manifest.permission.CALL_PHONE})
    public void checkSecond() {
        Log.i("MainActivity", "SecondActivity==========success");
    }


    @PermissionDenied(value = {Manifest.permission.CALL_PHONE})
    public void checkSecondFailed() {
        Log.i("MainActivity", "SecondActivity==========fail");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        SecondActivity_AutoGenerate.permissionDenied(this, permissions, grantResults);
    }
}
