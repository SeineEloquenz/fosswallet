package nz.eloque.foss_wallet.persistence.pass

import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import java.time.Instant

internal object AutoArchiver {
    fun shouldBeAutoArchived(
        pass: Pass,
        metadata: PassMetadata,
        previous: Pass? = null,
        now: Instant = Instant.now(),
    ): Boolean {
        if (metadata.archived) return true
        if (!metadata.autoArchive) return false
        if (pass.voided && previous?.voided == false) return true

        val expiration = pass.expirationDate?.toInstant() ?: return false
        val expired = !expiration.isAfter(now)
        val expiredWhileHeld = pass.addedAt.isBefore(expiration)
        return expired && expiredWhileHeld
    }
}
