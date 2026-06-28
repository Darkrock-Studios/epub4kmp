package io.documentnode.epub4kmp.domain

/**
 * Base class holding a reference to a [Resource].
 */
abstract class ResourceReference(
    /**
     * The referenced [Resource], or null if none is set.
     */
    open var resource: Resource?
) {
    val resourceId: String?
        /**
         * The id of the referenced [Resource], or null if there is no resource.
         */
        get() = resource?.id
}
