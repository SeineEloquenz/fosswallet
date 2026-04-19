package nz.eloque.foss_wallet.model

data class LocalizedPassWithTags(
    val pass: Pass,
    val metadata: PassMetadata,
    val tags: Set<Tag>,
) {
    companion object {
        fun placeholder(): LocalizedPassWithTags {
            val pass = Pass.placeholder()
            return LocalizedPassWithTags(pass, PassMetadata(pass.id), setOf())
        }
    }
}
