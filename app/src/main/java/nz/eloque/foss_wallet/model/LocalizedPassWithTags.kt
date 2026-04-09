package nz.eloque.foss_wallet.model

data class LocalizedPassWithTags(
    val pass: Pass,
    val tags: Set<Tag>,
) {
    companion object {
        fun placeholder(): LocalizedPassWithTags = LocalizedPassWithTags(Pass.placeholder(), setOf())
    }
}
