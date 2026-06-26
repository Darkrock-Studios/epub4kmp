package io.documentnode.epub4kmp.util

import okio.FileHandle
import okio.FileSystem
import okio.Path

/**
 * Opens the EPUB ZIP at [zipPath] as a read-only [FileHandle] for random-access
 * reading via kmp-zip's `ZipFile`.
 *
 * JVM and native back this with okio's `openReadOnly`. wasmJs has no on-disk
 * filesystem in the browser, so its actual throws — load EPUBs there via
 * `EpubReader.readEpub(source)`, which streams through kmp-zip and works on
 * every target.
 */
internal expect fun openEpubZipHandle(fileSystem: FileSystem, zipPath: Path): FileHandle
