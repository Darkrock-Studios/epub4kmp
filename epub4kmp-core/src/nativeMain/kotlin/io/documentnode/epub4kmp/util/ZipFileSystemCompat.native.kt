package io.documentnode.epub4kmp.util

import okio.FileHandle
import okio.FileSystem
import okio.Path

internal actual fun openEpubZipHandle(fileSystem: FileSystem, zipPath: Path): FileHandle =
    fileSystem.openReadOnly(zipPath)
