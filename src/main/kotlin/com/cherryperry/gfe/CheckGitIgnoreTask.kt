package com.cherryperry.gfe

import org.apache.tools.ant.DirectoryScanner
import org.eclipse.jgit.ignore.IgnoreNode
import org.gradle.api.GradleException
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task checks if encryption source files are covered by any of .gitignore files.
 *
 * There is problems with discovering git files via gradle API,
 * see [link](https://github.com/gradle/gradle/issues/2986) and [link](https://github.com/gradle/gradle/issues/1348).
 */
open class CheckGitIgnoreTask : BaseTask() {

    companion object {
        const val FILE_GIT_IGNORE = ".gitignore"
    }

    override val plainFiles: Iterable<File>
        @InputFiles @SkipWhenEmpty get() = super.plainFiles

    @TaskAction
    fun checkGitIgnoreFiles() {
        // we will remove every ignored plain file from this set
        val plainFiles = plainFiles.toMutableSet()
        if (plainFiles.isEmpty()) {
            logger.info("No files to check")
            return
        }
        // find all .gitignore files on project
        visitPossibleGitIgnoreFiles { gitIgnoreFileVisitDetails ->
            val gitIgnoreFile = gitIgnoreFileVisitDetails.file
            // "include" filter does not work, here goes all files in project tree
            if (gitIgnoreFile.name != FILE_GIT_IGNORE || !gitIgnoreFile.canRead()) {
                return@visitPossibleGitIgnoreFiles
            }
            logger.info("Processing ${gitIgnoreFile.absolutePath}")
            // parse each .gitignore file
            val ignoreNode = IgnoreNode()
            gitIgnoreFile.inputStream().use { stream ->
                ignoreNode.parse(stream)
            }
            logger.info("There is ${ignoreNode.rules.size} rules")
            // check each left plain file
            val removedAtLeastOne = plainFiles.removeAll { plainFile ->
                // if it is under current .gitignore scope and is ignored
                val relativeFile = plainFile.relativeToOrNull(gitIgnoreFile.parentFile)
                if (relativeFile != null) {
                    val ignored = ignoreNode.isIgnored(relativeFile.path, plainFile.isDirectory) ==
                        IgnoreNode.MatchResult.IGNORED
                    logger.info("${plainFile.absolutePath} is ignored by ${gitIgnoreFile.absolutePath}")
                    ignored
                } else {
                    false
                }
            }
            if (removedAtLeastOne && plainFiles.isEmpty()) {
                // stop visiting if all files were checked
                gitIgnoreFileVisitDetails.stopVisiting()
            }
        }
        if (plainFiles.isNotEmpty()) {
            plainFiles.forEach { plainFile ->
                logger.error("${plainFile.absolutePath} is not ignored by any $FILE_GIT_IGNORE files of project")
            }
            throw GradleException("Some plain files are not ignored by git, see log above")
        }
    }

    private fun visitPossibleGitIgnoreFiles(visitor: (FileVisitDetails) -> Unit) {
        // it is possible for DirectoryScanner be changed from multiple threads!
        synchronized(DirectoryScanner::class) {
            val current = DirectoryScanner.getDefaultExcludes()
            // remove current excludes
            current.forEach { DirectoryScanner.removeDefaultExclude(it) }
            try {
                val fileTree = project.fileTree(project.projectDir) as FileTree
                fileTree.visit { fileVisitDetails -> visitor(fileVisitDetails) }
            } finally {
                // restore them
                current.forEach { DirectoryScanner.addDefaultExclude(it) }
            }
        }
    }
}
