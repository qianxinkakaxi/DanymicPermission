package com.app.plugin.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;


public class JARCompress {

    public static void doIt(String dir, String jarName) throws Exception {
        File folderObject = new File(dir);
        if (folderObject.exists()) {
            List fileList = getSubFiles(new File(dir));

            FileOutputStream fos = new FileOutputStream(jarName);
            JarOutputStream zos = new JarOutputStream(fos);
            JarEntry ze = null;
            byte[] buf = new byte[1024];
            int readLen = 0;
            for (int i = 0; i < fileList.size(); i++) {
                File f = (File) fileList.get(i);
                ze = new JarEntry(getAbsFileName(dir, f));
                ze.setSize(f.length());
                ze.setTime(f.lastModified());

                zos.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                while ((readLen = is.read(buf, 0, 1024)) != -1) {
                    zos.write(buf, 0, readLen);
                }
                is.close();
            }
            zos.close();
        } else {
            throw new Exception("文件夹不存在!");
        }
    }


    private static List<File> getSubFiles(File baseDir) {
        List<File> fileList = new ArrayList<>();
        File[] tmp = baseDir.listFiles();
        if (tmp != null) {
            for (File tempFile : tmp) {
                if (tempFile.isFile()) {
                    fileList.add(tempFile);
                } else {
                    fileList.addAll(getSubFiles(tempFile));
                }
            }
        }
        return fileList;
    }


    private static String getAbsFileName(String baseDir, File realFileName) {
        File real = realFileName;
        File base = new File(baseDir);
        String ret = real.getName();
        while (true) {
            real = real.getParentFile();
            if (real == null) {
                break;
            }
            if (real.equals(base)) {
                break;
            } else {
                ret = real.getName() + "/" + ret;
            }
        }
        return ret;
    }
}
