package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassGroup
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.model.PassTagCrossRef
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationDao
import nz.eloque.foss_wallet.persistence.migrations.M14_15
import nz.eloque.foss_wallet.persistence.migrations.M20_21
import nz.eloque.foss_wallet.persistence.migrations.M_17_18
import nz.eloque.foss_wallet.persistence.migrations.M_18_19
import nz.eloque.foss_wallet.persistence.migrations.M_19_20
import nz.eloque.foss_wallet.persistence.migrations.M_9_10
import nz.eloque.foss_wallet.persistence.pass.PassDao
import nz.eloque.foss_wallet.persistence.tag.TagDao

fun buildDb(context: Context) = Room.databaseBuilder(context, WalletDb::class.java, "wallet_db")
        .addMigrations(M_9_10)
        .addMigrations(M_17_18)
        .addMigrations(M_18_19)
        .addMigrations(M_19_20)
        .build()

@Database(
    version = 24,
    entities = [Pass::class, PassLocalization::class, PassGroup::class, Tag::class, PassTagCrossRef::class],
    autoMigrations = [
        AutoMigration (from = 4, to = 5),
        AutoMigration (from = 5, to = 6),
        AutoMigration (from = 6, to = 7),
        AutoMigration (from = 7, to = 8),
        AutoMigration (from = 8, to = 9),
        AutoMigration (from = 10, to = 11),
        AutoMigration (from = 11, to = 12),
        AutoMigration (from = 12, to = 13),
        AutoMigration (from = 13, to = 14),
        AutoMigration (from = 14, to = 15, spec = M14_15::class),
        AutoMigration (from = 15, to = 16),
        AutoMigration (from = 16, to = 17),
        AutoMigration (from = 20, to = 21, spec = M20_21::class),
        AutoMigration (from = 21, to = 22),
        AutoMigration (from = 22, to = 23),
        AutoMigration (from = 23, to = 24),
    ],
    exportSchema = true
)
@TypeConverters(nz.eloque.foss_wallet.persistence.TypeConverters::class)
abstract class WalletDb : RoomDatabase() {
    abstract fun passDao(): PassDao
    abstract fun localizationDao(): PassLocalizationDao
    abstract fun tagDao(): TagDao
}
