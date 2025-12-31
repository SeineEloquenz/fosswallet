package nz.eloque.foss_wallet.persistence.tag

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map
import nz.eloque.foss_wallet.model.Tag

class TagRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tagDao: TagDao
) {
    fun all() = tagDao.all().map { it.toSet() }
    suspend fun insert(tag: Tag) = tagDao.insert(tag)

    suspend fun remove(tag: Tag) = tagDao.remove(tag)
}
