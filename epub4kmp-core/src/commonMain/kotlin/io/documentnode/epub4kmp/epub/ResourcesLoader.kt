package io.documentnode.epub4kmp.epub

import io.documentnode.epub4kmp.domain.*
import io.documentnode.epub4kmp.util.ResourceUtil
import io.documentnode.epub4kmp.util.openEpubZipHandle
import io.documentnode.epub4kmp.util.readEntryBytes
import no.synth.kmpzip.okio.asInputStream
import no.synth.kmpzip.okio.asSeekableSource
import no.synth.kmpzip.zip.ZipFile
import no.synth.kmpzip.zip.ZipInputStream
import okio.*

/**
 * Loads [Resources] out of EPUB archives.
 */
object ResourcesLoader {

    /**
     * Loads all entries from the given [Source] (a streaming ZIP).
     *
     * Reads everything into memory; cheap to call but uses memory proportional
     * to the EPUB's content size.
     */
    fun loadResources(
        source: Source,
        defaultHtmlEncoding: String
    ): Resources {
        val resources = Resources()
        ZipInputStream(source.buffer().asInputStream()).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                if (entry.isDirectory) continue
                val bytes = zis.readBytes()
                val resource = ResourceUtil.createResource(entry.name, bytes)
                if (resource.mediaType == MediaTypes.XHTML) {
                    resource.inputEncoding = defaultHtmlEncoding
                }
                resources.add(resource)
            }
        }
        return resources
    }

    /**
     * Loads entries from the ZIP at [zipPath] in [fileSystem].
     *
     * Resources with a [MediaType] in [lazyLoadedTypes] are returned as
     * [LazyResource] instances that read their bytes from the ZIP on demand.
     */
    fun loadResources(
        fileSystem: FileSystem,
        zipPath: Path,
        defaultHtmlEncoding: String,
        lazyLoadedTypes: List<MediaType> = emptyList()
    ): Resources {
        val provider = EpubResourceProvider(fileSystem, zipPath)
        val resources = Resources()

        val handle = openEpubZipHandle(fileSystem, zipPath)
        try {
            val zip = ZipFile(handle.asSeekableSource())
            try {
                for (entry in zip.entries) {
                    if (entry.isDirectory) continue
                    val href = entry.name.trimStart('/')
                    if (href.isEmpty()) continue
                    val resource: Resource = if (shouldLoadLazy(href, lazyLoadedTypes)) {
                        LazyResource(provider, entry.size, entry.name, href)
                    } else {
                        ResourceUtil.createResource(href, zip.readEntryBytes(entry))
                    }
                    if (resource.mediaType == MediaTypes.XHTML) {
                        resource.inputEncoding = defaultHtmlEncoding
                    }
                    resources.add(resource)
                }
            } finally {
                zip.close()
            }
        } finally {
            handle.close()
        }
        return resources
    }

    private fun shouldLoadLazy(
        href: String,
        lazilyLoadedMediaTypes: List<MediaType>
    ): Boolean {
        if (lazilyLoadedMediaTypes.isEmpty()) return false
        return lazilyLoadedMediaTypes.contains(MediaTypes.determineMediaType(href))
    }
}
