package nz.eloque.foss_wallet.persistence.localization

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.PassLocalization

class OfflinePassLocalizationRepository @Inject constructor(
    private val localizationDao: PassLocalizationDao
) : PassLocalizationRepository {

    override fun all(): Flow<List<PassLocalization>> = localizationDao.all()

    override suspend fun byPassId(passId: Int, lang: String): Set<PassLocalization> = localizationDao.byPassId(passId, lang).toSet()

    override suspend fun insert(localization: PassLocalization): Long {
        return localizationDao.insert(localization)
    }
}
