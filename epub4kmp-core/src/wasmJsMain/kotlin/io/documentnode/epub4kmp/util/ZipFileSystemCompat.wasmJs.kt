package io.documentnode.epub4kmp.util

import okio.FileHandle
import okio.FileSystem
import okio.Path

internal actual fun openEpubZipHandle(fileSystem: FileSystem, zipPath: Path): FileHandle =
    throw UnsupportedOperationException(
        "Loading an EPUB from a file path is not supported on wasmJs. " +
            "Read from an okio Source via EpubReader.readEpub(source) instead."
    )
