package com.app.aoplibrary.aspect;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.app.aoplibrary.annotation.CheckPermission;
import com.app.aoplibrary.internal.permission.PermissionUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;


@Aspect
public class CheckPermissionAspect {

    @Pointcut("execution(@com.app.aoplibrary.annotation.CheckPermission * *(..))")
    public void checkPermissionPointCut() {
    }

    @Around("checkPermissionPointCut()")
    public void dealPoint(final ProceedingJoinPoint point) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        // 获取注解
        CheckPermission annotation = methodSignature.getMethod().getAnnotation(CheckPermission.class);
        String[] value = annotation.value();
        Context context = null;
        try {
            if (point.getThis() instanceof android.support.v4.app.Fragment) {
                android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) point.getThis();
                context = fragment.getActivity();
            } else {
                context = (Context) point.getThis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Activity activity = (Activity) context;
        if (PermissionUtil.hasPermissions(context, value)) {
            point.proceed();
        } else {
//            ABC$$ZYAO zyao$$ZYAO = new ABC$$ZYAO();

            PermissionUtil.requestPermissions(context, value, new ActivityCompat.OnRequestPermissionsResultCallback() {
                @Override
                public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                    // 这个是防止上层权限申请之后有特殊的需求，如果调用方主要在权限申请回调之后做特殊处理，可以继承AOPPermissionResultCallback来实现
                    if (PermissionUtil.requestPermissionsResult(grantResults)) {
                        try {
                            point.proceed();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    } else {
                        activity.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    }
                }
            });
        }
    }
}
