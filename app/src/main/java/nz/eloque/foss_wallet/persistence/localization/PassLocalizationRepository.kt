package nz.eloque.foss_wallet.persistence.localization

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.PassLocalization

class PassLocalizationRepository @Inject constructor(
    private val localizationDao: PassLocalizationDao
) {

    fun all(): Flow<List<PassLocalization>> = localizationDao.all()

    fun byPassId(passId: Int, lang: String): Set<PassLocalization> = localizationDao.byPassId(passId, lang).toSet()

    fun insert(localization: PassLocalization): Long {
        return localizationDao.insert(localization)
    }
}
