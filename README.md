## DynamicPermission1.在项目中使用AspectJ实现动态权限申请

### 1.在项目中接入aspectJ

aspectJ在介入的时候需要注意library和projecr的区别，这两个在接入的时候代码是不同的

#### -  在工程的build.gradle的中引入maven库
    

```
classpath 'com.xports:danymic-permission-plugin:1.0.0'
```


#### -  在module中接入aspectJ
1.    module的类型是project
  
在build.gradle文件头中引入
      
```
import com.app.plugin.AspectjPlugin
```


在build.gradle文件底部添加代码
    
```
apply plugin: AspectjPlugin
```


2.    module的类型是library

在build.gradle文件头中引入
      
```
import com.app.plugin.AspectjLibrary
```


在build.gradle文件底部添加代码
       
```
apply plugin: AspectjLibrary
```

### 2.利用aspectj实现动态权限封装申请
1. 在项目中引入
  
```
implementation 'com.xports:aoplibrary:1.0.0'
```
2. 在需要申请动态权限的方法名上添加切点CheckPermission，其中value的值是需要申请的权限，是一个String数组

```
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPermission {
    String[] value();
}
```

比如下面的代码，只有当权限申请成功之后，才会执行到check方法里面来

```
@CheckPermission(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void check() {
        Log.i("MainActivity", "MainActivity==========weqwrqwrqw");
    }
```

MainActivity的代码如下

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }

    @CheckPermission(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void check() {
        Toast.makeText(this,"successful",1).show();
    }
}
```

[效果如图](https://note.youdao.com/yws/public/resource/bd24ff9b39c238c024a1dd1a87a9f03d/xmlnote/9265C26D188341CCA22BAB9C93E90E2C/1110)

## DynamicPermission2.在项目中使用Apt生成权限申请失败调用文件

在上面我们只讲到了用户允许权限运行没有问题，但是并没有用户拒绝之后的处理，我们之前写代码的时候，一般是在下面的代码根据grantResults来执行代码的
 
```
@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
```
我们可以使用annotationProcessor（apt）来自动生成权限申请失败的回调代码，最后将代码绑定到onRequestPermissionsResult里面来执行

首先，build.gradle引入相应的库
在配置文件里面声明使用java 1.8语法解析

```
android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.example.a60282.dpdemoapplication"
        minSdkVersion 19
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {//使用JAVA8语法解析
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```


```
   annotationProcessor ('com.xports:danymic-permission-annotation:1.0.0')
   implementation ('com.xports:danymic-permission-annotation:1.0.0')
```

在需要处理权限拒绝的activity的上面添加注解申明需要处理权限拒绝的情况

```
package com.example.a60282.dpdemoapplication;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.app.annotationprocessor.annotation.DynamicPermission;
import com.app.annotationprocessor.annotation.PermissionDenied;
import com.app.aoplibrary.annotation.CheckPermission;

@DynamicPermission
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }

    @CheckPermission(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    private void check() {
        Toast.makeText(this, "successful", 1).show();
    }


    @PermissionDenied(value = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void permissionDenied() {
        Toast.makeText(this, "PermissionDenied", 1).show();
    }

}
```
在编译的时候，看到这个标识的时候，就会在相应的包下面生成对应的XXX_AutoGenerate这个类。

这个类是apt生成的，其中java代码是利用javapoet生成的，路径是build\generated\source\apt\debug 对应的包名下面，生成的代码如下

```
package com.example.a60282.dpdemoapplication;

import java.lang.String;

public final class MainActivity_AutoGenerate {
  public static void permissionDenied(MainActivity activity, String[] permissions,
      int[] grantResults) {
    for (int i = 0; i < permissions.length; i++) {
      if(permissions[i].equals("android.permission.WRITE_EXTERNAL_STORAGE") && grantResults[i] != 0) {
        activity.permissionDenied();
      }
    }
  }
}
```

可以看到在这个类中，当拒绝权限的时候，调用了这个MainActivity的permissionDenied方法

下面我们要做的是将onRequestPermissionsResult方法中，调用MainActivity_AutoGenerate.permissionDenied方法。我们可以复写onRequestPermissionsResult，在里面调用方法。

```
@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivity_AutoGenerate.permissionDenied(this,permissions,grantResults);
    }
