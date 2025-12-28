package nz.eloque.foss_wallet.persistence.tag

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import nz.eloque.foss_wallet.model.Tag

class TagRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val tagDao: TagDao
) {
    fun insert(tag: Tag) = tagDao.insert(tag)
}
