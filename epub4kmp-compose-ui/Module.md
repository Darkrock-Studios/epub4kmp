# Module epub4kmp-compose-ui

Compose Multiplatform UI for rendering EPUBs loaded with **epub4kmp-core**. Ships a
batteries-included reader screen plus building-block composables you can arrange
yourself. Chapters render in a platform WebView with images, CSS, and fonts inlined.

> **Try it live:** [the EPUB reader running on Wasm »](https://darkrock-studios.github.io/epub4kmp/)

```kotlin
implementation("com.darkrockstudios:epub4kmp-compose-ui:0.2.0")
```

## Drop-in reader

Load a [Book][io.documentnode.epub4kmp.domain.Book] with `epub4kmp-core`, then hand it to
[EpubReader][io.documentnode.epub4kmp.compose.EpubReader] — it wires up a table-of-contents
sidebar, prev/next controls, and chapter rendering for you:

```kotlin
@Composable
fun ReaderScreen(book: Book) {
    EpubReader(
        book = book,
        modifier = Modifier.fillMaxSize(),
    )
}
```

## Building your own layout

Drive navigation with [EpubReaderState][io.documentnode.epub4kmp.compose.EpubReaderState]
(via [rememberEpubReaderState][io.documentnode.epub4kmp.compose.rememberEpubReaderState])
and compose the pieces yourself —
[TableOfContents][io.documentnode.epub4kmp.compose.TableOfContents],
[EpubContent][io.documentnode.epub4kmp.compose.EpubContent],
[CoverImage][io.documentnode.epub4kmp.compose.CoverImage], and
[MetadataCard][io.documentnode.epub4kmp.compose.MetadataCard]:

```kotlin
@Composable
fun TwoPaneReader(book: Book) {
    val state = rememberEpubReaderState(book)

    Row(Modifier.fillMaxSize()) {
        TableOfContents(
            book = book,
            onSelect = { ref -> state.goto(ref) },
            modifier = Modifier.width(280.dp),
        )
        state.currentResource?.let { chapter ->
            EpubContent(
                book = book,
                resource = chapter,
                fragmentId = state.currentFragmentId,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
```

## Handling link taps

Pass `onLinkClicked` to [EpubContent][io.documentnode.epub4kmp.compose.EpubContent] to
intercept `<a>` taps. A [LinkClick][io.documentnode.epub4kmp.compose.LinkClick] resolves
in-book links to a [Resource][io.documentnode.epub4kmp.domain.Resource] (with optional
fragment) so you can navigate, while external links carry just their href:

```kotlin
EpubContent(
    book = book,
    resource = chapter,
    onLinkClicked = { click ->
        click.resource?.let { state.gotoResource(it, click.fragmentId) }
    },
)
```

# Package io.documentnode.epub4kmp.compose

The reader UI: the all-in-one [EpubReader][io.documentnode.epub4kmp.compose.EpubReader]
and its [EpubReaderState][io.documentnode.epub4kmp.compose.EpubReaderState], plus the
building blocks ([EpubContent][io.documentnode.epub4kmp.compose.EpubContent],
[TableOfContents][io.documentnode.epub4kmp.compose.TableOfContents],
[CoverImage][io.documentnode.epub4kmp.compose.CoverImage],
[MetadataCard][io.documentnode.epub4kmp.compose.MetadataCard]) and the
[LinkClick][io.documentnode.epub4kmp.compose.LinkClick] model for handling link taps.
