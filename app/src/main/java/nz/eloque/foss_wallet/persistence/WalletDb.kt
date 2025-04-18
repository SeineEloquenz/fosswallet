package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassLocalization
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationDao
import nz.eloque.foss_wallet.persistence.pass.PassDao


@Database(
    version = 5,
    entities = [Pass::class, PassLocalization::class],
    autoMigrations = [
        AutoMigration (from = 4, to = 5)
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
                    .build()
                    .also { Instance = it }
            }
        }
    }

}
