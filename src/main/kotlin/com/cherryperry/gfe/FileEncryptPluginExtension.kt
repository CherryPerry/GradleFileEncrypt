package com.cherryperry.gfe

import org.gradle.api.Project
import java.io.File

open class FileEncryptPluginExtension(
    project: Project
) {

    open val files = project.objects.listProperty(File::class.java)!!
}
