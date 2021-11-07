package com.cherryperry.gfe

import com.cherryperry.gfe.base.BaseTask
import com.cherryperry.gfe.base.PlainFilesAware
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.WorkingTreeIterator
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.IOException

/**
 * Task checks if encryption source files are covered by any of .gitignore files.
 *
 * See `CGitIgnoreTest.java` for ignore file check implementation.
 */
open class CheckGitIgnoreTask : BaseTask(), PlainFilesAware {

    private val projectDir: Directory =
        project.layout.projectDirectory

    @get:[InputFiles SkipWhenEmpty]
    override val plainFiles: FileCollection =
        fileEncryptPluginExtension.plainFiles

    @get:InputFiles
    val gitIgnoreFiles: FileCollection =
        project.fileTree(mapOf("dir" to projectDir, "include" to "**/${Constants.DOT_GIT_IGNORE}"))

    @TaskAction
    open fun checkGitIgnoreFiles() {
        val git = try {
            Git.open(projectDir.asFile)
        } catch (exception: IOException) {
            throw GradleException("Git repository was not found at path $projectDir", exception)
        }
        val plainFiles = plainFiles.mapNotNull { it.toRelativeString(projectDir.asFile) }.toHashSet()
        TreeWalk(git.repository).use { treeWalk -> walkThrough(git, treeWalk, plainFiles) }
        failTaskIfAnyFilesLeft(plainFiles)
    }

    private fun walkThrough(git: Git, treeWalk: TreeWalk, plainFiles: MutableCollection<String>) {
        val fileTreeIterator = FileTreeIterator(git.repository)
        fileTreeIterator.setWalkIgnoredDirectories(true)
        treeWalk.addTree(fileTreeIterator)
        treeWalk.isRecursive = true
        while (treeWalk.next() && plainFiles.isNotEmpty()) {
            if (treeWalk.getTree(WorkingTreeIterator::class.java).isEntryIgnored) {
                val path = treeWalk.pathString
                val removed = plainFiles.remove(path)
                if (removed) {
                    logger.info("$path is ignored")
                }
            }
        }
    }

    private fun failTaskIfAnyFilesLeft(relativePlainFiles: Collection<String>) {
        if (relativePlainFiles.isNotEmpty()) {
            relativePlainFiles.forEach { plainFile ->
                logger.error("${projectDir.file(plainFile)} is not ignored by any ${Constants.DOT_GIT_IGNORE} files of project")
            }
            throw GradleException("Some plain files are not ignored by git, see log above")
        }
    }
}
