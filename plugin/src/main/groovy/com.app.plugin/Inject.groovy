package com.app.plugin

import com.google.common.io.ByteStreams
import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.Modifier
import javassist.NotFoundException
import org.apache.commons.io.FileUtils

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * <br> ClassName:   ${className}* <br> Description:
 * <br>
 * <br> @author:      谢文良
 * <br> Date:        2018/6/29 10:57
 */
class Inject {
    private static Map<String, Class> map = new HashMap<>()

    private static Class getAnnotationClass(String className, ClassPool mClassPool) {
        if (!map.containsKey(className)) {
            CtClass mCtClass = mClassPool.getCtClass(className)
            if (mCtClass.isFrozen()) {
                mCtClass.defrost()
            }
            map.put(className, mCtClass.toClass())
            mCtClass.detach()
        }
        return map.get(className)
    }

    /**
     * 遍历该目录下的所有class，对所有class进行代码注入。
     * 其中以下class是不需要注入代码的：
     * --- 1. R文件相关
     * --- 2. 配置文件相关（BuildConfig）
     * --- 3. Application
     * @param path 目录的路径
     */
    static void injectDir(String inputPath, String outPutPath, ClassPool mClassPool) {
        File dir = new File(inputPath)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
                if (file.isFile()) {
                    File outPutFile = new File(outPutPath + filePath.substring(inputPath.length()))
                    Files.createParentDirs(outPutFile)
                    if (filePath.endsWith(".class")
                            && !filePath.contains('R$')
                            && !filePath.contains('R.class')
                            && !filePath.contains("BuildConfig.class")) {
                        FileInputStream inputStream = new FileInputStream(file)
                        FileOutputStream outputStream = new FileOutputStream(outPutFile)
                        transform(inputStream, outputStream, mClassPool)
                    } else {
                        FileUtils.copyFile(file, outPutFile)
                    }
                }
            }
        }
    }

    static void injectJar(String jarInPath, String jarOutPath, ClassPool mClassPool) throws IOException {
        ArrayList entries = new ArrayList()
        Files.createParentDirs(new File(jarOutPath))
        FileInputStream fis = null
        ZipInputStream zis = null
        FileOutputStream fos = null
        ZipOutputStream zos = null
        try {
            fis = new FileInputStream(new File(jarInPath))
            zis = new ZipInputStream(fis)
            fos = new FileOutputStream(new File(jarOutPath))
            zos = new ZipOutputStream(fos)
            ZipEntry entry = zis.getNextEntry()
            while (entry != null) {
                String fileName = entry.getName()
                if (!entries.contains(fileName)) {
                    entries.add(fileName)
                    zos.putNextEntry(new ZipEntry(fileName))
                    if (!entry.isDirectory() && fileName.endsWith(".class")
                            && !fileName.contains('R$')
                            && !fileName.contains('R.class')
                            && !fileName.contains("BuildConfig.class"))
                        transform(zis, zos, mClassPool)
                    else {
                        ByteStreams.copy(zis, zos)
                    }
                }
                entry = zis.getNextEntry()
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            if (zos != null)
                zos.close()
            if (fos != null)
                fos.close()
            if (zis != null)
                zis.close()
            if (fis != null)
                fis.close()
        }
    }


    static void transform(InputStream input, OutputStream out, ClassPool mClassPool) {
        try {
            CtClass c = mClassPool.makeClass(input)
            play(c, mClassPool)
            out.write(c.toBytecode())
            c.detach()
        } catch (Exception e) {
            e.printStackTrace()
            input.close()
            out.close()
            throw new RuntimeException(e.getMessage())
        }
    }

    private static void play(CtClass c, ClassPool mClassPool) {

        if (c.isFrozen()) {
            c.defrost()
        }
        Class a = getAnnotationClass("com.app.annotationprocessor.annotation.DynamicPermission", mClassPool)
        Object object = c.getAnnotation(a)
        if (object != null) {
            CtMethod m = null
            try {

                m = c.getDeclaredMethod("onRequestPermissionsResult");
            } catch (NotFoundException e) {

            }

            if (m != null) {
                m.insertAfter(c.getName() + "_AutoGenerate.permissionDenied(\$0, \$2,\$3);");
            } else {
                try {
                    CtMethod ctMethod = CtNewMethod.make("public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {}", c);
                    ctMethod.setBody("{super.onRequestPermissionsResult(\$1,  \$2,\$3);\n" +
                            "        " + c.getName() + "_AutoGenerate.permissionDenied(\$0, \$2,\$3);}")
                    c.addMethod(ctMethod);
                } catch (NotFoundException e) {

                }
            }
        }
    }


    private static boolean checkMethod(int modifiers) {
        return !Modifier.isStatic(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers) && !Modifier.isEnum(modifiers) && !Modifier.isInterface(modifiers)
    }


}
