package io.documentnode.epub4kmp

import io.documentnode.epub4kmp.domain.Author
import io.documentnode.epub4kmp.domain.Book
import io.documentnode.epub4kmp.domain.Identifier
import io.documentnode.epub4kmp.domain.MediaTypes
import io.documentnode.epub4kmp.domain.Relator
import io.documentnode.epub4kmp.domain.Resource
import io.documentnode.epub4kmp.domain.Resources
import io.documentnode.epub4kmp.epub.EpubProcessorSupport
import io.documentnode.epub4kmp.epub.EpubReader
import io.documentnode.epub4kmp.epub.EpubWriter
import io.documentnode.epub4kmp.epub.NCXDocument
import io.documentnode.epub4kmp.epub.PackageDocumentReader
import io.documentnode.epub4kmp.epub.PackageDocumentWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The OPF package document is written with the OPF namespace as the default (unprefixed
 * elements, as in the EPUB 2 OPF examples), and both that spelling and the older
 * `opf:`-prefixed one read back.
 */
class PackageDocumentNamespaceTest {

    private val coverImageBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

    private fun xhtml(title: String): ByteArray =
        "<html><head><title>$title</title></head><body><p>$title</p></body></html>"
            .encodeToByteArray()

    private fun buildBook(): Book = Book().apply {
        metadata.addTitle("Namespace Book")
        metadata.addAuthor(Author("Ada", "Lovelace"))
        metadata.addIdentifier(Identifier(Identifier.Scheme.ISBN, "978-0-00-000000-0"))
        metadata.language = "en"
        coverImage = Resource("cover-image", coverImageBytes, "images/cover.png")
        coverPage = Resource("cover", xhtml("Cover"), "cover.xhtml")
        addSection("Chapter 1", Resource("ch1", xhtml("Chapter 1"), "ch1.xhtml"))
        addSection("Chapter 2", Resource("ch2", xhtml("Chapter 2"), "ch2.xhtml"))
        val ncx = NCXDocument.createNCXResource(this)
        spine.tocResource = ncx
        resources.add(ncx)
    }

    private fun writeOpf(book: Book): String {
        val sb = StringBuilder()
        val writer = EpubProcessorSupport.createXmlWriter(sb)
        try {
            PackageDocumentWriter.write(EpubWriter(), writer, book)
        } finally {
            writer.close()
        }
        return sb.toString()
    }

    /** Fresh copies of [book]'s resources, without ids, as the reader would find them in the zip. */
    private fun resourcesOf(book: Book): Resources = Resources().apply {
        for (resource in book.resources.all) {
            add(Resource(null, resource.bytes(), resource.href, resource.mediaType))
        }
    }

    private fun readOpf(opf: String, resources: Resources): Book {
        val book = Book()
        val packageResource = Resource(opf.encodeToByteArray(), "content.opf")
        PackageDocumentReader.read(packageResource, EpubReader(), book, resources)
        return book
    }

    @Test
    fun writesOpfElementsUnprefixed() {
        val opf = writeOpf(buildBook())

        assertTrue(opf.contains("<package "), opf)
        assertTrue(opf.contains("xmlns=\"http://www.idpf.org/2007/opf\""), opf)
        assertTrue(opf.contains("xmlns:opf=\"http://www.idpf.org/2007/opf\""), opf)
        for (tag in listOf("<metadata", "<meta ", "<manifest>", "<item ", "<itemref ", "<guide>", "<reference ")) {
            assertTrue(opf.contains(tag), "expected $tag in:\n$opf")
        }
        assertTrue(opf.contains("<spine toc=\"ncx\">"), opf)
        assertFalse(opf.contains("<opf:"), opf)
        assertFalse(opf.contains("</opf:"), opf)

        assertTrue(opf.contains("opf:role="), opf)
        assertTrue(opf.contains("opf:file-as="), opf)
        assertTrue(opf.contains("opf:scheme="), opf)
    }

