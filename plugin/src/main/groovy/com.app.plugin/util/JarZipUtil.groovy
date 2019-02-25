package com.app.plugin.util



class JarZipUtil {
    static void deleteFile(File file) {
        if (file.isDirectory()) {
            file.listFiles().each {
                if (it.isDirectory()) {
                    deleteFile(it)
                } else {
                    it.delete()
                }
            }
            file.delete()
        } else {
            file.delete()
        }
    }

}
