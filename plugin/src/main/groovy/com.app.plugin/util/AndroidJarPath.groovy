package com.app.plugin.util

import org.gradle.api.Project


class AndroidJarPath {

    static String getPath(Project project){
        return project.android.bootClasspath[0].toString()
    }
}
