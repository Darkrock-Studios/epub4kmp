package io.documentnode.epub4kmp.util

import no.synth.kmpzip.okio.asSource
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipFile
import okio.buffer

/**
 * Reads a single [entry]'s full uncompressed bytes from this [ZipFile].
 *
 * okio's `Source` is not `AutoCloseable` on native, so the stream is closed
 * via `try`/`finally` rather than `use`.
 */
internal fun ZipFile.readEntryBytes(entry: ZipEntry): ByteArray {
    val source = getInputStream(entry).asSource().buffer()
    return try {
        source.readByteArray()
    } finally {
        source.close()
    }
}