```

这样的话，用户申请权限之后就会执行的permissionDenied方法。

这边主要的是利用annotationProcessor处理注解，根据注解生成java类，java代码主要是利用javapoet实现的，具体代码可以查看[https://github.com/qianxinkakaxi/DanymicPermission/tree/master/annotationprocessor](https://github.com/qianxinkakaxi/DanymicPermission/tree/master/annotationprocessor)。

[点击查看最后执行的效果图](https://note.youdao.com/yws/public/resource/bd24ff9b39c238c024a1dd1a87a9f03d/xmlnote/05FE296B6F3B44879D92F357CFAFE3AD/1218)

## DynamicPermission3.使用javassist修改onRequestPermissionsResult方法

在上面最后的时候，会自动生成XXX_AutoGenerate这个类，但是需要我们手动的去复写onRequestPermissionsResult，在里面去调用XXX_AutoGenerate.permissionDenied方法，显得非常笨拙，其实我们可以利用javassist在打包的时候修改class文件，在onRequestPermissionsResult里面调用permissionDenied方法。

首先在build.gradle中引入import com.app.plugin.MyPlugin，在最后使用该plugin，
apply plugin: MyPlugin，demo中完整的build.gradle代码如下


```
apply plugin: 'com.android.application'
import com.app.plugin.AspectjPlugin
import com.app.plugin.MyPlugin

android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.example.a60282.dpdemoapplication"
        minSdkVersion 19
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {//使用JAVA8语法解析
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    testImplementation 'junit:junit:4.12'
    implementation 'com.xports:aoplibrary:1.0.0'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    annotationProcessor ('com.xports:danymic-permission-annotation:1.0.0')
    implementation ('com.xports:danymic-permission-annotation:1.0.0')
}

apply plugin: AspectjPlugin
apply plugin: MyPlugin
```
这样的话，就可以不用我们手写调用permissionDenied方法。我们可以查看app\build\intermediates\transforms\MyJavassistTransform\debug对应的报名路径下面查看，生成的class文件如下：

```
package com.example.a60282.dpdemoapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.app.annotationprocessor.annotation.DynamicPermission;
import com.app.annotationprocessor.annotation.PermissionDenied;
import com.app.aoplibrary.annotation.CheckPermission;
import com.app.aoplibrary.aspect.CheckPermissionAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.runtime.reflect.Factory;

@DynamicPermission
public class MainActivity extends AppCompatActivity {
    public MainActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(2131296284);
        this.findViewById(2131165218).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.check();
            }
        });
    }

    @CheckPermission({"android.permission.WRITE_EXTERNAL_STORAGE"})
    private void check() {
        JoinPoint var1 = Factory.makeJP(ajc$tjp_0, this, this);
        CheckPermissionAspect var10000 = CheckPermissionAspect.aspectOf();
        Object[] var2 = new Object[]{this, var1};
        var10000.dealPoint((new MainActivity$AjcClosure1(var2)).linkClosureAndJoinPoint(69648));
    }

    @PermissionDenied({"android.permission.WRITE_EXTERNAL_STORAGE"})
    public void has() {
        Toast.makeText(this, "PermissionDenied", 1).show();
    }

    static {
        ajc$preClinit();
    }

    public void onRequestPermissionsResult(int var1, String[] var2, int[] var3) {
        super.onRequestPermissionsResult(var1, var2, var3);
        MainActivity_AutoGenerate.permissionDenied(this, var2, var3);
    }
}
```
可以看到在文件中添加了onRequestPermissionsResult方法，并且在方法里调用了permissionDenied。

这是一种情况，当如果用户已经复写了这个方法的话，就是会onRequestPermissionsResult最后一行添加调用permissionDenied的方法。

这是我们自己写的代码
```
@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this,"这是手写的，不是javassist生成的",1).show();
    }
```
这个是class文件中的代码，可以看到在最后一行调用了permissionDenied方法，

```
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, "这是手写的，不是javassist生成的", 1).show();
        Object var5 = null;
        MainActivity_AutoGenerate.permissionDenied(this, permissions, grantResults);
    }
```

[点击查看运行效果图](https://note.youdao.com/yws/public/resource/bd24ff9b39c238c024a1dd1a87a9f03d/xmlnote/5BB01C45BB5F4538A4E50B204D6DA8DF/1281)

点击下载[demo](http://note.youdao.com/yws/public/resource/bd24ff9b39c238c024a1dd1a87a9f03d/xmlnote/3490E8F6D6224AA389BA83FE421B5EA7/1286)
