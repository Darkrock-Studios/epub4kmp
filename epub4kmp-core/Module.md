# Module epub4kmp-core

A Kotlin Multiplatform library for reading, writing, and manipulating EPUB files — a
KMP fork of [epub4j](https://github.com/documentnode/epub4j) with all JVM-only code
replaced by multiplatform equivalents (okio, xmlutil, kotlinx-datetime). Runs on JVM,
Android, iOS, macOS, Linux, and Windows.

> **Try it live:** [the EPUB reader running on Wasm »](https://darkrock-studios.github.io/epub4kmp/)

```kotlin
implementation("com.darkrockstudios:epub4kmp-core:0.2.0")
```

For drop-in Compose reader UI on top of this, see the **epub4kmp-compose-ui** module.

## Reading an EPUB

Create an [EpubReader][io.documentnode.epub4kmp.epub.EpubReader] and read from an okio
`Path` (or any `Source`). The result is a [Book][io.documentnode.epub4kmp.domain.Book]:

```kotlin
import io.documentnode.epub4kmp.epub.EpubReader
import okio.FileSystem
import okio.Path.Companion.toPath

val book = EpubReader().readEpub(FileSystem.SYSTEM, "book.epub".toPath())

println(book.title)
book.metadata.authors.forEach { println("${it.firstname} ${it.lastname}") }
```

For large books, keep heavy resources out of memory by lazy-loading them from the ZIP
on demand — pass the [MediaTypes][io.documentnode.epub4kmp.domain.MediaTypes] you want
deferred:

```kotlin
val book = EpubReader().readEpub(
    fileSystem = FileSystem.SYSTEM,
    zipPath = "book.epub".toPath(),
    lazyLoadedTypes = listOf(MediaTypes.JPG, MediaTypes.PNG),
)
```

## Inspecting the book

A [Book][io.documentnode.epub4kmp.domain.Book] exposes the EPUB's structure:

```kotlin
book.spine.getSpineReferences()           // reading order
book.tableOfContents.getTocReferences()   // navigation tree
book.coverImage                           // cover Resource, if any

val html = book.spine.getResource(0)?.asString()   // first chapter's XHTML
```

## Writing / creating an EPUB

Build or modify a [Book][io.documentnode.epub4kmp.domain.Book] and write it with
[EpubWriter][io.documentnode.epub4kmp.epub.EpubWriter]:

```kotlin
import io.documentnode.epub4kmp.epub.EpubWriter
import io.documentnode.epub4kmp.util.ResourceUtil
import okio.buffer

book.addSection("Afterword", ResourceUtil.createResource("Afterword", "afterword.html"))

FileSystem.SYSTEM.sink("out.epub".toPath()).buffer().use { sink ->
    EpubWriter().write(book, sink)
}
```

Hook the read/write pipeline with a
[BookProcessor][io.documentnode.epub4kmp.epub.BookProcessor] when you need to transform
content (the default writer uses one to auto-link stylesheets).

# Package io.documentnode.epub4kmp.epub

The read/write entry points:
[EpubReader][io.documentnode.epub4kmp.epub.EpubReader],
[EpubWriter][io.documentnode.epub4kmp.epub.EpubWriter], and the
[BookProcessor][io.documentnode.epub4kmp.epub.BookProcessor] pipeline for transforming a
[Book][io.documentnode.epub4kmp.domain.Book] during reading or writing.

# Package io.documentnode.epub4kmp.domain

The EPUB object model: [Book][io.documentnode.epub4kmp.domain.Book] and everything it
holds — [Metadata][io.documentnode.epub4kmp.domain.Metadata],
[Resources][io.documentnode.epub4kmp.domain.Resources] /
[Resource][io.documentnode.epub4kmp.domain.Resource],
[Spine][io.documentnode.epub4kmp.domain.Spine],
[TableOfContents][io.documentnode.epub4kmp.domain.TableOfContents],
[Guide][io.documentnode.epub4kmp.domain.Guide], plus the
[MediaTypes][io.documentnode.epub4kmp.domain.MediaTypes] registry and a
[stylesheet][io.documentnode.epub4kmp.domain.StylesheetBuilder] DSL.

# Package io.documentnode.epub4kmp.browsersupport

Helpers for building a reader UI: [Navigator][io.documentnode.epub4kmp.browsersupport.Navigator]
tracks the current position in a book and emits
[NavigationEvent][io.documentnode.epub4kmp.browsersupport.NavigationEvent]s, with
[NavigationHistory][io.documentnode.epub4kmp.browsersupport.NavigationHistory] for
back/forward.

# Package io.documentnode.epub4kmp.util

Small public helpers, most notably
[ResourceUtil][io.documentnode.epub4kmp.util.ResourceUtil] for creating
[Resource][io.documentnode.epub4kmp.domain.Resource]s and parsing them as XML.