    @Test
    fun writtenOpfReadsBack() {
        val original = buildBook()
        val readBack = readOpf(writeOpf(original), resourcesOf(original))

        assertEquals(
            setOf("toc.ncx", "images/cover.png", "cover.xhtml", "ch1.xhtml", "ch2.xhtml"),
            readBack.resources.allHrefs.toSet()
        )
        assertEquals("ch1", readBack.resources.getByHref("ch1.xhtml")?.id)
        assertEquals(MediaTypes.PNG, readBack.resources.getByHref("images/cover.png")?.mediaType)

        assertEquals(
            listOf("cover.xhtml", "ch1.xhtml", "ch2.xhtml"),
            (0 until readBack.spine.size).map { readBack.spine.getResource(it)?.href }
        )
        assertFalse(readBack.spine.getSpineReferences()[0].linear, "cover page is non-linear")
        assertEquals("toc.ncx", readBack.spine.tocResource?.href)

        assertEquals("images/cover.png", readBack.coverImage?.href)
        assertEquals("cover.xhtml", readBack.coverPage?.href)

        assertEquals(listOf("Namespace Book"), readBack.metadata.getTitles())
        val author = readBack.metadata.getAuthors().single()
        assertEquals("Ada", author.firstname)
        assertEquals("Lovelace", author.lastname)
        assertEquals(Relator.AUTHOR, author.relator)

        val identifier = readBack.metadata.getIdentifiers().single()
        assertEquals(Identifier.Scheme.ISBN, identifier.scheme)
        assertEquals("978-0-00-000000-0", identifier.value)
        assertTrue(identifier.isBookId, "unique-identifier marks the book id")
    }

    @Test
    fun readsLegacyPrefixedOpf() {
        val opf = """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:opf="http://www.idpf.org/2007/opf" version="2.0" unique-identifier="BookId">
              <opf:metadata>
                <dc:identifier id="BookId" opf:scheme="UUID">legacy-id</dc:identifier>
                <dc:title>Legacy Book</dc:title>
                <dc:creator opf:role="aut" opf:file-as="Lovelace, Ada">Ada Lovelace</dc:creator>
                <dc:language>en</dc:language>
                <opf:meta name="cover" content="cover-image" />
                <opf:meta name="generator" content="EPUB4KMP v" />
              </opf:metadata>
              <opf:manifest>
                <opf:item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml" />
                <opf:item id="cover-image" href="cover.png" media-type="image/png" />
                <opf:item id="ch1" href="ch1.xhtml" media-type="application/xhtml+xml" />
                <opf:item id="ch2" href="ch2.xhtml" media-type="application/xhtml+xml" />
              </opf:manifest>
              <opf:spine toc="ncx">
                <opf:itemref idref="ch2" />
                <opf:itemref idref="ch1" linear="no" />
              </opf:spine>
              <opf:guide>
                <opf:reference type="text" href="ch1.xhtml" title="Start" />
              </opf:guide>
            </package>
        """.trimIndent()

        val resources = Resources().apply {
            add(Resource("<ncx/>".encodeToByteArray(), "toc.ncx"))
            add(Resource(coverImageBytes, "cover.png"))
            add(Resource(xhtml("Chapter 1"), "ch1.xhtml"))
            add(Resource(xhtml("Chapter 2"), "ch2.xhtml"))
        }
        val book = readOpf(opf, resources)

        assertEquals(
            setOf("toc.ncx", "cover.png", "ch1.xhtml", "ch2.xhtml"),
            book.resources.allHrefs.toSet()
        )
        assertEquals("ch2", book.resources.getByHref("ch2.xhtml")?.id)
        assertEquals(
            listOf("ch2.xhtml", "ch1.xhtml"),
            (0 until book.spine.size).map { book.spine.getResource(it)?.href }
        )
        assertFalse(book.spine.getSpineReferences()[1].linear)
        assertEquals("toc.ncx", book.spine.tocResource?.href)
        assertEquals("cover.png", book.coverImage?.href)

        val guideReference = book.guide.getReferences().single()
        assertEquals("text", guideReference.type)
        assertEquals("ch1.xhtml", guideReference.resource?.href)

        assertEquals(listOf("Legacy Book"), book.metadata.getTitles())
        val author = book.metadata.getAuthors().single()
        assertEquals("Ada", author.firstname)
        assertEquals("Lovelace", author.lastname)
        assertEquals(Relator.AUTHOR, author.relator)
        val identifier = book.metadata.getIdentifiers().single()
        assertEquals(Identifier.Scheme.UUID, identifier.scheme)
        assertTrue(identifier.isBookId)
        assertEquals("cover-image", book.metadata.getMetaAttribute("cover"))
    }

    @Test
    fun generatorCarriesLibraryVersion() {
        val versioned = Regex("EPUB4KMP v\\d+\\.\\d+\\.\\d+\\S*")
        assertTrue(versioned.matches(Constants.EPUB4KMP_GENERATOR_NAME), Constants.EPUB4KMP_GENERATOR_NAME)

        val opf = writeOpf(buildBook())
        val generator = Regex("<meta name=\"generator\" content=\"([^\"]*)\"").find(opf)
        assertNotNull(generator, opf)
        assertTrue(versioned.matches(generator.groupValues[1]), generator.groupValues[1])
    }
}
