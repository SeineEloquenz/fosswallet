package nz.eloque.foss_wallet.persistence.pass

import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import java.time.Instant

internal object AutoArchiver {
    fun shouldBeAutoArchived(
        pass: Pass,
        metadata: PassMetadata,
        now: Instant = Instant.now(),
    ): Boolean {
        if (metadata.archived) return true

        val expired = pass.expirationDate?.toInstant()?.let { expiration -> !expiration.isAfter(now) } ?: false
        return metadata.autoArchive && expired
    }
}
