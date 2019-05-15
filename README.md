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
