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
        if (!metadata.autoArchive) return false

        val expiration = pass.expirationDate?.toInstant() ?: return false
        val expired = !expiration.isAfter(now)
        val expiredWhileHeld = pass.addedAt.isBefore(expiration)
        return expired && expiredWhileHeld
    }
}
