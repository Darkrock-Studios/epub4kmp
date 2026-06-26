package io.documentnode.epub4kmp.domain

import io.documentnode.epub4kmp.util.openEpubZipHandle
import io.documentnode.epub4kmp.util.readEntryBytes
import no.synth.kmpzip.okio.asSeekableSource
import no.synth.kmpzip.zip.ZipFile
import okio.FileSystem
import okio.Path

/**
 * Lazily reads resources from an EPUB on disk.
 *
 * Each call to [getResourceBytes] opens the ZIP and seeks straight to the
 * requested entry via its central directory, without streaming the archive.
 */
class EpubResourceProvider(
    private val fileSystem: FileSystem,
    private val zipPath: Path
) : LazyResourceProvider {
    override fun getResourceBytes(href: String): ByteArray {
        val handle = openEpubZipHandle(fileSystem, zipPath)
        try {
            val zip = ZipFile(handle.asSeekableSource())
            try {
                val entry = zip.getEntry(href) ?: error("Missing entry: $href")
                return zip.readEntryBytes(entry)
            } finally {
                zip.close()
            }
        } finally {
            handle.close()
        }
    }
}
