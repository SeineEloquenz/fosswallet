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
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationDao
import nz.eloque.foss_wallet.persistence.migrations.M14_15
import nz.eloque.foss_wallet.persistence.migrations.M_9_10
import nz.eloque.foss_wallet.persistence.pass.PassDao


@Database(
    version = 16,
    entities = [Pass::class, PassLocalization::class, PassGroup::class],
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
    ],
    exportSchema = true
)
@TypeConverters(nz.eloque.foss_wallet.persistence.TypeConverters::class)
abstract class WalletDb : RoomDatabase() {
    abstract fun passDao(): PassDao
    abstract fun localizationDao(): PassLocalizationDao

    companion object {
        @Volatile
        private var Instance: WalletDb? = null

        fun getDb(context: Context): WalletDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, WalletDb::class.java, "wallet_db")
                    .addMigrations(M_9_10)
                    .build()
                    .also { Instance = it }
            }
        }
    }

}
