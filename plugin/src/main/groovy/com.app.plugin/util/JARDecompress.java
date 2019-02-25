package com.app.plugin.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class JARDecompress {
    /**
     * <br> Description: 压缩文件
     * <br> Author:      xwl
     * <br> Date:        2018/7/25 16:28
     *
     * @param jarFileName jarFileName
     * @param outputPath  outputPath
     */
    public static void doIt(String jarFileName, String outputPath) {
        try {
            // 执行解压
            decompress(jarFileName, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void decompress(String fileName, String outputPath)
            throws IOException {

        if (!outputPath.endsWith(File.separator)) {
            outputPath += File.separator;
        }

        JarFile jf = new JarFile(fileName);

        for (Enumeration e = jf.entries(); e.hasMoreElements(); ) {
            JarEntry je = (JarEntry) e.nextElement();
            String outFileName = outputPath + je.getName();
            File f = new File(outFileName);

            createFatherDir(outFileName);

            if (f.isDirectory()) {
                continue;
            }
            InputStream in = null;
            OutputStream out = null;
            try {
                in = jf.getInputStream(je);
                FileOutputStream fos = new FileOutputStream(f);
                out = new BufferedOutputStream(fos);
                byte[] buffer = new byte[2048];
                int nBytes = 0;
                while ((nBytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, nBytes);
                }
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                try {
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
                } catch (IOException ioe) {
                    throw ioe;
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        }

    }


    private static void createFatherDir(String outFileName) {

        Pattern p = Pattern.compile("[/\\" + File.separator + "]");
        Matcher m = p.matcher(outFileName);

        while (m.find()) {
            int index = m.start();
            String subDir = outFileName.substring(0, index);
            File subDirFile = new File(subDir);
            if (!subDirFile.exists()) {
                subDirFile.mkdir();
            }
        }
    }
}
