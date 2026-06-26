package io.documentnode.epub4kmp.domain

/**
 * A Resource that loads its bytes only on-demand from an EPUB file.
 *
 * [zipEntryName] is the resource's full path within the archive and is used as
 * the lookup key. It is kept separate from [href], which the package-document
 * reader rewrites to be relative to the OPF.
 *
 * The data is loaded on the first call that requires it, and can be released
 * via [close].
 */
class LazyResource(
    private val resourceProvider: LazyResourceProvider,
    private val cachedSize: Long,
    private val zipEntryName: String,
    href: String = zipEntryName
) : Resource(
    null,
    null,
    href,
    MediaTypes.determineMediaType(href)
) {
    @Deprecated(
        "Pass the ZIP entry name as the lookup key; href is rewritten relative " +
            "to the OPF and no longer locates the entry.",
        ReplaceWith("LazyResource(resourceProvider, -1, href, href)")
    )
    constructor(resourceProvider: LazyResourceProvider, href: String) :
        this(resourceProvider, -1, href, href)

    override var data: ByteArray? = null
        get() = field ?: resourceProvider.getResourceBytes(zipEntryName).also { field = it }

    /** Tells this resource to release its cached data. */
    override fun close() {
        data = null
    }

    override val size: Long
        get() = if (cachedSize >= 0) cachedSize else (data?.size?.toLong() ?: 0)
}
