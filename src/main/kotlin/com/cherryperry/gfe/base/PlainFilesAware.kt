package com.cherryperry.gfe.base

import java.io.File

interface PlainFilesAware {

    val plainFiles: Iterable<File>
}
