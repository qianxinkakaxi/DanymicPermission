package com.app.testlibrary;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.app.annotationprocessor.annotation.DynamicPermission;
import com.app.annotationprocessor.annotation.PermissionDenied;
import com.app.aoplibrary.annotation.CheckPermission;
import com.app.testlibrary.R;

@DynamicPermission
public class TestMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }


    @CheckPermission(value = {Manifest.permission.CALL_PHONE})
    public void check() {
        Log.i("MainActivity", "SecondActivity==========success");
    }


    @PermissionDenied(value = {Manifest.permission.CALL_PHONE})
    public void checkSecondFailed() {
        Log.i("MainActivity", "SecondActivity==========fail");
    }


}

