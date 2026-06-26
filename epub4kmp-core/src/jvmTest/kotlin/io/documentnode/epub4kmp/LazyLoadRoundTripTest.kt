package io.documentnode.epub4kmp

import io.documentnode.epub4kmp.domain.Author
import io.documentnode.epub4kmp.domain.Book
import io.documentnode.epub4kmp.domain.LazyResource
import io.documentnode.epub4kmp.domain.MediaTypes
import io.documentnode.epub4kmp.domain.Resource
import io.documentnode.epub4kmp.epub.EpubReader
import io.documentnode.epub4kmp.epub.EpubWriter
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LazyLoadRoundTripTest {

    /**
     * Writes an EPUB to a file, reads it back via the random-access file path
     * with XHTML lazy-loaded, and asserts the lazy stub resolves to the right
     * bytes through the ZIP central directory.
     */
    @Test
    fun lazyLoadFromFileRoundTrips() {
        val chapterHtml = """
            <html><head><title>Chapter 1</title></head>
            <body><h1>Hello, world!</h1><p>From a lazy-load test.</p></body></html>
        """.trimIndent().encodeToByteArray()

        val original = Book().apply {
            metadata.addTitle("Lazy Book")
            metadata.addAuthor(Author("Ada", "Lovelace"))
            metadata.language = "en"
            val ch = Resource("ch1", chapterHtml, "ch1.xhtml")
            ch.mediaType = MediaTypes.XHTML
            addSection("Chapter 1", ch)
        }

        val fs = FakeFileSystem()
        val zipPath = "/book.epub".toPath()
        fs.sink(zipPath).use { sink ->
            EpubWriter().write(original, sink)
        }

        val readBack = EpubReader().readEpub(
            fileSystem = fs,
            zipPath = zipPath,
            lazyLoadedTypes = listOf(MediaTypes.XHTML)
        )

        val chapter = readBack.resources.getByHref("ch1.xhtml")
        assertNotNull(chapter, "chapter resource is present")
        assertTrue(chapter is LazyResource, "XHTML resource is loaded lazily")
        assertEquals(chapterHtml.size.toLong(), chapter.size, "size comes from the central directory")
        assertEquals(
            chapterHtml.decodeToString(),
            chapter.bytes().decodeToString(),
            "lazy bytes resolve via random-access ZipFile read"
        )

        assertEquals("Lazy Book", readBack.metadata.getTitles().firstOrNull())
    }
}
