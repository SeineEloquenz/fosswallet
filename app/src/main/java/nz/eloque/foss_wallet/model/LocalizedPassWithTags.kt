package nz.eloque.foss_wallet.model

data class LocalizedPassWithTags(
    val pass: Pass,
    val tags: Set<Tag>,
)