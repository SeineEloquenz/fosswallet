package nz.eloque.foss_wallet.app

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import nz.eloque.foss_wallet.persistence.TransactionalExecutor
import nz.eloque.foss_wallet.persistence.WalletDb
import nz.eloque.foss_wallet.persistence.buildDb
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationDao
import nz.eloque.foss_wallet.persistence.localization.PassLocalizationRepository
import nz.eloque.foss_wallet.persistence.pass.PassDao
import nz.eloque.foss_wallet.persistence.pass.PassRepository
import java.util.concurrent.Callable


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideLocalizationRepository(localizationDao: PassLocalizationDao): PassLocalizationRepository {
        return PassLocalizationRepository(localizationDao)
    }

    @Provides
    fun providePassRepository(@ApplicationContext context: Context, passDao: PassDao): PassRepository {
        return PassRepository(context, passDao)
    }

    @Provides
    @Singleton
    fun provideWalletDb(@ApplicationContext context: Context): WalletDb {
        return buildDb(context)
    }

    @Provides
    fun providePassDao(walletDb: WalletDb): PassDao {
        return walletDb.passDao()
    }

    @Provides
    fun provideTransactionalExecutor(walletDb: WalletDb): TransactionalExecutor {
        return object : TransactionalExecutor {
            override fun <T> runTransactionally(callable: Callable<T>): T = walletDb.runInTransaction(callable)
            override fun runTransactionally(runnable: Runnable) = walletDb.runInTransaction(runnable)
        }
    }

    @Provides
    fun provideLocalizationDAo(walletDb: WalletDb): PassLocalizationDao {
        return walletDb.localizationDao()
    }

    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)!!
    }
}