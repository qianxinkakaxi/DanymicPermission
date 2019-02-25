package com.app.permissiontestapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.app.annotationprocessor.annotation.DynamicPermission;
import com.app.annotationprocessor.annotation.PermissionDenied;
import com.app.aoplibrary.annotation.CheckPermission;
import com.app.permissiontestapplication.R;

import java.util.List;

@DynamicPermission
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.test).setOnClickListener(v -> check());

    }

    @CheckPermission(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void check() {
        Log.i("MainActivity", "MainActivity==========weqwrqwrqw");
        startActivity(new Intent(this, SecondActivity.class));
    }

    @PermissionDenied(value = {Manifest.permission.WRITE_CONTACTS, Manifest.permission.CALL_PHONE})
    public boolean hasAppInstalled() {
        Log.i("MainActivity", "MainActivityqrqwrr==========");
        return false;
    }


    @PermissionDenied(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public boolean has() {
        Log.i("MainActivity", "MainActivity==========");
        return false;
    }

}
