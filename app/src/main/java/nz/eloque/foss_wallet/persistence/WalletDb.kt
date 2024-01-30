package nz.eloque.foss_wallet.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nz.eloque.foss_wallet.model.Pass

@Database(entities = [Pass::class], version = 1)
@TypeConverters(nz.eloque.foss_wallet.model.TypeConverters::class)
abstract class WalletDb : RoomDatabase() {
    abstract fun passDao(): PassDao

    companion object {
        @Volatile
        private var Instance: WalletDb? = null

        fun getDb(context: Context): WalletDb {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, WalletDb::class.java, "wallet_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }

}
